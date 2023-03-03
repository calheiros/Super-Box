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
package com.jefferson.application.br

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import com.jefferson.application.br.activity.CrashActivity
import com.jefferson.application.br.activity.MyCompatActivity
import com.jefferson.application.br.service.AppLockService
import com.jefferson.application.br.util.JDebug
import com.jefferson.application.br.util.ServiceUtils
import kotlin.system.exitProcess

class App : Application(), Thread.UncaughtExceptionHandler {
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private val activities = ArrayList<MyCompatActivity>()

    var isCounting = false
        private set
    var appLockService: AppLockService? = null

    private val mRunnable: Runnable = Runnable {
        if (isAnyNotRunning) {
            destroyActivities()
        }
        isCounting = false
    }
    private val isAnyNotRunning: Boolean
        get() {
            for (activity in activities) {
                if (activity.isAlive) return false
            }
            return true
        }

    fun remove(activity: MyCompatActivity) {
        activities.remove(activity)
    }

    override fun uncaughtException(thread: Thread, thow: Throwable) {
        try {
            val error = JDebug.getStackeTrace(thow)
            JDebug.writeLog(this,error)
            startCrashActivity(error)
            destroyActivities()
            exitProcess(-1)
            //android.os.Process.killProcess(android.os.Process.myPid());
        } catch (e: Exception) {
            JDebug.writeLog(e.cause, this)
        }
        defaultExceptionHandler?.uncaughtException(thread, thow)
    }

    private fun startCrashActivity(error: String) {
        val intent = Intent(this, CrashActivity::class.java)
        intent.putExtra("message", error)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        am[AlarmManager.ELAPSED_REALTIME_WAKEUP, 200] = pending
    }

    override fun onCreate() {
        super.onCreate()
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        try {
            startServiceNotRunning()
        } catch (e: Exception) {
            Toast.makeText(this, "error: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startServiceNotRunning() {
        if (ServiceUtils.isMyServiceRunning(AppLockService::class.java, applicationContext)) {
            val intent = Intent(applicationContext, AppLockService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "starting service...", Toast.LENGTH_SHORT).show()
        }
    }

    fun putActivity(activity: MyCompatActivity, str: String?) {
        activities.add(activity)
    }

    private fun destroyActivities() {
        for (activity in activities) {
            if (!activity.isDestroyed) {
                activity.finish()
            }
        }
        activities.clear()
    }

    fun stopCount() {
        mHandler.removeCallbacks(mRunnable)
    }

    fun startCount(millis: Int) {
        val upTime = SystemClock.uptimeMillis()
        mHandler.postAtTime(mRunnable, upTime + millis)
        isCounting = true
    }

    fun startCount() {
        startCount(60000)
        isCounting = true
    }

    companion object {
        const val TEST_ADS_ID = "ca-app-pub-3940256099942544/6300978111"
        var TAG: String? = null
            private set
        const val ACTION_REPORT_CRASH = "com.jefferson.application.action.REPORT_CRASH"
        var PERMISSION_RECEIVE_BUS_DATA = "com.jefferson.application.RECEIVE_UPDATED_DATA"
        var localeConfigured = false
        const val ACTION_OPEN_FROM_DIALER =
            "com.jefferson.application.action.ACTION_OPEN_FROM_DIALER"
        const val ACTION_APPLOCK_SERVICE_UPDATE_DATA = "com.jefferson.application.action.UPDATA"
        const val ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD =
            "com.jefferson.applicatiom.action.APPLOCK_SERVICE_UPDATE_PASSWORD"

    }
}