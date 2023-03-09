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
@file:Suppress("NAME_SHADOWING")

package com.jefferson.application.br.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.jefferson.application.br.util.Storage.getInternalStorage
import com.jefferson.application.br.util.StringUtils.formattedDate
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

object JDebug {
    private const val PREFERENCE_NAME = "Debug"

    @JvmStatic
    fun writeLogFile(context: Context, name : String? = formattedDate, error: String?) {
        if (error == null || error.isEmpty()) {
            return
        }
        try {
            val logFile = File(
                getInternalStorage(
                    context
                ) + "/.logs/" + name + ".txt"
            )
            logFile.parentFile?.mkdirs()
            val writer = FileWriter(logFile)
            writer.write(error)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @JvmStatic
    fun writeLog(context: Context, err: String?) {
        writeLogFile(context, null, err)
    }
    @JvmStatic
    fun writeLog(th: Throwable?, context: Context) {
        writeLogFile(context, null, getStackTrace(th))
    }

    @JvmStatic
    fun getStackTrace(throwable: Throwable?): String? {
        if (throwable == null) return null
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        var throwable = throwable
        while (throwable != null) {
            throwable.printStackTrace(printWriter)
            throwable = throwable.cause
        }
        val result = writer.toString()
        printWriter.close()
        return result
    }
    @JvmStatic
    fun toast(msg: String?) {
        if (msg == null) return
        toast(null, msg, Toast.LENGTH_SHORT)
    }
    @JvmStatic
    fun toast(context: Context?, msg: String, duration: Int) {
        if (context == null) return
        toast(context, null, msg, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun isDebugOn(context: Context?): Boolean {
        if (context == null) return false
        val prefs = MyPreferences.getSharedPreferences(context)
        return prefs.getBoolean(PREFERENCE_NAME, false)
    }
    @JvmStatic
    fun toast(context: Context?, tag: String?, msg: String, duration: Int) {
        if (context == null) return
        if (isDebugOn(context)) Handler(Looper.getMainLooper()).post {
            val text = if (tag == null) msg else "$tag: $msg"
            Toast.makeText(context, text, duration).show()
        }
    }
    @JvmStatic
    fun setDebug(on: Boolean, context: Context?) {
        if (context == null) return
        val prefs = MyPreferences.getSharedPreferences(context)
        prefs.edit().putBoolean(PREFERENCE_NAME, on).apply()
    }
}