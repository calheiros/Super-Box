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
package com.jefferson.application.br.util

import android.content.Context
import android.widget.Toast
import com.jefferson.application.br.R
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.fragment.AlbumFragment
import com.jefferson.application.br.model.AlbumModel
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.SimpleAlbumModel
import java.io.File

object AlbumUtils {

    private fun deleteEmptyAlbum(albumPath: String, context: Context): Result {
        val file = File(albumPath)
        val database = AlbumDatabase.getInstance(context)
        val id = file.name
        val success = file.delete()
        if (success)
            database.deleteMediaData(id)
        return Result(success, if (success) "success" else "failed")
    }

    /**
     * Rename album from database
     */
    fun renameAlbum(
        context: Context,
        model: SimpleAlbumModel,
        newName: String,
        position: Int
    ): Boolean {
        val success = renameAlbum(context, model.albumPath, newName, position)
        if (success)
            model.albumName = newName
        return success
    }

    fun renameAlbum(
        context: Context,
        albumPath: String,
        newName: String,
        position: Int
    ): Boolean {
        val database: AlbumDatabase = AlbumDatabase.getInstance(context)
        try {
            val folderType = if (position == 0) AlbumDatabase.IMAGE_TYPE else AlbumDatabase.VIDEO_TYPE
            val file = File(albumPath)
            val id = file.name
            val folderName = database.getAlbumName(id, folderType)
            val newFolderId = database.getAlbumIdFromName(newName, folderType)
            if (folderName != null && folderName == newName) {
                Toast.makeText(
                    context,
                    context.getString(R.string.pasta_mesmo_nome),
                    Toast.LENGTH_LONG
                ).show()
                database.close()
                return false
            }
            if (newFolderId != null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.pasta_existe),
                    Toast.LENGTH_LONG
                ).show()
                database.close()
                return false
            }
            if (folderName == null) {
                database.addAlbum(id, newName, folderType)
            } else {
                database.updateAlbumName(id, newName, folderType)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            database.close()
        }
        return true
    }

    fun createAlbum(context: Context?, name: String, position: Int): SimpleAlbumModel? {
        val database: AlbumDatabase = AlbumDatabase.getInstance(context!!)
        var folder: SimpleAlbumModel? = null
        try {
            val type = if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
            var id = database.getAlbumIdFromName(name, type)
            val randomStr = StringUtils.getRandomString(24)
            if (id == null) {
                id = randomStr
                val strType = if (position == 0) Storage.IMAGE else Storage.VIDEO
                val file = File(Storage.getFolder(strType, context), randomStr)
                if (file.mkdirs()) {
                    folder = SimpleAlbumModel(name, file.absolutePath)
                    database.addAlbum(id, name, type)
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.pasta_existe),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.close()
        }
        return folder
    }

    /*
     ** validate album name
    */
    fun validateName(name: String, context: Context?): Result {
        val noSpace = name.replace(" ", "")
        return if (noSpace.isEmpty()) {
            Result(false, context?.getString(R.string.pasta_nome_vazio))
        } else if (name.length > 50) {
            Result(false, context?.getString(R.string.pasta_nome_muito_grande))
        } else {
            Result(true, AlbumFragment.ALBUM_NAME_OKAY)
        }
    }

    fun removeFromFavorites(albumModel: SimpleAlbumModel, context: Context) {
        val database = AlbumDatabase.getInstance(context)
        val file = File(albumModel.albumPath)
        val name = file.name
        if (!database.removeFavoriteFolder(name)) {
            Toast.makeText(context, "failed to remove from bookmark", Toast.LENGTH_SHORT)
                .show()
            return
        }
        albumModel.isFavorite = false
    }
}

class Result {
    var ok: Boolean = false
    var message: String? = null
    var model: AlbumModel? = null

    constructor(ok: Boolean, message: String?, model: AlbumModel?) {
        this.ok = ok
        this.message = message
        this.model = model
    }

    constructor(ok: Boolean, message: String?) : this(ok, message, null)
    constructor(ok: Boolean) : this(ok, null, null)
}