package com.jefferson.application.br.util

import android.content.Context
import com.jefferson.application.br.database.PathsDatabase
import java.io.File

object MediaUtils {
    /**
     * delete media by
     * @param path
     **/
    fun deleteMedia(context: Context, path: String): Boolean {
        val database = PathsDatabase.getInstance(context)
        val file = File(path)
        val id = file.name
        val deleted = file.delete()
        if (deleted) {
            database.deleteMediaData(id)
        }
        database.close()
        return deleted
    }

}