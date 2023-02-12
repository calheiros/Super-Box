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

import java.io.*
import java.nio.channels.FileChannel
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class FileTransfer {
    private var running = true
    var transferredKilobytes = 0.0
        private set

    fun increment(length: Double) {
        transferredKilobytes += length
    }

    fun transferStream(inputStream: InputStream, outputStream: OutputStream): String {
        try {
            val bArr = ByteArray(4096)
            while (running) {
                val read = inputStream.read(bArr)
                if (read == -1) {
                    outputStream.close()
                    inputStream.close()
                    return OK
                }
                outputStream.write(bArr, 0, read)
                transferredKilobytes += read.toDouble() / 1024.0
            }
        } catch (e: IOException) {
            return e.toString()
        }
        return "Interrupted"
    }

    fun cancel() {
        running = false
    }

    fun moveFile(source: File?, dest: File?): Boolean {
        val sourceChannel: FileChannel?
        val destChannel: FileChannel?
        try {
            sourceChannel = FileInputStream(source).channel
            destChannel = FileOutputStream(dest).channel
            if (sourceChannel != null && destChannel != null) {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
            } else return false
            sourceChannel.close()
            destChannel.close()
        } catch (e: IOException) {
            return false
        }
        return true
    }

    object Error {
        const val NO_LEFT_SPACE =
            "java.io.IOException: write failed: ENOSPC (No space left on device)"
    }

    class Encrypetion {
        fun encryptFile(res: File?, dest: File?): Boolean {
            try {
                val input = FileInputStream(res)
                val output = FileOutputStream(dest)
                val key: SecretKey = SecretKeySpec("0x200 & 0xff".toByteArray(), "ARC4")
                val cipher = Cipher.getInstance("ARC4")
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val outCipher = CipherOutputStream(output, cipher)
                val buffer = ByteArray(1024)
                var count: Int
                while (input.read(buffer).also { count = it } != -1) {
                    outCipher.write(buffer, 0, count)
                }
                outCipher.close()
            } catch (e: Exception) {
                return false
            }
            return true
        }
    }

    companion object {
        const val OK = "OK"
    }
}