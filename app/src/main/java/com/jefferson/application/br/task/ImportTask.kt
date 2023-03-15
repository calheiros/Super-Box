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
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.database.AlbumDatabase.Companion.getInstance
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.util.FileTransfer
import com.jefferson.application.br.util.Storage
import com.jefferson.application.br.util.StringUtils
import java.io.*

class ImportTask(
    private val activity: Activity, models: ArrayList<FileModel>, private val listener: Listener?
) : JTask() {
    private val importedFilesPath = ArrayList<String>()
    private val maxProgress: Int
    private val models: ArrayList<FileModel>
    private val errorMessage = StringBuilder()
    private val mTransfer: FileTransfer
    private val noLeftSpaceErrorMessage = "\nNão há espaço suficiente no dispositivo\n"
    private var error: Exception? = null
    private var watchTransfer: WatchTransference? = null
    var failuresCount = 0
        private set
    var isWaiting = false
        private set

    init {
        maxProgress = models.size
        this.models = models
        mTransfer = FileTransfer()
    }

    override fun onException(e: Exception) {
        error = e
        failuresCount = 1
        revokeFinish(false)
        errorMessage.append(e.message)
        e.printStackTrace()
    }

    override fun onStarted() {
        listener?.onBeingStarted()
    }

    fun error(): Exception? {
        return error
    }

    override fun onTaskCancelled() {
        super.onTaskCancelled()
        Toast.makeText(activity, "Task cancelled!", Toast.LENGTH_SHORT).show()
    }

    override fun onFinished() {
        refreshMediaStore()
        listener?.onFinished()
    }

    private fun refreshMediaStore() {
        Storage.scanMediaFiles(importedFilesPath.toTypedArray(), activity)
    }

    public override fun onInterrupted() {
        listener?.onInterrupted()
        Toast.makeText(activity, activity.getString(R.string.canceledo_usuario), Toast.LENGTH_SHORT)
            .show()
    }

    public override fun onUpdated(values: Array<Any>) {}
    override fun workingThread() {
        var max = 0.0
        val database: AlbumDatabase = getInstance(
            activity, Storage.getDefaultStoragePath(
                activity
            )
        )
        for (resource in models) {
            val file = File(resource.resource!!)
            max += file.length().toDouble()
        }
        val target = File(
            Storage.getDefaultStoragePath(
                activity
            )
        )
        if (target.freeSpace < max) {
            sendUpdate(-2, activity.getString(R.string.sem_espaco_aviso))
            waitForResponse()
        }
        max /= 1024.0
        sendUpdate(PROGRESS_UPDATE, null, null, max)
        watchTransfer = WatchTransference(this, mTransfer)
        watchTransfer?.start()
        for (i in models.indices) {
            if (isInterrupted) {
                break
            }
            val model = models[i]
            val file = File(model.resource!!)
            if (!file.exists()) {
                failuresCount++
                errorMessage.append(
                    """
    ${activity.getString(R.string.erro)} $failuresCount: O arquivo "${file.name}" não existe!
    """
                )
                continue
            }
            sendUpdate(PROGRESS_UPDATE, file.name, null, null)
            val albumName = file.parentFile?.name ?: ""
            val mediaFileName = StringUtils.getRandomString(24)
            var albumFileName = StringUtils.getRandomString(24)
            val albumId = database.getAlbumIdFromName(albumName, model.type!!)
            if (albumId == null) {
                database.addAlbum(albumFileName, albumName, model.type)
            } else {
                albumFileName = albumId
            }
            var parentPath = model.parentPath
            if (parentPath == null) {
                parentPath = Storage.getFolder(
                    if (FileModel.IMAGE_TYPE == model.type) Storage.IMAGE else Storage.VIDEO,
                    activity
                ).toString() + File.separator + albumFileName
            }
            val destFile = File(parentPath, mediaFileName)
            destFile.parentFile?.mkdirs()
            if (file.renameTo(destFile)) {
                database.insertMediaData(mediaFileName, model.resource)
                importedFilesPath.add(file.absolutePath)
                mTransfer.increment(destFile.length().toDouble() / 1024.0)
            } else {
                var inputStream: InputStream?
                var outputStream: FileOutputStream?
                try {
                    inputStream = FileInputStream(file)
                    outputStream = FileOutputStream(destFile)
                } catch (e: FileNotFoundException) {
                    failuresCount++
                    continue
                }
                val response = mTransfer.transferStream(inputStream, outputStream)
                if (FileTransfer.OK == response) {
                    if (Storage.deleteFile(file, activity)) {
                        database.insertMediaData(mediaFileName, model.resource)
                        importedFilesPath.add(file.absolutePath)
                    } else {
                        destFile.delete()
                    }
                } else {
                    destFile.delete()
                    failuresCount++
                    if (FileTransfer.Error.NO_LEFT_SPACE == response) {
                        errorMessage.append(noLeftSpaceErrorMessage)
                    } else {
                        errorMessage.append(
                            """
    ${activity.getString(R.string.erro)}$failuresCount: $response when moving: ${file.name}
    """
                        )
                    }
                }
            }
            sendUpdate(PREPARATION_UPDATE, i + 1 - failuresCount, models.size)
        }
        database.close()
    }

    private fun waitForResponse() {
        isWaiting = true
        while (isWaiting) {
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                isWaiting = false
                break
            }
        }
    }

    fun stopWaiting() {
        isWaiting = false
    }

    interface Listener {
        fun onBeingStarted()
        fun onUserInteraction()
        fun onInterrupted()
        fun onFinished()
    }

    private inner class WatchTransference(
        private val task: JTask, private val transfer: FileTransfer
    ) : Thread() {
        override fun run() {
            super.run()
            while (task.status == Status.STARTED) {
                try {
                    sleep(50)
                } catch (_: InterruptedException) {
                }
                task.sendUpdate(PROGRESS_UPDATE, null, transfer.transferredKilobytes, null)
            }
        }
    }

    companion object {
        const val PREPARATION_UPDATE = 1
        const val PROGRESS_UPDATE = 2
    }
}