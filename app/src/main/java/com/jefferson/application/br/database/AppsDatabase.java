package com.jefferson.application.br.database;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class AppsDatabase extends SQLiteOpenHelper {

    public static final  String DATABASE_NAME = "PACKAGES_DB";
    
    public AppsDatabase(Context context) {

        super(context, DATABASE_NAME, null, 1);
    }

	public void clearUnlockedApps() {
		getWritableDatabase().execSQL("DELETE FROM UNLOCKED_APP_TABLE;");
	}

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE LOCKED_APP_TABLE(" + 
				   "ID INTEGER PRIMARY KEY AUTOINCREMENT,PACKAGE TEXT)");
		db.execSQL("CREATE TABLE UNLOCKED_APP_TABLE(" +
				   "ID INTEGER PRIMARY KEY,PACKAGE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS UNLOCKED_APP_TABLE");
    }

    public ArrayList<String> getLockedPackages() {
        SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<String> list = new ArrayList<String>();
        Cursor res = db.rawQuery("Select * from LOCKED_APP_TABLE", null);

		while (res.moveToNext()) {
			list.add(res.getString(1));
		}
		res.close();
		db.close();
        return  list;
    }
    
	public boolean isAppUnlocked(String pack) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("Select * From UNLOCKED_APP_TABLE" , null);
		while (cursor.moveToNext()) {
			if (pack.equals(cursor.getString(1)))
				return true;
		}
		db.close();
		return false;
	}
    
	public void removeLockedApp(String appName) {

		getWritableDatabase().execSQL("DELETE FROM LOCKED_APP_TABLE WHERE PACKAGE = '" + appName + "'");
	}
    
	public void addUnlockedApp(String pack) {
		SQLiteDatabase database = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("PACKAGE", pack);
		database.insert("UNLOCKED_APP_TABLE", null, values);
		database.close();
	}
    
	public boolean addLockedApp(String name) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PACKAGE", name);

        long result = db.insert("LOCKED_APP_TABLE", null, contentValues);
        db.close();

        if (result == -1) {
            return false;
        } else {
            return true;
		}
	}
}
