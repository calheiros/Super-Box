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

import androidx.activity.result.ActivityResult
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.fragment.LockFragment
import com.jefferson.application.br.fragment.MainFragment
import com.jefferson.application.br.fragment.SettingFragment
import com.jefferson.application.br.service.AppLockService
import com.jefferson.application.br.task.ImportTask
import com.jefferson.application.br.util.*
import eightbitlab.com.blurview.BlurView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class MainActivity : MyCompatActivity(), OnLayoutChangeListener,
    NavigationView.OnNavigationItemSelectedListener, ImportTask.Listener,
    BottomNavigationView.OnNavigationItemSelectedListener {
    private val getSdCardUriHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            Toast.makeText(
                this@MainActivity, getString(R.string.selecionar_sdcard), Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, msg.what)
        }
    }

    lateinit var mainFragment: MainFragment

    @JvmField
    var calculatorStateEnabled = false
    var oldMargin = 0
    private lateinit var buttonNavigationView: BottomNavigationView
    private lateinit var receiver: BroadcastReceiver
    private lateinit var lockFragment: LockFragment
    private lateinit var settingFragment: SettingFragment

    private var oldFrag: Fragment? = null
    private var position = 0
    private lateinit var adview: AdView
    private var restarting = false
    private var squareAdview: AdView? = null
    private fun updateCurrentFragment() {
        val pagerPosition = mainFragment.pagerPosition
        updateFragment(pagerPosition)
    }

    fun setupToolbar(toolbar: Toolbar?, title: CharSequence?) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayShowHomeEnabled(false)
    }

    fun setRestarting(restarting: Boolean) {
        this.restarting = restarting
    }

    override fun onBeingStarted() {}
    override fun onUserInteraction() {}
    override fun onInterrupted() {
        updateCurrentFragment()
    }

    override fun onFinished() {
        updateCurrentFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        MobileAds.initialize(this) {
            squareAdview = createSquareAdview(this@MainActivity)
        }
        setContentView(R.layout.main_activity)
        CURRENT_THEME = ThemeConfig.getTheme(this)
        buttonNavigationView = findViewById(R.id.navigationView)
        buttonNavigationView.setOnNavigationItemSelectedListener(this)
        calculatorStateEnabled = isCalculatorComponentEnabled
        if (savedInstanceState != null) {
            startActivity(
                Intent(this, VerifyActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )
        }
        createFragments()
        createAdView()
        createReceiver()
        configureBlur()
    }

    private fun showUserAgreement() {
        val view = layoutInflater.inflate(R.layout.user_agreement_layout, null)
        val textView = view.findViewById<TextView>(R.id.user_agreement_text)
        val dialog = SimpleDialog(this, SimpleDialog.STYLE_ALERT)
        dialog.setContentView(view)
        try {
            val termOfService = getTextFromRaw(R.raw.terms_of_service)
            textView.text = termOfService
        } catch (e: IOException) {
            e.printStackTrace()
        }
        dialog.show()
    }

    @Throws(IOException::class)
    private fun getTextFromRaw(raw_id: Int): String {
        val resources = resources
        val inputStream = resources.openRawResource(raw_id)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
            stringBuilder.append("\n")
        }
        reader.close()
        inputStream.close()
        return stringBuilder.toString()
    }

    private fun configureBlur() {
        val blurView = findViewById<BlurView>(R.id.blurView)
        BlurUtils.setupWith(blurView, this, 13f)
        blurView.addOnLayoutChangeListener(this)
    }

    val squareAdView: AdView?
        get() {
            if (squareAdview == null) {
                createSquareAdview(this)
            }
            return squareAdview
        }

    fun removeFolder(folderPosition: Int, pagerPosition: Int) {
        mainFragment.removeFolder(folderPosition, pagerPosition)
    }

    private fun createReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_UPDATE)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateAllFragments()
            }
        }
        registerReceiver(receiver, filter)
    }

    private fun createAdView() {
        adview = findViewById(R.id.ad_view)
        adview.loadAd(AdRequest.Builder().build())
    }

    fun updateFragment(position: Int) {
        mainFragment.updateFragment(position)
    }

    fun updateAllFragments() {
        mainFragment.updateAllFragments()
    }

    fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                return true
            }
        }
        return false
    }

    private fun createFragments() {
        mainFragment = MainFragment()
        lockFragment = LockFragment(this)
        settingFragment = SettingFragment()
        val startInSetting = ACTION_START_IN_PREFERENCES == intent.action
        if (startInSetting) {
            settingFragment.setCalculatorEnabled(
                intent.getBooleanExtra(
                    "calculator_enabled", false
                )
            )
        } else {
            settingFragment.setCalculatorEnabled(calculatorStateEnabled)
        }
        changeFragment(if (startInSetting) settingFragment else mainFragment)
        buttonNavigationView.menu.getItem(if (startInSetting) 2 else 0).isChecked = true
    }

    private val isCalculatorComponentEnabled: Boolean
        get() = PackageManager.COMPONENT_ENABLED_STATE_ENABLED == packageManager.getComponentEnabledSetting(
            ComponentName(this, "com.jefferson.application.br.CalculatorAlias")
        )

    private fun changeFragment(fragment: Fragment?) {
        if (fragment !== supportFragmentManager.findFragmentById(R.id.fragment_container)) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            if (oldFrag != null) transaction.detach(oldFrag!!)
            transaction.replace(R.id.fragment_container, fragment!!)
            transaction.attach(fragment)
            transaction.commit()
            oldFrag = fragment
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_item1 -> {
                changeFragment(mainFragment)
            }
            R.id.main_item2 -> {
                changeFragment(lockFragment)
            }
            R.id.item3 -> {
                changeFragment(settingFragment)
            }
            R.id.item_4 -> {
                try {
                    IntentUtils.shareApp(this)
                } catch (e: ActivityNotFoundException) {
                    activityNotFound()
                }
            }
            R.id.item_5 -> {
                try {
                    IntentUtils.reportBug(this)
                } catch (e: ActivityNotFoundException) {
                    activityNotFound()
                }
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun activityNotFound() {
        Toast.makeText(this, "Nenhum app encontrado!", Toast.LENGTH_LONG).show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (ServiceUtils.isMyServiceRunning(AppLockService::class.java, this)) {
            startService(
                Intent(
                    this, AppLockService::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(instance, "Drawoverlay permision needed!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        if (resultCode == RESULT_OK) {

            if (requestCode == SettingFragment.CALCULATOR_CREATE_CODE_RESULT) {
                settingFragment.setCodeDescription(MyPreferences.getCalculatorCode(this))
                return
            }
            if (requestCode == MainFragment.GET_FILE) {
                val uri: Uri?
                if (data != null) {
                    uri = data.data
                    Toast.makeText(this, uri!!.path, Toast.LENGTH_LONG).show()
                }
                return
            }
            if (requestCode == GET_SDCARD_URI_CODE) {
                val uri = data?.data ?: return
                if (Storage.checkIfSDCardRoot(uri)) {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    Storage.storeExternalUri(uri.toString(), this)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getSdCardUri(code: Int) {
        Toast.makeText(this@MainActivity, getString(R.string.selecionar_sdcard), Toast.LENGTH_SHORT)
            .show()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 54)
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    private fun showExitDialog() {
        val dialog = SimpleDialog(this)
        dialog.setTitle(getString(R.string.confirmacao))
        dialog.setMessage(getString(R.string.quer_realmente_sair))
        dialog.setPositiveButton(getString(R.string.sim), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                finish()
                return true
            }
        })
        dialog.setNegativeButton(getString(R.string.nao), null).show()
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()
        adview.resume()
    }

    public override fun onPause() {
        super.onPause()
        adview.pause()
    }

    public override fun onStop() {
        super.onStop()
    }

    public override fun onDestroy() {
        adview.destroy()
        unregisterReceiver(receiver)

        if (!restarting) {
            instance = null
            val enabled = settingFragment.isCalculatorEnabledInSettings
            if (enabled != isCalculatorComponentEnabled) {
                settingFragment.disableLauncherActivity(enabled)
                settingFragment.setComponentEnabled(
                    !enabled, "com.jefferson.application.br.CalculatorAlias"
                )
                Toast.makeText(
                    this, getString(R.string.aplicando_configuracoes), Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onDestroy()
    }

    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (oldMargin != v.height) {
            notifyBottomLayoutChanges(v)
        }
        oldMargin = v.height
    }

    private fun notifyBottomLayoutChanges(v: View) {
        mainFragment.notifyBottomLayoutChanged(v)
        lockFragment.notifyBottomLayoutChanged(v)
    }

    companion object {
        const val ACTION_START_IN_PREFERENCES =
            "com.jefferson.application.action.START_IN_PREFERENCES"
        const val ACTION_UPDATE = "com.jefferson.application.action.UPDATE_FRAGMENTS"
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 12
        private const val GET_SDCARD_URI_CODE = 98
        private const val ADS_ID = "ca-app-pub-3062666120925607/2904985113"

        @JvmField
        var CURRENT_THEME = 0

        @JvmStatic
        var instance: MainActivity? = null
            private set

        fun createSquareAdview(context: Context?): AdView {
            val squareAdview = AdView(context!!)
            squareAdview.setAdSize(AdSize(300, 250))
            squareAdview.adUnitId = ADS_ID
            squareAdview.loadAd(AdRequest.Builder().build())
            return squareAdview
        }
    }
}