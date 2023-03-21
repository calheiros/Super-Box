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

class AlbumDatabase private constructor(context: Context, path: String) :
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
                    "ALTER TABLE $ALBUM_TABLE_NAME ADD COLUMN favorite" +
                            " INTEGER DEFAULT 0"
                )
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_MEDIA_TABLE_SQL)
        db.execSQL(CREATE_ALBUM_TABLE_SQL)
    }

    fun setFavoriteFolder(folder: String?): Boolean {
        return updateFavoriteAlbum(folder, 1)
    }

    fun removeFavoriteFolder(folder: String?): Boolean {
        return updateFavoriteAlbum(folder, 0)
    }

    fun updateFavoriteAlbum(folderName: String?, value: Int): Boolean {
        var result = false
        val db = writableDatabase
        val values = ContentValues()
        values.put("favorite", value)
        val rowsUpdated = db.update(ALBUM_TABLE_NAME, values, "id = ?", arrayOf(folderName))
        if (rowsUpdated > 0) {
            result = true
        }
        return result
    }

    fun isFavoriteAlbum(folderName: String): Boolean {
        var result = false
        val db = readableDatabase
        val query = "SELECT * FROM $ALBUM_TABLE_NAME WHERE favorite = 1 AND id = ?"
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

    fun updateVideoDuration(name: String, millSecond: Int) {
        val database = writableDatabase
        val data = ContentValues()
        data.put(MEDIA_DURATION_COL, millSecond)
        database.update(MEDIA_TABLE_NAME, data, "$MEDIA_ID_COL = ?", arrayOf(name))
        database.close()
    }

    fun getDuration(fileName: String): Int {
        var duration = -1
        val database = readableDatabase
        val cursor = database.rawQuery(
            "SELECT $MEDIA_DURATION_COL FROM $MEDIA_TABLE_NAME" +
                    " WHERE $MEDIA_ID_COL  = ?", arrayOf(fileName)
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
            "Select $MEDIA_PATH_COL from $MEDIA_TABLE_NAME" +
                    " WHERE $MEDIA_ID_COL = ?", arrayOf(id), null
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
            db.execSQL("DELETE FROM $MEDIA_TABLE_NAME WHERE $MEDIA_ID_COL = ?", arrayOf(id))
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
        contentValues.put(MEDIA_PATH_COL, name)
        contentValues.put(MEDIA_ID_COL, id)
        contentValues.put(MEDIA_DURATION_COL, duration)
        val result = db.insert(MEDIA_TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    fun insertMediaData(id: String?, name: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(MEDIA_PATH_COL, name)
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

    fun getAlbumIdFromName(name: String, type: String): String? {
        val readableDatabase = readableDatabase
        val rawQuery = readableDatabase.rawQuery(
            "SELECT id FROM FOLDER_ WHERE name = ? AND type = ?",
            arrayOf(name, type),null
        )
        if (rawQuery.moveToFirst()) {
            return rawQuery.getString(0)
        }
        rawQuery.close()
        return null
    }

    fun addAlbum(id: String?, name: String?, type: String?) {
        val writableDatabase = writableDatabase
        val values = ContentValues()
        values.put("id", id)
        values.put("name", name)
        values.put("type", type)
        writableDatabase.insert(ALBUM_TABLE_NAME, null, values)
        writableDatabase.close()
    }

    fun updateAlbumName(id: String, name: String, type: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("type", type)
        }
        writableDatabase.update(ALBUM_TABLE_NAME, values, "id = ?", arrayOf(id))
        writableDatabase.close()
    }

    fun getAlbumName(id: String, type: String): String? {
        val database = readableDatabase
        val rawQuery = database.rawQuery(
            "SELECT name FROM FOLDER_ WHERE id=? AND type=?",
            arrayOf(id, type)
        )
        var res: String? = null
        try {
            if (rawQuery.moveToFirst()) {
                res = rawQuery.getString(0)
            }
        } finally {
            rawQuery.close()
            database.close()
        }
        return res
    }

    fun deleteAlbum(f_name: String, type: String) {
        val database = writableDatabase
        try {
            val deleteQuery = "DELETE FROM FOLDER_ WHERE id = ? AND type = ?"
            val deleteArgs = arrayOf(f_name, type)
            database.execSQL(deleteQuery, deleteArgs)
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            database.close()
        }
    }

    fun mediaIdExists(id: String): Boolean {
        val exists: Boolean
        val database = readableDatabase
        val cursor = database.rawQuery(
            "SELECT $MEDIA_ID_COL FROM $MEDIA_TABLE_NAME WHERE $MEDIA_ID_COL = ?", arrayOf(id)
        )
        exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    val favoritesAlbum: Map<String, Boolean>
        get() {
            val favorites: MutableMap<String, Boolean> = HashMap()
            val data = readableDatabase
            val cursor = data.query(
                ALBUM_TABLE_NAME,
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
        const val IMAGE_TYPE = "imagem"
        const val VIDEO_TYPE = "video"
        const val DATABASE_NAME = "database.db"
        const val MEDIA_TABLE_NAME = "PATHS_"
        const val DATABASE_VERSION = 11
        const val MEDIA_ID_COL = "ID"
        const val MEDIA_PATH_COL = "NAME"
        const val MEDIA_DURATION_COL = "DURATION"
        const val ALBUM_TABLE_NAME = "FOLDER_"

        val CREATE_ALBUM_TABLE_SQL: String
            get() {
            return "CREATE TABLE IF NOT EXISTS " + ALBUM_TABLE_NAME +
                    "(id TEXT NOT NULL, " +
                    "name TEXT," +
                    " type VARCHAR(6) NOT NULL," +
                    "favorite INTEGER DEFAULT 0);"
        }
        val CREATE_MEDIA_TABLE_SQL: String
            get() = ("CREATE TABLE IF NOT EXISTS " + MEDIA_TABLE_NAME + " (" + MEDIA_ID_COL + " TEXT NOT NULL,"
                    + MEDIA_PATH_COL + " TEXT, " + MEDIA_DURATION_COL + " INTEGER DEFAULT -1);")

        @JvmStatic
        fun getInstance(context: Context, path: String): AlbumDatabase {
            return AlbumDatabase(context, "$path/$DATABASE_NAME")
        }

        @JvmStatic
        fun getInstance(context: Context): AlbumDatabase {
            val file = File(Storage.getDefaultStoragePath(context), "database.db")
            file.parentFile?.mkdirs()
            return AlbumDatabase(context, file.absolutePath)
        }
    }
}