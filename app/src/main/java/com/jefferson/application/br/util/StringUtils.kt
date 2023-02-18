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

import android.util.ArrayMap
import android.util.Log
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object StringUtils {
    var Dictionary = "abcdefghijklmnopqrstuvwxyz01234567890123456789"
    private const val TAG = "StringUtils"
    fun replaceEach(text: String, operators: ArrayMap<Char?, Char?>): String {
        // Create a buffer sufficiently large that re-allocations are minimized.
        val builder = StringBuilder(text.length)
        for (element in text) {
            var c = element
            if (operators.containsKey(c)) {
                c = operators[c]!!
            }
            builder.append(c)
        }
        return builder.toString()
    }

    fun getRandomString(length: Int): String {
        var generate = String()
        val random = Random()
        for (i in 0 until length) {
            val pos = random.nextInt(Dictionary.length)
            generate += Dictionary[pos]
        }
        return generate
    }

    fun extractLinks(text: String?): Array<String> {
        val links: MutableList<String> = ArrayList()
        val m = Patterns.WEB_URL.matcher(text as CharSequence)
        while (m.find()) {
            val url = m.group()
            Log.d(TAG, "URL extracted: $url")
            links.add(url)
        }
        return links.toTypedArray()
    }

    @JvmStatic
    fun getFormattedVideoDuration(millis: String): String {
        var duration = 0
        try {
            duration = millis.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
            )
        var minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val hours = TimeUnit.MINUTES.toHours(minutes)
        minutes -= hours * 60
        //long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return if (hours > 0) String.format(
            "%d:%02d:%02d",
            hours,
            minutes,
            seconds
        ) else String.format("%d:%02d", minutes, seconds)
    }

    val formattedDate: String
        get() = getFormattedDate("dd-MM-yyyy_HH-mm-ss")

    fun getFormattedDate(format: String?): String {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        val simpleDate = SimpleDateFormat(format, Locale.getDefault())
        return simpleDate.format(now)
    }

    fun getFormattedFileSize(fileSize: Long): String {
        // Use StringBuilder to build the string instead of multiple string concatenations
        val sb = StringBuilder()
        if (fileSize < 1024) {
            // File size is in bytes
            sb.append(fileSize).append(" Bytes")
        } else if (fileSize < 1024 * 1024) {
            // File size is in kilobytes
            sb.append(fileSize / 1024).append(" Kilobytes")
        } else if (fileSize < 1024 * 1024 * 1024) {
            // File size is in megabytes
            sb.append(fileSize / (1024 * 1024)).append(" Megabytes")
        } else if (fileSize < 1024L * 1024 * 1024 * 1024) {
            // File size is in gigabytes
            sb.append(fileSize / (1024 * 1024 * 1024)).append(" Gigabytes")
        } else {
            // File size is in terabytes
            sb.append(fileSize / (1024L * 1024 * 1024 * 1024)).append(" Terabytes")
        }
        return sb.toString()
    }
}