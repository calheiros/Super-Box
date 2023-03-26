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
package com.jefferson.application.br.fragment

import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.jefferson.application.br.LocaleManager
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.CreatePattern
import com.jefferson.application.br.activity.DeveloperActivity
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.adapter.SettingAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.model.PreferenceItem
import com.jefferson.application.br.model.PreferenceItem.ID
import com.jefferson.application.br.util.*

class SettingFragment : Fragment(), OnItemClickListener, View.OnClickListener,
    OnItemLongClickListener {
    private var paddingBottom: Int = 0
    private lateinit var storages: Array<String>
    private var listView: ListView? = null

    private var adapter: SettingAdapter? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var egg = 0
    private var rootView: View? = null
    var isCalculatorEnabledInSettings = false
        private set

    fun setCodeDescription(calculatorCode: String?) {
        adapter!!.onCalculatorCodeChanged(calculatorCode)
    }

    fun setCalculatorEnabled(enabled: Boolean) {
        isCalculatorEnabledInSettings = enabled
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.settings_fragment_layout, container, false) as ViewGroup
            val toolbar = rootView?.findViewById<Toolbar>(R.id.toolbar)
            storages = arrayOf(getString(R.string.armaz_interno), getString(R.string.armaz_externo))
            (requireActivity() as MainActivity).setupToolbar(
                toolbar, getString(R.string.configuracoes)
            )
            sharedPreferences = MyPreferences.getSharedPreferences(context as Context)
            editor = sharedPreferences?.edit()
            listView = rootView?.findViewById(R.id.settings_list_view)
            adapter = SettingAdapter(itemsList, this)
            listView?.adapter = adapter
            listView?.divider = null
            listView?.onItemClickListener = this
            listView?.onItemLongClickListener = this

            if (paddingBottom > 0) {
                ViewUtils.setViewPaddingBottom(listView, paddingBottom)
            }
        }
        return rootView
    }

    private val dialerCode: String?
        get() = sharedPreferences?.getString("secret_code", "#4321")

    private val itemsList: ArrayList<PreferenceItem>
        get() {
            val items = ArrayList<PreferenceItem>()
            for (i in 0..10) {
                val item = PreferenceItem()
                when (i) {
                    0 -> {
                        item.title = getString(R.string.preferecias_gerais)
                        item.type = PreferenceItem.SECTION_TYPE
                    }
                    1 -> {
                        item.id = ID.PASSWORD
                        item.iconResId = R.drawable.ic_key
                        item.title = getString(R.string.mudar_senha)
                        item.type = PreferenceItem.ITEM_TYPE
                    }
                    2 -> {
                        item.id = ID.LANGUAGE
                        item.iconResId = R.drawable.ic_language
                        item.title = getString(R.string.idioma)
                        item.type = PreferenceItem.ITEM_TYPE
                        item.description = languageDisplay
                    }
                    3 -> {
                        item.id = ID.APP_THEME
                        item.type = PreferenceItem.ITEM_TYPE
                        item.iconResId = R.drawable.ic_palette
                        item.title = getString(R.string.tema_applicativo)
                        item.description = ThemeConfig.getCurrentThemeName(requireContext())
                    }
                    4 -> {
                        item.type = PreferenceItem.SECTION_TYPE
                        item.title = getString(R.string.preferecias_avancadas)
                    }
                    5 -> {
                        item.id = ID.STORAGE
                        item.type = PreferenceItem.ITEM_TYPE
                        item.iconResId = storageIcon
                        item.title = getString(R.string.local_armazenamento)
                        item.description = storageName
                    }
                    6 -> {
                        item.id = ID.APP_ICON
                        item.title = getString(R.string.disfarce_calculadora)
                        item.iconResId = R.drawable.ic_calculator_variant
                        item.type = PreferenceItem.ITEM_SWITCH_TYPE
                        item.description = getString(R.string.ocultar_descricao)
                        item.checked = isCalculatorEnabledInSettings
                    }
                    7 -> {
                        item.id = ID.SCREENSHOT
                        item.iconResId = R.drawable.ic_cellphone_screenshot
                        item.type = PreferenceItem.ITEM_SWITCH_TYPE
                        item.title = getString(R.string.permitir_captura_tela)
                        item.description = getString(R.string.menos_seguro_se_habilitado)
                        item.checked = MyPreferences.getAllowScreenshot(requireContext())
                    }
                    9 -> {
                        item.title = getString(R.string.preferecias_sobre)
                        item.type = PreferenceItem.SECTION_TYPE
                    }
                    8 -> {
                        val sharedPrefs = MyPreferences.getSharedPreferences((activity)!!)
                        val checked = sharedPrefs.getBoolean(MyPreferences.KEY_FINGERPRINT, false)
                        item.id = ID.FINGERPRINT
                        item.title = "Use fingerprint"
                        item.type = PreferenceItem.ITEM_SWITCH_TYPE
                        item.iconResId = R.drawable.ic_fingerprint
                        item.description = "Enable fingerprint unlock"
                        item.checked = checked
                    }
                    10 -> {
                        item.id = ID.ABOUT
                        item.iconResId = R.drawable.ic_about
                        item.title = getString(R.string.app_name)
                        item.type = PreferenceItem.ITEM_TYPE
                        item.description =
                            getPackageInfo(requireContext().packageName, 0).versionName
                    }
                }
                items.add(item)
            }
            return items
        }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageName: String, flags: Int): PackageInfo {
        val context = requireContext()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        else @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(packageName, flags)
    }

    private fun setAllEggsFound() {
        MyPreferences.getSharedPreferencesEditor(requireContext()).putBoolean("eggs_found", true)
            .commit()
    }

    private fun allEggsFound(): Boolean {
        return MyPreferences.getSharedPreferences((activity)!!).getBoolean("eggs_found", false)
    }

    private fun enterDebugActivity() {
        val intent = Intent(context, DeveloperActivity::class.java)
        requireActivity().startActivity(intent)
    }

    override fun onItemLongClick(
        adapterView: AdapterView<*>?, view: View, position: Int, id: Long
    ): Boolean {
        return position == 8
    }

    fun notifyBottomLayoutChanged(view: View) {
        paddingBottom = view.height
        ViewUtils.setViewPaddingBottom(listView, paddingBottom)
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View, position: Int, id: Long) {
        when (this@SettingFragment.adapter!!.getItem(position).id) {
            ID.PASSWORD -> {
                val intent = Intent(context, CreatePattern::class.java)
                intent.action = CreatePattern.ENTER_RECREATE
                requireActivity().startActivity(intent)
            }
            ID.LANGUAGE -> {
                showLanguageDialog()
            }
            ID.APP_THEME -> {
                showThemeDialog()
            }
            ID.STORAGE -> {
                showDialogStorage()
            }
            ID.SCREENSHOT -> {
                val mySwitch = view.findViewById<MaterialSwitch>(R.id.prefs_switch)
                val checked = !mySwitch.isChecked
                MyPreferences.setAllowScreenshot(checked, requireContext())
                val window = requireActivity().window
                val flags = WindowManager.LayoutParams.FLAG_SECURE
                if (checked) {
                    window.clearFlags(flags)
                } else {
                    window.addFlags(flags)
                }
                setItemChecked((this.adapter)!!, mySwitch, position, checked)
            }
            ID.DIALER_CODE -> {
                changeCodeDialog()
            }
            ID.ABOUT -> {
                showAbout()
            }
            ID.FINGERPRINT -> {
                val mySwitch = view.findViewById<MaterialSwitch>(R.id.prefs_switch)
                val sharedPrefs = MyPreferences.getSharedPreferences((activity)!!)
                val checked = !mySwitch.isChecked
                if (supportFingerprint()) {
                    if (sharedPrefs.edit().putBoolean(MyPreferences.KEY_FINGERPRINT, checked)
                            .commit()
                    ) setItemChecked(
                        (this.adapter)!!, mySwitch, position, checked
                    )
                }
            }
            else -> {}
        }
    }

    private fun setItemChecked(
        adapter: SettingAdapter, mySwitch: MaterialSwitch, position: Int, checked: Boolean
    ) {
        val item = adapter.getItem(position)
        mySwitch.isChecked = checked
        item.checked = checked
    }

    private fun showThemeDialog() {
        val items = ThemeConfig.getMenuList(requireContext())
        val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU)
        dialog.setTitle(getString(R.string.escolha_tema))
            .setMenuItems(items) { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                dialog.cancel()
                val themeIndex: Int = ThemeConfig.getThemeIndex(requireContext())
                val currentTheme: Int = MainActivity.currentTheme
                val newTheme: Int = ThemeConfig.resolveTheme(requireContext(), position)
                val needRefresh: Boolean = newTheme != currentTheme
                if (position != themeIndex) {
                    ThemeConfig.setTheme(position, context)
                }
                if (needRefresh) {
                    refreshActivity()
                    return@setMenuItems
                }
                //"must update description"
                updateItemDescription(ID.APP_THEME, items[position].name)
            }
        dialog.show()
    }

    override fun onClick(view: View) {
        openGithub()
    }

    override fun onPause() {
        super.onPause()
        egg = 0
    }

    private fun configureDialog(dialog: AlertDialog?) {
        DialogUtils.configureDialog(dialog)
    }

    private fun openGithub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/calheiros"))
        try {
            startActivity(intent)
        } catch (err: ActivityNotFoundException) {
            Toast.makeText(context, "Can't open URL", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDialogStorage() {
        val storagePosition = Storage.getStoragePosition(requireContext())
        val options = ArrayList<SimpleDialog.MenuItem>()
        options.add(
            SimpleDialog.MenuItem(
                getString(R.string.armaz_interno), R.drawable.ic_twotone_smartphone
            )
        )
        if (Storage.getExternalStorage(requireContext()) != null) options.add(
            SimpleDialog.MenuItem(
                getString(R.string.armaz_externo), R.drawable.ic_micro_sd
            )
        )
        val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU)
        dialog.setTitle(getString(R.string.armazenamento))
        dialog.setMenuItems(
            options
        ) { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            dialog.dismiss()
            if (position == storagePosition) {
                return@setMenuItems
            }
            Storage.setNewLocalStorage(position, requireContext())
            (requireActivity() as MainActivity).mainFragment.reloadFragments()
            val item: SimpleDialog.MenuItem = options.get(position)
            updateItem(ID.STORAGE, item.name, item.icon)
        }
        dialog.show()
    }

    private fun refreshActivity() {
        val activity = activity as MainActivity? ?: return
        activity.setRestarting(true)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = MainActivity.ACTION_START_IN_PREFERENCES
        intent.putExtra("calculator_enabled", isCalculatorEnabledInSettings)
        startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }

    fun disableLauncherActivity(disable: Boolean) {
        requireActivity().packageManager.setComponentEnabledSetting(
            ComponentName(
                (context)!!, "com.jefferson.application.br.LuancherAlias"
            ),
            if (disable) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun supportFingerprint(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    requireContext(),
                    "Nenhum hardware de biometria detectado! \nConsidere adquirir um smartphone melhor.",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e(
                "MY_APP_TAG", "Biometric features are currently unavailable."
            )
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                var enrollIntent: Intent? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    enrollIntent.putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                requireActivity().startActivityForResult(enrollIntent, REQUEST_CODE)
            }
        }
        return false
    }

    fun setComponentEnabled(enabled: Boolean, component: String?) {
        requireActivity().packageManager.setComponentEnabledSetting(
            ComponentName(
                (context)!!, (component)!!
            ),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun changeCodeDialog() {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_call, null)
        val editText = view.findViewById<EditText>(R.id.editTextDialogUserInput)
        editText.append(dialerCode)
        val builder = AlertDialog.Builder(requireContext(), DialogUtils.getTheme(requireContext()))
        builder.setTitle("New code")
        builder.setPositiveButton(getString(R.string.salvar)) { _: DialogInterface?, _: Int ->
            val code: String = editText.text.toString()
            if (code.length < 3) {
                Toast.makeText(
                    context, "O Código não pode ser menor que 3 caractéres.", Toast.LENGTH_LONG
                ).show()
            } else if (code.length > 15) {
                Toast.makeText(
                    context, "O código não pode ter mais que 15 caractéres.", Toast.LENGTH_LONG
                ).show()
            } else {
                editor!!.putString("secret_code", code).commit()
                updateItemDescription(ID.DIALER_CODE, code)
                adapter!!.notifyDataSetChanged()
            }
        }
        builder.setNegativeButton(getString(R.string.cancelar), null)
        builder.setView(view)
        val dialog = builder.create()
        configureDialog(dialog)
        dialog.show()
    }

    private fun updateItemDescription(dialerCode: ID, code: String) {
        updateItem(dialerCode, code, -1)
    }

    fun getComponentEnabledState(componentName: String?): Int {
        return requireActivity().packageManager.getComponentEnabledSetting(
            ComponentName(
                (context)!!, (componentName)!!
            )
        )
    }

    private val storageName: String
        get() = if ((Storage.getStorageLocation(requireContext()) == Storage.INTERNAL)) getString(R.string.armaz_interno) else getString(
            R.string.armaz_externo
        )

    private val storageIcon: Int
        get() = if ((Storage.getStorageLocation(requireContext()) == Storage.INTERNAL)) R.drawable.ic_twotone_smartphone else R.drawable.ic_micro_sd

    private fun updateItem(id: ID, description: String?, icon: Int) {
        for (i in 0 until adapter!!.count) {
            val item = adapter!!.getItem(i)
            if (item.id == id) {
                item.description = description
                if (icon != -1) item.iconResId = icon
                adapter!!.notifyDataSetChanged()
                //Toast.makeText(getContext(), "fount item to update! " + description, 1).show();
                break
            }
        }
    }

    private val languageDisplay: String?
        get() {
            val locale = MyPreferences.getSharedPreferences((activity)!!)
                .getString(LocaleManager.LOCALE_KEY, LocaleManager.SYSTEM_LOCALE)
            when (locale) {
                LocaleManager.SYSTEM_LOCALE -> return getString(R.string.padrao_do_sistema)
                "en" -> return "English"
                "pt" -> return "Portugu\u00eas"
                "de" -> return "Deutsch"
                "es" -> return "Espa\u00f1ol"
                "ja" -> return "日本語"
            }
            return null
        }

    private fun showAbout() {
        val dialog = SimpleDialog(requireActivity())
        val view = LayoutInflater.from(context).inflate(R.layout.credits_layout, null, false)
        val asciiTextView = view.findViewById<TextView>(R.id.ascii_text_view)
        val font = Typeface.createFromAsset(activity?.assets, "fonts/Raleway-Regular.ttf")
        asciiTextView.typeface = font
        asciiTextView.letterSpacing = 0f
        asciiTextView.text = ASCIIArt.CHIKA_ART
        view.findViewById<View>(R.id.githubTextView).setOnClickListener(this)
        dialog.setContentView(view)
        dialog.setPositiveButton(android.R.string.ok, object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                egg = 0
                return true
            }
        })
        if (!allEggsFound()) {
            dialog.setOnDismissListener {
                if (++egg == 7) {
                    setAllEggsFound()
                }
            }
        }
        if (allEggsFound()) {
            dialog.setNegativeButton("DEV", object : OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    enterDebugActivity()
                    return true
                }
            })
        }
        dialog.show()
    }

    private fun showWarning() {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_check_box_view, null)
        val mCheckBox = view.findViewById<CheckBox>(R.id.dialogcheckbox)
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.information)).setIcon(
            R.drawable.ic_information
        ).setMessage(
            String.format(
                "Vc pode abriar a aplicativo efetuando uma chamanda para o código %s", dialerCode
            )
        ).setPositiveButton("fechar", null).setView(view).show().setOnDismissListener {
            if (mCheckBox.isChecked) {
                editor?.putBoolean("dont_show_info_on_hidden", true)?.commit()
            }
        }
    }

    private fun showLanguageDialog() {
        val items = languageMenuItems
        val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU)
        dialog.setTitle(R.string.escolha_idioma)
        dialog.setMenuItems(items) { _, _, position, _ ->
            dialog.cancel()
            val locale: String = when (position) {
                1 -> "en"
                2 -> "es"
                3 -> "de"
                4 -> "pt"
                5 -> "ja"
                else -> LocaleManager.SYSTEM_LOCALE
            }
            if (locale != MyPreferences.getSharedPreferences(requireContext()).getString(
                    LocaleManager.LOCALE_KEY, LocaleManager.SYSTEM_LOCALE
                )
            ) {
                LocaleManager.setNewLocale(context, locale)
                refreshActivity()
            }
        }
        dialog.show()
    }

    private val languageMenuItems: List<SimpleDialog.MenuItem>
        get() {
            val languages = arrayOf(
                getString(R.string.padrao_do_sistema),
                "English",
                "Español",
                "Deutsch",
                "Português (Brasil)",
                "日本語"
            )
            val flags = intArrayOf(
                R.drawable.ic_auto_fix,
                R.drawable.flag_us,
                R.drawable.flag_es,
                R.drawable.flag_de,
                R.drawable.flag_br,
                R.drawable.flag_jp
            )
            val items: MutableList<SimpleDialog.MenuItem> = ArrayList()
            for (i in languages.indices) {
                items.add(SimpleDialog.MenuItem(languages[i], flags[i], i == 0))
            }
            return items
        }

    companion object {
        const val CALCULATOR_CREATE_CODE_RESULT = 85
        private const val REQUEST_CODE = 109
    }
}