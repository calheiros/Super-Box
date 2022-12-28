package com.jefferson.application.br.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jefferson.application.br.util.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathsDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final String MEDIA_TABLE_NAME = "PATHS_";
    public static final int DATABASE_VERSION = 11;

    public static final String MEDIA_ID_COL = "ID";
    public static final String MEDIA_NAME_COL = "NAME";
    public static final String MEDIA_DURATION_COL = "DURATION";

    public final static String FOLDER_TABLE_NAME = "FOLDER_";

    private PathsDatabase(Context context, String path) {
        super(context, path, null, DATABASE_VERSION);
        //SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    public static String getCreateMediaTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + MEDIA_TABLE_NAME + " (" + MEDIA_ID_COL + " TEXT NOT NULL,"
                + MEDIA_NAME_COL + " TEXT, " + MEDIA_DURATION_COL + " INTEGER DEFAULT -1);";
    }

    public static PathsDatabase getInstance(Context context, String path) {
        return new PathsDatabase(context, path + "/" + DATABASE_NAME);
    }

    public static PathsDatabase getInstance(Context context) {
        File file = new File(Storage.getDefaultStoragePath(), "database.db");
        file.getParentFile().mkdirs();
        return new PathsDatabase(context, file.getAbsolutePath());
    }

    public void onUpgradeDatabase(SQLiteDatabase sQLiteDatabase, int oldVersion, int newVersion) {
        Log.e("DATABASE", " UPGRADE: oldVersion => " + oldVersion);
        if (oldVersion <= 9) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE " + MEDIA_TABLE_NAME + " ADD COLUMN " + MEDIA_DURATION_COL +
                        " INTEGER DEFAULT -1");
            } catch (SQLException err) {
                //do nothing
            }
        }
        if (oldVersion <= 10) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE " + FOLDER_TABLE_NAME + " ADD COLUMN favorite" +
                        " INTEGER DEFAULT 0");
            } catch (SQLException e) {
                //do nothing
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOLDER_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + FOLDER_TABLE_NAME +
                "(id TEXT NOT NULL, " +
                "name TEXT," +
                " type VARCHAR(6) NOT NULL," +
                "favorite INTEGER DEFAULT 0);";
        db.execSQL(getCreateMediaTableSql());
        db.execSQL(CREATE_FOLDER_TABLE_SQL);
    }

    public boolean setFavoriteFolder(String folder) {
        return updateFavoriteFolder(folder, 1);
    }

    public boolean removeFavoriteFolder(String folder) {
        return updateFavoriteFolder(folder, 0);
    }

    public boolean updateFavoriteFolder(String folderName, int value) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("favorite", value);

        int rowsUpdated = db.update(FOLDER_TABLE_NAME, values, "id = ?", new String[]{folderName});
        if (rowsUpdated > 0) {
            result = true;
        }

        return result;
    }

    public boolean isFavoriteFolder(String folderName) {
        boolean result = false;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + FOLDER_TABLE_NAME + " WHERE favorite = 1 AND id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{folderName});

        if (cursor.moveToFirst()) {
            result = true;
        }

        cursor.close();
        return result;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgradeDatabase(db, oldVersion, newVersion);
    }

    public void updateMediaDuration(String name, int millSecond) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(MEDIA_DURATION_COL, millSecond);
        Log.i("Database", "duration " + millSecond);
        database.update(MEDIA_TABLE_NAME, data, MEDIA_ID_COL + " = '" + name + "'", null);
        database.close();
    }

    public int getDuration(String fileName) {
        int duration = -1;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + MEDIA_DURATION_COL + " FROM " + MEDIA_TABLE_NAME +
                " WHERE " + MEDIA_ID_COL + " = '" + fileName + "';", null);
        try {
            if (cursor.moveToFirst()) {
                duration = cursor.getInt(0);
            }
        } finally {
            cursor.close();
            database.close();
        }
        return duration;
    }

    public String getMediaPath(String id) {
        String path = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("Select " + MEDIA_NAME_COL + " from " + MEDIA_TABLE_NAME +
                " WHERE " + MEDIA_ID_COL + " = '" + id + "';", null);
        try {
            if (res.moveToFirst())
                path = res.getString(0);
        } finally {
            res.close();
            db.close();
        }
        return path;
    }

    public void deleteMediaData(String id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.execSQL("DELETE FROM " + MEDIA_TABLE_NAME + " WHERE " + MEDIA_ID_COL + " = '" + id + "';");
            db.close();
        }
    }

    public List<String> getAllMediaData() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> allData = new ArrayList<String>();
        Cursor cursor = db.rawQuery("Select * from " + MEDIA_TABLE_NAME, null);

        try {
            while (cursor.moveToNext()) {
                allData.add(cursor.getString(1));
            }
        } finally {
            cursor.close();
            db.close();
        }
        return allData;
    }

    public boolean insertMediaData(String id, String name, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEDIA_NAME_COL, name);
        contentValues.put(MEDIA_ID_COL, id);
        contentValues.put(MEDIA_DURATION_COL, duration);

        long result = db.insert(MEDIA_TABLE_NAME, null, contentValues);
        db.close();

        return result != -1;
    }

    public boolean insertMediaData(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEDIA_NAME_COL, name);
        contentValues.put(MEDIA_ID_COL, id);
        long result = -1;
        try {
            result = db.insert(MEDIA_TABLE_NAME, null, contentValues);
        } catch (Exception ignored) {
        } finally {
            db.close();
        }
        return result != -1;
    }

    public String getFolderIdFromName(String name, String type) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor rawQuery = readableDatabase.rawQuery("SELECT id FROM FOLDER_ WHERE name = '" + name + "' AND type = '" + type + "'", null);

        if (rawQuery.moveToFirst()) {
            return rawQuery.getString(0);
        }
        rawQuery.close();
        return null;
    }

    public void addFolderName(String id, String name, String type) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("type", type);
        writableDatabase.insert(FOLDER_TABLE_NAME, null, values);
        writableDatabase.close();
    }

    public void updateFolderName(String id, String name, String type) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.execSQL("UPDATE FOLDER_ SET name = '" + name + "' WHERE id = '" + id + "' AND type = '" + type + "'");
        writableDatabase.close();
    }

    public String getFolderName(String str, String type) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor rawQuery = readableDatabase.rawQuery("SELECT name FROM FOLDER_ WHERE id='" + str + "' AND type = '" + type + "';", null);
        String res = null;
        try {
            if (rawQuery.moveToFirst()) {
                res = rawQuery.getString(0);
            }
        } finally {
            rawQuery.close();
            readableDatabase.close();
        }
        return res;
    }

    public void deleteFolder(String f_name, String type) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            writableDatabase.execSQL("DELETE FROM FOLDER_ WHERE id='" + f_name + "' AND type = '" + type + "';");
        } finally {
            writableDatabase.close();
        }
    }

    public Map<String, Boolean> getFavoritesFolder() {
        Map<String, Boolean> favorites = new HashMap<>();
        SQLiteDatabase data = getReadableDatabase();
        Cursor cursor = data.query(FOLDER_TABLE_NAME, new String[]{"id", "favorite"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            boolean favorite = cursor.getInt(1) == 1;
            favorites.put(id, favorite);
        }
        cursor.close();
        return favorites;
    }

}
