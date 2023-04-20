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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PasswordManager {
    var file: File? = null
    fun setPasswordToFile(password: String) {
        try {
            file?.parentFile?.mkdirs()
            file?.createNewFile() ?: return
            val out: OutputStream = FileOutputStream(file)
            out.write(password.toByteArray())
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PIN_KEY = "pin_key"
        private const val PATTERN_KEY = "pattern"
        private const val KEY_PASSWORD_TEXT = "password_text"

        fun getPinCode(context: Context?): String {
            return MyPreferences.getSharedPreferences(context!!).getString(PIN_KEY, "") ?: ""
        }

        fun setPinCode(context: Context?, pin: String) {
            MyPreferences.getSharedPreferences(context!!).edit().putString(PIN_KEY, pin).apply()
        }

        fun setPatternCode(context: Context?, pattern: String) {
            MyPreferences.getSharedPreferences(context!!).edit().putString(PATTERN_KEY, pattern)
                .apply()
        }

        fun getPatternCode(context: Context?): String {
            return MyPreferences.getSharedPreferences(context!!).getString(PATTERN_KEY, "") ?: ""
        }

        fun getTextPassword(context: Context?): String {
            return MyPreferences.getSharedPreferences(context!!).getString(KEY_PASSWORD_TEXT, "") ?: ""
        }

        fun setTextPassword(context: Context?, password: String) {
            MyPreferences.getSharedPreferences(context!!).edit()
                .putString(KEY_PASSWORD_TEXT, password)
                .apply()
        }
    }
}