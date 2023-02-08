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
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.jefferson.application.br.util.Storage
import java.io.File

class PathsDatabase private constructor(context: Context, path: String) :
    SQLiteOpenHelper(context, path, null, DATABASE_VERSION) {
    private fun onUpgradeDatabase(sQLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e("DATABASE", " UPGRADE: oldVersion => $oldVersion")
        if (oldVersion <= 9) {
            try {
                sQLiteDatabase.execSQL(
                    "ALTER TABLE " + MEDIA_TABLE_NAME + " ADD COLUMN " + MEDIA_DURATION_COL +
                            " INTEGER DEFAULT -1"
                )
            } catch (err: SQLException) {
                err.printStackTrace()
            }
        }
        if (oldVersion <= 10) {
            try {
                sQLiteDatabase.execSQL(
                    "ALTER TABLE " + FOLDER_TABLE_NAME + " ADD COLUMN favorite" +
                            " INTEGER DEFAULT 0"
                )
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
       val CREATE_FOLDER_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + FOLDER_TABLE_NAME +
                "(id TEXT NOT NULL, " +
                "name TEXT," +
                " type VARCHAR(6) NOT NULL," +
                "favorite INTEGER DEFAULT 0);"
        db.execSQL(createMediaTableSql)
        db.execSQL(CREATE_FOLDER_TABLE_SQL)
    }

    fun setFavoriteFolder(folder: String?): Boolean {
        return updateFavoriteFolder(folder, 1)
    }

    fun removeFavoriteFolder(folder: String?): Boolean {
        return updateFavoriteFolder(folder, 0)
    }

    fun updateFavoriteFolder(folderName: String?, value: Int): Boolean {
        var result = false
        val db = writableDatabase
        val values = ContentValues()
        values.put("favorite", value)
        val rowsUpdated = db.update(FOLDER_TABLE_NAME, values, "id = ?", arrayOf(folderName))
        if (rowsUpdated > 0) {
            result = true
        }
        return result
    }

    fun isFavoriteFolder(folderName: String): Boolean {
        var result = false
        val db = readableDatabase
        val query = "SELECT * FROM $FOLDER_TABLE_NAME WHERE favorite = 1 AND id = ?"
        val cursor = db.rawQuery(query, arrayOf(folderName))
        if (cursor.moveToFirst()) {
            result = true
        }
        cursor.close()
        return result
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        super.onDowngrade(db, oldVersion, newVersion)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgradeDatabase(db, oldVersion, newVersion)
    }

    fun updateMediaDuration(name: String, millSecond: Int) {
        val database = writableDatabase
        val data = ContentValues()
        data.put(MEDIA_DURATION_COL, millSecond)
        Log.i("Database", "duration $millSecond")
        database.update(MEDIA_TABLE_NAME, data, "$MEDIA_ID_COL = '$name'", null)
        database.close()
    }

    fun getDuration(fileName: String): Int {
        var duration = -1
        val database = readableDatabase
        val cursor = database.rawQuery(
            "SELECT " + MEDIA_DURATION_COL + " FROM " + MEDIA_TABLE_NAME +
                    " WHERE " + MEDIA_ID_COL + " = '" + fileName + "';", null
        )
        try {
            if (cursor.moveToFirst()) {
                duration = cursor.getInt(0)
            }
        } finally {
            cursor.close()
            database.close()
        }
        return duration
    }

    fun getMediaPath(id: String): String? {
        var path: String? = null
        val db = this.readableDatabase
        val res = db.rawQuery(
            "Select " + MEDIA_NAME_COL + " from " + MEDIA_TABLE_NAME +
                    " WHERE " + MEDIA_ID_COL + " = '" + id + "';", null
        )
        try {
            if (res.moveToFirst()) path = res.getString(0)
        } finally {
            res.close()
            db.close()
        }
        return path
    }

    fun deleteMediaData(id: String) {
        this.writableDatabase.use { db ->
            db.execSQL("DELETE FROM $MEDIA_TABLE_NAME WHERE $MEDIA_ID_COL = '$id';")
            db.close()
        }
    }

    val allMediaData: List<String>
        get() {
            val db = this.readableDatabase
            val allData: MutableList<String> = ArrayList()
            val cursor = db.rawQuery("Select * from $MEDIA_TABLE_NAME", null)
            try {
                while (cursor.moveToNext()) {
                    allData.add(cursor.getString(1))
                }
            } finally {
                cursor.close()
                db.close()
            }
            return allData
        }

    fun insertMediaData(id: String?, name: String?, duration: Long): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(MEDIA_NAME_COL, name)
        contentValues.put(MEDIA_ID_COL, id)
        contentValues.put(MEDIA_DURATION_COL, duration)
        val result = db.insert(MEDIA_TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    fun insertMediaData(id: String?, name: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(MEDIA_NAME_COL, name)
        contentValues.put(MEDIA_ID_COL, id)
        var result: Long = -1
        try {
            result = db.insert(MEDIA_TABLE_NAME, null, contentValues)
        } catch (ignored: Exception) {
        } finally {
            db.close()
        }
        return result != -1L
    }

    fun getFolderIdFromName(name: String, type: String): String? {
        val readableDatabase = readableDatabase
        val rawQuery = readableDatabase.rawQuery(
            "SELECT id FROM FOLDER_ WHERE name = '$name' AND type = '$type'",
            null
        )
        if (rawQuery.moveToFirst()) {
            return rawQuery.getString(0)
        }
        rawQuery.close()
        return null
    }

    fun addFolderName(id: String?, name: String?, type: String?) {
        val writableDatabase = writableDatabase
        val values = ContentValues()
        values.put("id", id)
        values.put("name", name)
        values.put("type", type)
        writableDatabase.insert(FOLDER_TABLE_NAME, null, values)
        writableDatabase.close()
    }

    fun updateFolderName(id: String, name: String, type: String) {
        val writableDatabase = writableDatabase
        writableDatabase.execSQL("UPDATE FOLDER_ SET name = '$name' WHERE id = '$id' AND type = '$type'")
        writableDatabase.close()
    }

    fun getFolderName(str: String, type: String): String? {
        val readableDatabase = readableDatabase
        val rawQuery = readableDatabase.rawQuery(
            "SELECT name FROM FOLDER_ WHERE id='$str' AND type = '$type';",
            null
        )
        var res: String? = null
        try {
            if (rawQuery.moveToFirst()) {
                res = rawQuery.getString(0)
            }
        } finally {
            rawQuery.close()
            readableDatabase.close()
        }
        return res
    }

    fun deleteFolder(f_name: String, type: String) {
        val writableDatabase = writableDatabase
        try {
            writableDatabase.execSQL("DELETE FROM FOLDER_ WHERE id='$f_name' AND type = '$type';")
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            writableDatabase.close()
        }
    }

    val favoritesFolder: Map<String, Boolean>
        get() {
            val favorites: MutableMap<String, Boolean> = HashMap()
            val data = readableDatabase
            val cursor = data.query(
                FOLDER_TABLE_NAME,
                arrayOf("id", "favorite"),
                null,
                null,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val favorite = cursor.getInt(1) == 1
                favorites[id] = favorite
            }
            cursor.close()
            return favorites
        }

    companion object {
        const val DATABASE_NAME = "database.db"
        const val MEDIA_TABLE_NAME = "PATHS_"
        const val DATABASE_VERSION = 11
        const val MEDIA_ID_COL = "ID"
        const val MEDIA_NAME_COL = "NAME"
        const val MEDIA_DURATION_COL = "DURATION"
        const val FOLDER_TABLE_NAME = "FOLDER_"
        val createMediaTableSql: String
            get() = ("CREATE TABLE IF NOT EXISTS " + MEDIA_TABLE_NAME + " (" + MEDIA_ID_COL + " TEXT NOT NULL,"
                    + MEDIA_NAME_COL + " TEXT, " + MEDIA_DURATION_COL + " INTEGER DEFAULT -1);")
        @JvmStatic
        fun getInstance(context: Context, path: String): PathsDatabase {
            return PathsDatabase(context, "$path/$DATABASE_NAME")
        }

        @JvmStatic
        fun getInstance(context: Context): PathsDatabase {
            val file = File(Storage.getDefaultStoragePath(context), "database.db")
            file.parentFile?.mkdirs()
            return PathsDatabase(context, file.absolutePath)
        }
    }
}