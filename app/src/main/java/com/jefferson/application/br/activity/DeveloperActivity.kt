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
package com.jefferson.application.br.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.jefferson.application.br.ContactsActivity
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.JDebug
import java.io.File

class DeveloperActivity : MyCompatActivity() {
    private var observer: FileObserver? = null
    private var wifi: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        setContentView(R.layout.developer_layout)
        val switchView = findViewById<Switch>(R.id.developerlayoutSwitch)
        switchView.isChecked = JDebug.isDebugOn()
        switchView.setOnCheckedChangeListener { compButton, checked ->
            JDebug.setDebug(checked)
            val msg = if (checked) "Debug mode: ENABLED!" else "Debug mode: DISABLED!"
            Toast.makeText(this@DeveloperActivity, msg, Toast.LENGTH_LONG).show()
        }
        fileObserver()
    }

    private fun toggleWifi(state: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) wifi!!.isWifiEnabled = true else {
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
            startActivityForResult(panelIntent, 1)
        }
    }

    @Throws(Exception::class)
    fun trowException(v: View?) {
        throw Exception("Test exception")
    }

    fun enableWifi(v: View?) {
        toggleWifi(true)
        Toast.makeText(this, "Wifi ON", Toast.LENGTH_LONG).show()
    }

    fun disableWifi(vi: View?) {
        toggleWifi(false)
        Toast.makeText(this, "Wifi OFF", Toast.LENGTH_LONG).show()
    }

    fun showAlertDialog(v: View?) {
        val simple = SimpleDialog(this)
        simple.setTitle(R.string.unicode_shrug)
        simple.setMessage("Mensagem de teste")
        simple.setPositiveButton("ok", null)
        simple.show()
    }

    fun showProgressDialog(v: View?) {
        val simple = SimpleDialog(this, SimpleDialog.STYLE_PROGRESS)
        simple.setTitle(R.string.unicode_shrug)
        simple.setMessage("Mensagem de teste")
        simple.setProgress(76)
        simple.setPositiveButton(getString(android.R.string.ok), null)
        simple.setNegativeButton(getString(android.R.string.cancel), null)
        simple.show()
    }

    fun openPin(v: View?) {
        val intent = Intent(this, PinActivity::class.java)
        startActivity(intent)
    }

    fun contacts(v: View?) {
        startActivity(Intent(this, ContactsActivity::class.java))
    }

    fun notification(v: View?) {
        val bypass = true
        if (bypass || !NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(
                    applicationContext.packageName
                )
        ) {
            //no have access
            val intent =
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS") //For API level 22+ you can directly use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(intent, NOTIFICATION_REQUEST_CODE)
        } else { //Your own logic 
            Log.d(TAG, "You have Notification Access")
            JDebug.toast("You have Notification Access")
        }
    }

    fun camera(v: View) {
        val intent = Intent(this, VerifyActivity::class.java)
        var bundle: Bundle? = null
        //
        bundle = ActivityOptions.makeScaleUpAnimation(
            v, 0, 0, v.width,  //
            v.height
        ).toBundle()
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.parseColor("#308cf8"))
        bundle = ActivityOptions.makeThumbnailScaleUpAnimation(v, bitmap, 0, 0).toBundle()
        startActivity(intent, bundle)
        //startActivity(new);
    }

    fun quit(v: View?) {
        val intent = Intent(this, ImportMediaActivity::class.java)
        startActivity(intent)
    }

    private fun fileObserver() {
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        observer =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MyObserver(file) else MyObserver(
                file.absolutePath
            )
        (observer as MyObserver).startWatching()
    }

    private inner class MyObserver : FileObserver {
        constructor(path: String?) : super(path)

        @RequiresApi(Build.VERSION_CODES.Q)
        constructor(file: File?) : super(file!!)

        override fun onEvent(event: Int, name: String?) {
            JDebug.toast(name)
        }
    }

    fun testThread(v: View?) {
        val dialog = SimpleDialog(this, SimpleDialog.STYLE_PROGRESS)
        dialog.setTitle("thread test")
        dialog.setMax(100)
        dialog.show()
        object : JTask() {
            override fun onException(e: Exception) {
                dialog.resetDialog()
                dialog.setTitle("Execeção ocorrida!")
                dialog.setMessage("Error caught! " + e.message)
                dialog.setPositiveButton("okay", null)
            }

            var x: Long = 0
            var progress = 0
            override fun workingThread() {
                progress = 0
                while (progress <= 100) {
                    try {
                        Thread.sleep(40)
                    } catch (e: InterruptedException) {
                        break
                    }
                    if (progress >= 56) {
                        throw RuntimeException("Exception test!")
                    }
                    sendUpdate(progress)
                    progress++
                }
            }

            override fun onUpdated(vararg args: Any) {
                dialog.setProgress(args[0] as Int)
            }

            override fun onInterrupted() {
                Toast.makeText(this@DeveloperActivity, "Interrupted!", Toast.LENGTH_SHORT).show()
            }

            override fun onBeingStarted() {
                Toast.makeText(this@DeveloperActivity, "STARTED!\nx = $x", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFinished() {
                dialog.setPositiveButton("close", null)
                Toast.makeText(this@DeveloperActivity, "FINISHED!\nx = $x", Toast.LENGTH_SHORT)
                    .show()
            }
        }.start()
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 2
        private const val TAG = "Notifaction"
    }
}