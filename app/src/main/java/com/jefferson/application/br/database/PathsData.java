package com.jefferson.application.br.database;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;

public class PathsData extends SQLiteOpenHelper {

    public static final  String DATABASE_NAME = "database.db";
    public static final  String TABLE_NAME = "PATHS_";
	public static final int DATABASE_VERSION = 3;

    public static final  String COL_1 = "ID";
    public static final  String COL_2 = "NAME";

	public static String getBaseCommand() {
		return "CREATE TABLE " + TABLE_NAME + " (" + COL_1 + " TEXT NOT NULL," + COL_2 + " TEXT);";
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
		//Toast.makeText(App.getAppContext(),"onCreate called: database", 1).show();
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public String getPath(String id) {   
	    String path = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("Select " + COL_2 + " from " + TABLE_NAME + " WHERE " + COL_1 + " = '" + id + "';", null);
		if (res.moveToFirst())
			path = res.getString(0);
		res.close();
		db.close();
        return path;
    }

	public void deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_1 + " = '" + id + "';");
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
    
	public boolean insertData(String id , String name) {
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
		contentValues.put(COL_1, id);

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

			Cursor rawQuery = readableDatabase.rawQuery("SELECT id FROM FOLDER_ WHERE name = '" + str + "' AND type = '" + type +"'", (String[]) null);
			if (rawQuery.moveToFirst()) {
				return rawQuery.getString(0);
			}
			return null;
		}

		public void addName(String str, String str2, String type) {

			SQLiteDatabase writableDatabase = getWritableDatabase();
			writableDatabase.execSQL("INSERT INTO FOLDER_ VALUES ('" + str2 + "', '" + str + "', '" + type + "');");
			writableDatabase.close();
		}

		public void updateName(String id, String name, String type) {
           
            SQLiteDatabase writableDatabase = getWritableDatabase();
			writableDatabase.execSQL("UPDATE FOLDER_ SET name = '" + name + "' WHERE id = '" + id + "' AND type = '" + type +"'");
			writableDatabase.close();
		}

		public static Folder getInstance(Context context) {
			
            File file = new File(Storage.getDefaultStorage(),"database.db");
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
			sQLiteDatabase.execSQL("DROP TABLE IF EXISTS FOLDER_");
		}
	}
}

