/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class AppLockDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PACKAGES_DB";

    public AppLockDatabase(Context context) {
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
        return list;
    }

    public boolean isAppUnlocked(String pack) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * From UNLOCKED_APP_TABLE", null);
        while (cursor.moveToNext()) {
            if (pack.equals(cursor.getString(1)))
                return true;
        }
        cursor.close();
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
        return result != -1;
    }
}
