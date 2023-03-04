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
package com.jefferson.application.br.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppLockDatabase(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    fun clearUnlockedApps() {
        writableDatabase.execSQL("DELETE FROM UNLOCKED_APP_TABLE;")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE LOCKED_APP_TABLE(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,PACKAGE TEXT)"
        )
        db.execSQL(
            "CREATE TABLE UNLOCKED_APP_TABLE(" +
                    "ID INTEGER PRIMARY KEY,PACKAGE TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UNLOCKED_APP_TABLE")
    }

    val lockedPackages: ArrayList<String>
        get() {
            val db = this.writableDatabase
            val list = ArrayList<String>()
            val res = db.rawQuery("Select * from LOCKED_APP_TABLE", null)
            while (res.moveToNext()) {
                list.add(res.getString(1))
            }
            res.close()
            db.close()
            return list
        }

    fun isAppUnlocked(pack: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("Select * From UNLOCKED_APP_TABLE", null)
        while (cursor.moveToNext()) {
            if (pack == cursor.getString(1)) return true
        }
        cursor.close()
        db.close()
        return false
    }

    fun removeLockedApp(appName: String?) {
        if (appName == null) return
        writableDatabase.execSQL("DELETE FROM LOCKED_APP_TABLE WHERE PACKAGE = '$appName'")
    }

    fun addUnlockedApp(pack: String?) {
        if (pack == null) return
        val database = this.writableDatabase
        val values = ContentValues()
        values.put("PACKAGE", pack)
        database.insert("UNLOCKED_APP_TABLE", null, values)
        database.close()
    }

    fun addLockedApp(name: String?): Boolean {
        if (name == null) return false
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("PACKAGE", name)
        val result = db.insert("LOCKED_APP_TABLE", null, contentValues)
        db.close()
        return result != -1L
    }

    companion object {
        @JvmStatic
        fun getInstance(context: Context): AppLockDatabase {
            return AppLockDatabase(context)
        }

        const val DATABASE_NAME = "PACKAGES_DB"
    }
}