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

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.nio.ByteBuffer

object Storage : DocumentUtil() {
    const val EXTERNAL = "external"
    const val IMAGE = 0
    const val INTERNAL = "internal"
    const val STORAGE_LOCATION = "storage_loacation"
    const val VIDEO = 1
    const val IMAGE_DIR_NAME = "b17rvm0891wgrqwoal5sg6rr"
    const val VIDEO_DIR_NAME = "bpe8x1svi9jvhmprmawsy3d8"
    const val EXTERNAL_URI_KEY = "external_uri"
    fun storeExternalUri(uri: String?, context: Context?) {
        MyPreferences.getSharedPreferencesEditor(context!!).putString(EXTERNAL_URI_KEY, uri)
            .commit()
    }

    fun writeFile(content: String?, target: File): Boolean {
        val parent = target.parentFile!!
        if (!parent.exists()) {
            parent.mkdirs()
        }
        try {
            val writer = FileWriter(target)
            writer.write(content)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getRecycleBinPath(context: Context): String {
        val file = File(getDefaultStoragePath(context), ".trashed")
        file.mkdirs()
        return file.absolutePath
    }

    fun writeFile(content: ByteArray?, target: File): Boolean {
        val parent = target.parentFile!!
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val buffer = ByteBuffer.wrap(content!!)
        try {
            val writer = FileOutputStream(target)
            val channel = writer.channel
            channel.write(buffer)
            channel.close()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun readFile(file: File?): String? {
        val builder = StringBuilder()
        var result: String? = null
        var c = 0
        val buff = CharArray(128)
        try {
            val reader = FileReader(file)
            while (reader.read(buff).also { c = it } > 0) {
                builder.append(buff, 0, c)
            }
            result = builder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun readFileToByte(file: File?): ByteArray? {
        val out = ByteArrayOutputStream()
        var result: ByteArray? = null
        var c = 0
        val buff = ByteArray(128)
        try {
            val reader = FileInputStream(file)
            while (reader.read(buff).also { c = it } > 0) {
                out.write(buff, 0, c)
            }
            reader.close()
            out.flush()
            out.close()
            result = out.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun setNewLocalStorage(selected: Int, context: Context?) {
        if (selected == 0 || selected == 1) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(STORAGE_LOCATION, if (selected == 0) INTERNAL else EXTERNAL).apply()
        }
    }

    @JvmStatic
    fun getExternalUri(context: Context?): Uri? {
        val string = MyPreferences.getSharedPreferences(context!!)
            .getString(EXTERNAL_URI_KEY, null)
            ?: return null
        return Uri.parse(string)
    }

    fun getFolder(type: Int, context: Context): File? {
        return when (type) {
            IMAGE -> File(
                getDefaultStoragePath(
                    context
                ), IMAGE_DIR_NAME
            )
            VIDEO -> File(
                getDefaultStoragePath(
                    context
                ), VIDEO_DIR_NAME
            )
            else -> null
        }
    }

    fun getStorageLocation(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(STORAGE_LOCATION, INTERNAL)
    }

    fun getStoragePosition(context: Context?): Int {
        val storageLocation = getStorageLocation(context)
        if (INTERNAL == storageLocation) {
            return 0
        }
        return if (EXTERNAL == storageLocation) {
            1
        } else -1
    }

    @JvmStatic
    fun getDefaultStoragePath(context: Context): String {
        val storageLocation = getStorageLocation(context)
        val extPath = getExternalStorage(context)
        return if (INTERNAL == storageLocation || extPath == null) {
            getInternalStorage(context)
        } else {
            extPath
        }
    }

    fun getExternalStorage(context: Context): String? {
        try {
            val externalFilesDirs = context.getExternalFilesDirs("") ?: return null
            for (file in externalFilesDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    return file.absolutePath
                }
                Log.i("SD PATH", file.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null as String?
    }

    @JvmStatic
    fun getInternalStorage(context: Context): String {
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/." + context.packageName + "/data"
        )
        file.mkdirs()
        return file.absolutePath
    }

    fun deleteFile(file: File, context: Context?): Boolean {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val documentFile = getDocumentFile(file, false, context)
            if (documentFile != null) return documentFile.delete()
        }
        return file.delete()
    }

    fun deleteFileFromMediaStore(file: File, context: Context) {
        val canonicalPath: String
        val contentResolver = context.contentResolver
        canonicalPath = try {
            file.canonicalPath
        } catch (e: IOException) {
            file.absolutePath
        }
        val contentUri = MediaStore.Files.getContentUri(EXTERNAL)
        val stringBuffer = StringBuffer()
        val args = stringBuffer.append("_data").append("=?").toString()
        val strArr = arrayOfNulls<String>(1)
        strArr[0] = canonicalPath
        if (contentResolver.delete(contentUri, args, strArr) == 0) {
            val absolutePath = file.absolutePath
            if (absolutePath != canonicalPath) {
                contentResolver.delete(contentUri, absolutePath, arrayOf(absolutePath))
            }
        }
    }

    fun scanMediaFiles(paths: Array<String>, context: Context) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mediaScannerConnection(paths, context)
        } else {
            for (path in paths) {
                val intent = Intent(Intent.ACTION_MEDIA_MOUNTED)
                intent.data = Uri.parse("file://$path")
                context.sendBroadcast(intent)
            }
        }
    }

    @JvmStatic
    fun getPath(uri: Uri?, context: Context?): String {
        return FileUtils(context).getPath(uri)
    }

    fun mediaScannerConnection(strArr: Array<String>?, context: Context?) {
        MediaScannerConnection.scanFile(
            context, strArr, null
        ) { p1, p2 -> }
    }

    @TargetApi(21)
    fun checkIfSDCardRoot(uri: Uri): Boolean {
        return isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)
    }

    @TargetApi(21)
    fun isRootUri(uri: Uri?): Boolean {
        return DocumentsContract.getTreeDocumentId(uri).endsWith(":")
    }

    @TargetApi(21)
    fun isInternalStorage(uri: Uri): Boolean {
        return isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains("primary")
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
}