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

import android.content.Context
import android.widget.Toast
import com.jefferson.application.br.model.FileModel

class FileModelBuilderTask(
    private val context: Context,
    private val paths: ArrayList<String>,
    private val type: String?,
    private val parentPath: String?
) : JTask() {
    val data = ArrayList<FileModel>()
    override fun workingThread() {
        for (i in paths.indices) {
            if (isCancelled) {
                break
            }
            val path = paths[i]
            val model = FileModel()
            model.resource = path
            model.parentPath = parentPath
            model.type = type
            data.add(model)
            postUpdate(1, i + 1, paths.size)
        }
    }

    override fun onStarted() {}
    override fun onTaskCancelled() {
        super.onTaskCancelled()
        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
    }

    override fun onFinished() {}
    override fun onException(e: Exception?) {}
    fun setDestination(absolutePath: String?) {}
}