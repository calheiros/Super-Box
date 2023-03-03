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

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.jefferson.application.br.AppLockWindow
import com.jefferson.application.br.database.AppLockDatabase
import com.jefferson.application.br.receiver.ScreenOnOff
import com.jefferson.application.br.util.JDebug

class AppLockAccessibilityService : AccessibilityService() {
    private lateinit var activityOnTop: String
    private lateinit var database: AppLockDatabase
    private lateinit var appLockWindow: AppLockWindow
    private lateinit var broadcast: ScreenOnOff
    private var pActivity = ""

    override fun onCreate() {
        super.onCreate()
        database = AppLockDatabase(this)
        appLockWindow = AppLockWindow(this, database)
        broadcast = ScreenOnOff()
        registerReceiver(broadcast, IntentFilter(Intent.ACTION_SCREEN_ON))
        registerReceiver(broadcast, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        Log.d(
            "ABC-",
            accessibilityEvent.packageName.toString() + " -- " + accessibilityEvent.className
        )
        if (accessibilityEvent.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (accessibilityEvent.packageName != null && accessibilityEvent.className != null) {
                val componentName = ComponentName(
                    accessibilityEvent.packageName.toString(),
                    accessibilityEvent.className.toString()
                )
                val activityInfo = tryGetActivity(componentName)
                val isActivity = activityInfo != null
                if (isActivity) {
                    mangeActivity(accessibilityEvent.packageName)
                    Log.i("CurrentActivity", componentName.flattenToShortString())
                    //activityChanged(accessibilityEvent.getPackageName());
                }
            }
        }
    }

    private fun mangeActivity(name: CharSequence) {
        activityOnTop = name.toString()
        if (activityOnTop != pActivity) {
            pActivity = activityOnTop
            if (database.lockedPackages.contains(activityOnTop) && appLockWindow.passedApp != activityOnTop) {
                if (appLockWindow.isLocked) {
                    if (appLockWindow.lockePackageName == activityOnTop) {
                        return
                    }
                    appLockWindow.unlock()
                }
                appLockWindow.lock(activityOnTop)
            } else {
                if (appLockWindow.isLocked) {
                    Log.i(javaClass.simpleName, "UNLOCKED on: $activityOnTop")
                    appLockWindow.unlock()
                    JDebug.toast(this, activityOnTop, Toast.LENGTH_LONG)
                }
                //}
            }
        }
    }

    private fun activityChanged(flattenToShortString: CharSequence) {
        activityOnTop = flattenToShortString.toString()
        val lockedApps = database.lockedPackages
        if (appLockWindow.passedApp != activityOnTop && lockedApps.contains(activityOnTop)) {
            if (appLockWindow.isLocked) {
                val name = appLockWindow.lockePackageName
                if (activityOnTop != name) {
                    appLockWindow.unlock()
                    appLockWindow.lock(activityOnTop)
                }
            } else {
                appLockWindow.lock(activityOnTop)
            }
        } else if (appLockWindow.isLocked) {
            appLockWindow.unlock()
        }
    }

    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        //Configure these here for compatibility with API 13 and below. 
        val config = AccessibilityServiceInfo()
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        if (Build.VERSION.SDK_INT >= 16) //Just in case this helps 
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = config
    }

    override fun onInterrupt() {
        Log.e(TAG, " interrupt")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        JDebug.toast(this,"key", Toast.LENGTH_LONG)
        return super.onKeyEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            appLockWindow.refreshView()
        }
        super.onConfigurationChanged(newConfig)
    }

    companion object {
        private val TAG = AppLockAccessibilityService::class.java.name
    }
}