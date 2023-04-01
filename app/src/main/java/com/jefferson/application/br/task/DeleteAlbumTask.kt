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
package com.jefferson.application.br.task

import android.app.Activity
import android.widget.Toast
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.database.AlbumDatabase.Companion.getInstance
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.util.Storage.getDefaultStoragePath
import java.io.File

open class DeleteAlbumTask(
    activity: Activity, items: List<String>, position: Int, rootFile: File
) : JTask() {
    var progress = 0
    private val items: List<String?>
    private lateinit var dialog: SimpleDialog
    private val activity: Activity
    private val position: Int
    private val rootFile: File
    private var deletedAll = false
    private var listener: OnFinishedListener? = null

    init {
        this.items = items
        this.position = position
        this.rootFile = rootFile
        this.activity = activity
    }

    fun deletedAll(): Boolean {
        return deletedAll
    }

    override fun workingThread() {
        val database = getInstance(activity, getDefaultStoragePath(activity))
        for (path in items) {
            if (isInterrupted) {
                break
            }
            val file = File(path!!)
            if (file.delete()) {
                progress++
                var name: String?
                if (database.getMediaPath(file.name).also { name = it } != null) {
                    database.deleteMediaData(file.name)
                }
                postUpdate(path, name)
            }
        }
        database.close()
    }

    override fun onStarted() {
        dialog = SimpleDialog(activity)
        dialog.showProgressBar(items.isNotEmpty()).setTitle("Deleting").setMax(items.size)
            .setProgress(0).showPositiveButton(false)
            .setNegativeButton(activity.getString(R.string.cancelar),
                object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        interrupt()
                        return true
                    }
                })
        if (items.size > 10) {
            dialog.show()
        }
    }

    override fun onFinished() {
        dialog.cancel()
        deletedAll = rootFile.list()?.size == 0
        if (deletedAll) {
            deleteFolder(rootFile)
        }
        listener?.onFinished()
    }

    fun setOnFinishedListener(listener: OnFinishedListener) {
        this.listener = listener
    }

    override fun onException(e: Exception?) {
        Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupted() {
        super.onInterrupted()
        Toast.makeText(activity, activity.getString(R.string.canceledo_usuario), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onUpdated(args: Array<out Any>?) {
        super.onUpdated(args)
        dialog.setProgress(progress)
        dialog.setMessage((args?.get(1) as String))
    }

    private fun deleteFolder(file: File) {
        val database = getInstance(activity)
        if (file.delete()) {
            database.deleteAlbum(
                file.name, if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
            )
        }
        database.close()
    }
}