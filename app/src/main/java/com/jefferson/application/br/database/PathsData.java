package com.jefferson.application.br.database;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.App;
import android.util.Log;

public class PathsData extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final String TABLE_NAME = "PATHS_";
	public static final int DATABASE_VERSION = 10;

    public static final String ID_COL = "ID";
    public static final String NAME_COL = "NAME";
    public static final String DURATION_COL = "DURATION";

    public static void onUpgradeDatabase(SQLiteDatabase sQLiteDatabase, int oldVersion, int newVersion) {
        Log.e("DATABASE", " UPGRADE: oldVersion => " + oldVersion);
        if (oldVersion <= 9) {
            sQLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + DURATION_COL + " INTEGER DEFAULT 0");
        }
    }

	public static String getBaseCommand() {
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID_COL + " TEXT NOT NULL," + NAME_COL + " TEXT, " + DURATION_COL + " INTEGER DEFAULT 0);";
	}

    private PathsData(Context context, String path) {

		super(context, path, null, DATABASE_VERSION);
		//SQLiteDatabase.openOrCreateDatabase(path, null);
    }

	public static PathsData getInstance(Context context, String path) {
		return new PathsData(context, path + "/" +  DATABASE_NAME);
	}

    @Override
    public void onCreate(SQLiteDatabase db) {
		db.execSQL(getBaseCommand());
		db.execSQL(PathsData.Folder.base_command_sql);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgradeDatabase(db, oldVersion, newVersion);
    }

    public void updateFileDuration(String name, int millSecond) {

        SQLiteDatabase database = getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(DURATION_COL, millSecond);
        Log.i("Database", "duration " + millSecond);
        database.update(TABLE_NAME, data, ID_COL + " = '" + name + "'", null);
    }

    public int getDuration(String fileName) {

        int duration = 1;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + DURATION_COL + " FROM " + TABLE_NAME + " WHERE " + ID_COL + " = '" + fileName + "';", null);

        if (cursor.moveToFirst()) {
            duration = cursor.getInt(0);
        }
        cursor.close();
        database.close();
        return duration;
    }

    public String getPath(String id) {

	    String path = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("Select " + NAME_COL + " from " + TABLE_NAME + " WHERE " + ID_COL + " = '" + id + "';", null);

        if (res.moveToFirst())
			path = res.getString(0);
		res.close();
		db.close();
        return path;
    }

	public void deleteData(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ID_COL + " = '" + id + "';");
		db.close();
	}

    public List<String> getAllData() {

        SQLiteDatabase db = this.getReadableDatabase();
		List<String> allData = new ArrayList<String>();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME, null);

		while (cursor.moveToNext()) {
			allData.add(cursor.getString(1));
		}
		cursor.close();
		db.close();

		return allData;
	}

    public boolean insertData(String id , String name, long duration) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COL, name);
        contentValues.put(ID_COL, id);
        contentValues.put(DURATION_COL, duration);

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

	public boolean insertData(String id, String name) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COL, name);
		contentValues.put(ID_COL, id);

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        if (result == -1) {
            return false;
        } else {
            return true;
		}
	}

	public static class Folder extends SQLiteOpenHelper {

        public static String base_command_sql = "CREATE TABLE IF NOT EXISTS FOLDER_ (id TEXT NOT NULL, name TEXT, type VARCHAR(6) NOT NULL);";

		@Override
		public void onCreate(SQLiteDatabase sQLiteDatabase) {
			sQLiteDatabase.execSQL(base_command_sql);
			sQLiteDatabase.execSQL(PathsData.getBaseCommand());

		}

		public String getFolderId(String str, String type) {

			SQLiteDatabase readableDatabase = getReadableDatabase();
            Cursor rawQuery = readableDatabase.rawQuery("SELECT id FROM FOLDER_ WHERE name = '" + str + "' AND type = '" + type + "'", (String[]) null);
			
            if (rawQuery.moveToFirst()) {
				return rawQuery.getString(0);
			}
			return null;
		}

		public void addName(String id, String name, String type) {

			SQLiteDatabase writableDatabase = getWritableDatabase();
			writableDatabase.execSQL("INSERT INTO FOLDER_ VALUES ('" + id + "', '" + name + "', '" + type + "');");
			writableDatabase.close();
		}

		public void updateName(String id, String name, String type) {

            SQLiteDatabase writableDatabase = getWritableDatabase();
			writableDatabase.execSQL("UPDATE FOLDER_ SET name = '" + name + "' WHERE id = '" + id + "' AND type = '" + type + "'");
			writableDatabase.close();
		}

		public static Folder getInstance(Context context) {

            File file = new File(Storage.getDefaultStorage(), "database.db");
			file.getParentFile().mkdirs();
            return new Folder(file.getAbsolutePath(), context);
		}

		private Folder(String str, Context context) {

			super(context, str, null, PathsData.DATABASE_VERSION);
			SQLiteDatabase.openOrCreateDatabase(str, null);
		}

		public String getFolderName(String str, String type) {

			SQLiteDatabase readableDatabase = getReadableDatabase();
			Cursor rawQuery = readableDatabase.rawQuery("SELECT name FROM FOLDER_ WHERE id='" + str + "' AND type = '" + type + "';", null);
			String res = null;

			if (rawQuery.moveToFirst()) {
				res = rawQuery.getString(0);
			}

			rawQuery.close();
			readableDatabase.close();
			return res;
		}

		public void delete(String f_name, String type) {
			SQLiteDatabase writableDatabase = getWritableDatabase();
			writableDatabase.execSQL("DELETE FROM FOLDER_ WHERE id='" + f_name + "' AND type = '" + type + "';");
		}

		@Override
		public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
			sQLiteDatabase.execSQL(base_command_sql);
            PathsData.onUpgradeDatabase(sQLiteDatabase, i, i2);
		}
	}
}

