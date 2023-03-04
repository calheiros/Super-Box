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
package com.jefferson.application.br.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.*
import androidx.annotation.RequiresApi
import com.jefferson.application.br.App
import com.jefferson.application.br.AppLockWindow
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.AppLockAdapter
import com.jefferson.application.br.database.AppLockDatabase
import com.jefferson.application.br.receiver.KeyWatcher
import com.jefferson.application.br.receiver.ScreenOnOff
import com.jefferson.application.br.util.JDebug.toast
import com.jefferson.application.br.util.ServiceUtils
import java.util.*

class AppLockService : Service() {
    var mHomeWatcher: KeyWatcher? = null
    var lockedApps: ArrayList<String>? = null
    private var lockWindow: AppLockWindow? = null
    private var myBroadcast: ScreenOnOff? = null
    private var database: AppLockDatabase? = null
    private var usageStats: UsageStatsManager? = null
    private val dataBusReceiver: BroadcastReceiver? = null
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("operation not implemented!")
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate() {
        startForeground()
        database = AppLockDatabase(this)
        lockWindow = AppLockWindow(applicationContext, database!!)
        usageStats = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        lockedApps = database!!.lockedPackages
        startService()
        myBroadcast = ScreenOnOff()
        registerReceiver(myBroadcast, IntentFilter(Intent.ACTION_SCREEN_ON))
        registerReceiver(myBroadcast, IntentFilter(Intent.ACTION_SCREEN_OFF))
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD == action) {
            val key = intent.extras?.getString("key")
            if (key != null && lockWindow != null) {
                lockWindow?.setPassword(key)
            }
        } else if (App.ACTION_APPLOCK_SERVICE_UPDATE_DATA == action) {
            lockedApps = database!!.lockedPackages
        }
        return START_STICKY
    }

    private fun startForeground() {
        val channelId: String
        val builder: Notification.Builder?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = getNotificationChannelId("Super Box", "AppLock Service")
            builder = Notification.Builder(this, channelId)
        } else {
            builder = Notification.Builder(this)
        }
        val notification = builder.setContentTitle(resources.getString(R.string.app_name))
            .setTicker(resources.getString(R.string.app_name))
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_super)
            .setContentIntent(null)
            .setOngoing(true)
            .build()
        startForeground(9999, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getNotificationChannelId(id: String, name: String): String {
        val chan = NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        chan.lightColor = R.color.colorAccent
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return id
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lockWindow?.refreshView()
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        toast("DESTROY CALLED!")
        dataBusReceiver?.let { unregisterReceiver(it) }
        //mHomeWatcher.stopWatch();
        if (myBroadcast != null) {
            unregisterReceiver(myBroadcast)
        }
        sendBroadcast(Intent(ACTION_RESTART_SERVICE))
        super.onDestroy()
    }

    private fun startLauncher() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(e.message)
        }
    }

    private fun startService() {
        toastHandler = ToastHandler()
        timer.scheduleAtFixedRate(MainTask(), 0, 400)
    }

    private fun onWhitelist(name: String): Boolean {
        return false
    }

    inner class MainTask : TimerTask() {
        override fun run() {
            toastHandler!!.sendEmptyMessage(0)
        }
    }

    inner class ToastHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val activityOnTop = ServiceUtils.getForegroundPackage(usageStats)
                ?: return
            if (lockedApps == null) {
                lockedApps = database!!.lockedPackages
            }
            if (activityOnTop != pActivity) {
                pActivity = activityOnTop
                if (lockedApps!!.contains(activityOnTop) && !onWhitelist(activityOnTop)) {
                    if (lockWindow!!.isLocked) {
                        lockWindow!!.unlock()
                    }
                    if (activityOnTop == "com.android.settings.Settings") {
                        startLauncher()
                    }
                    lockWindow!!.lock(activityOnTop)
                } else {
                    if (lockWindow!!.isLocked && activityOnTop.isNotEmpty()) {
                        lockWindow!!.unlock()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_RESTART_SERVICE = "RestartBlockService"
        var pActivity: String? = null
        var toastHandler: Handler? = null
        var self: AppLockService? = null
        var toast = false
        private val timer = Timer()
    }
}