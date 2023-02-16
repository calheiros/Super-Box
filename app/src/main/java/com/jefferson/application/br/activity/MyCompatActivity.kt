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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jefferson.application.br.App
import com.jefferson.application.br.LocaleManager
import com.jefferson.application.br.util.MyPreferences
import com.jefferson.application.br.util.StringUtils
import com.jefferson.application.br.util.ThemeConfig

open class MyCompatActivity : AppCompatActivity() {
    private var allowQuit = false
    private var app: App? = null
    private var initialized = false
    private var pm: PowerManager? = null
    var isAlive = false
        private set

    override fun onResume() {
        super.onResume()
        if (app!!.isCounting) {
            app!!.stopCount()
        }
        allowQuit = false
        isAlive = true
    }

    override fun startActivity(intent: Intent) {
        allowQuit = true
        super.startActivity(intent)
    }

    override fun attachBaseContext(newBase: Context) {
        val context =
            LocaleManager.updateResources(newBase, LocaleManager.getLanguage(newBase))
        super.attachBaseContext(context)
    }

    override fun startActivityForResult(intent: Intent, i: Int) {
        allowQuit = true
        super.startActivityForResult(intent, i)
    }

    override fun finish() {
        allowQuit = true
        super.finish()
    }

    fun getAttrColor(resId: Int): Int {
        val typedValue = TypedValue()
        val theme = theme
        theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    fun hasNavBar(resources: Resources): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onApplyCustomTheme()
        super.onCreate(savedInstanceState)
        if (!MyPreferences.getAllowScreenshot(this)) window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        pm = getSystemService(POWER_SERVICE) as PowerManager
        app = application as App
        KEY = StringUtils.getRandomString(8)
        app!!.putActivity(this, KEY)
        initialized = true
    }

    protected open fun onApplyCustomTheme() {
        setTheme(ThemeConfig.getTheme(this))
    }

    fun haveWriteReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestWriteReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, REQUEST_WRITE_READ_PERMSSION_CODE)
            return
        }
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_READ_PERMSSION_CODE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (initialized) {
            app!!.remove(this)
        }
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
        if (!pm!!.isScreenOn) {
            app!!.startCount(5000)
        } else if (!allowQuit) {
            app!!.startCount()
        }
    }

    fun transparentStatusAndNavigation() {
        val window = window
        val systemUiScrim = Color.parseColor("#40000000") // 25% black{;
        var systemUiVisibility = 0
        // Use a dark scrim by default since light status is API 23+
        var statusBarColor = systemUiScrim
        //  Use a dark scrim by default since light nav bar is API 27+
        var navigationBarColor = systemUiScrim
        val winParams = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            navigationBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.decorView.systemUiVisibility = systemUiVisibility
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags =
                winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags =
                winParams.flags or (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navigationBarColor
        }
        window.attributes = winParams
    }

    companion object {
        const val REQUEST_WRITE_READ_PERMSSION_CODE = 13
        var KEY: String? = null
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val win = activity.window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }
}