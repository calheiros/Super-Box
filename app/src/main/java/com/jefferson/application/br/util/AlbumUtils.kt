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
import android.telephony.mbms.StreamingServiceInfo
import android.widget.Toast
import com.jefferson.application.br.R
import com.jefferson.application.br.database.PathsDatabase
import com.jefferson.application.br.fragment.AlbumFragment
import com.jefferson.application.br.model.AlbumModel
import com.jefferson.application.br.model.FileModel
import java.io.File

object AlbumUtils {

    private fun deleteAlbum(albumPath: String, context: Context): Result {
        val file = File(albumPath)
        val database = PathsDatabase.getInstance(context)
        val id = file.name
        val success = file.delete()
        if (success)
            database.deleteMediaData(id)
        return Result(success, if(success) "success" else "failed")
    }
    /**
     * Rename album from database
     */
    fun renameAlbum(
        context: Context?,
        model: AlbumModel,
        newName: String,
        position: Int
    ): Boolean {
        val database: PathsDatabase = PathsDatabase.getInstance(context!!)
        try {
            val folderType = if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
            val file = File(model.path!!)
            val id = file.name
            val folderName = database.getFolderName(id, folderType)
            val newFolderId = database.getFolderIdFromName(newName, folderType)
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
                database.addFolderName(id, newName, folderType)
            } else {
                database.updateFolderName(id, newName, folderType)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            database.close()
        }
        return true
    }

    fun createAlbum(context: Context?, name: String, position: Int): AlbumModel? {
        val database: PathsDatabase = PathsDatabase.getInstance(context!!)
        var folder: AlbumModel? = null
        try {
            val type = if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
            var id = database.getFolderIdFromName(name, type)
            val randomStr = StringUtils.getRandomString(24)
            if (id == null) {
                id = randomStr
                val strType = if (position == 0) Storage.IMAGE else Storage.VIDEO
                val file = File(Storage.getFolder(strType, context), randomStr)
                if (file.mkdirs()) {
                    folder = AlbumModel()
                    database.addFolderName(id, name, type)
                    folder.name = name
                    folder.path = file.absolutePath
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
    fun validateName(name: String, context: Context?): com.jefferson.application.br.util.Result {
        val noSpace = name.replace(" ", "")
        return if (noSpace.isEmpty()) {
            Result(false, context?.getString(R.string.pasta_nome_vazio))
        } else if (name.length > 50) {
            Result(false, context?.getString(R.string.pasta_nome_muito_grande))
        } else {
            Result(true, AlbumFragment.ALBUM_NAME_OKAY)
        }
    }

    fun removeFromFavorites(f_model: AlbumModel, context: Context) {
        val database = PathsDatabase.getInstance(context)
        val file = File(f_model.path!!)
        val name = file.name
        if (!database.removeFavoriteFolder(name)) {
            Toast.makeText(context, "failed to remove from bookmark", Toast.LENGTH_SHORT)
                .show()
            return
        }
        f_model.isFavorite = false
    }
}

class Result {
    var ok: Boolean = false
    var message :String? = null
    var model: AlbumModel? = null

    constructor(ok: Boolean, message: String?, model: AlbumModel?) {
        this.ok = ok
        this.message = message
        this.model = model
    }

    constructor(ok: Boolean, message: String?): this(ok, message, null)
    constructor(ok: Boolean): this(ok, null, null)
}