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

package com.jefferson.application.br.fragment;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;

import com.jefferson.application.br.LocaleManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.CreatePattern;
import com.jefferson.application.br.activity.DeveloperActivity;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.SettingAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.model.PreferenceItem;
import com.jefferson.application.br.util.ASCIIArt;
import com.jefferson.application.br.util.DialogUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.ThemeConfig;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

    public static final int CALCULATOR_CREATE_CODE_RESULT = 85;
    private static final int REQUEST_CODE = 109;
    public String[] storages;
    public String version;
    private SettingAdapter adapter;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mEdit;
    private int egg;
    private boolean calculatorEnabled = false;

    public void setCodeDescription(String calculatorCode) {
        adapter.onCalculatorCodeChanged(calculatorCode);
    }

    public boolean isCalculatorEnabledInSettings() {
        return calculatorEnabled;
    }

    public void setCalculatorEnabled(boolean enabled) {
        this.calculatorEnabled = enabled;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config, container, false);
        Toolbar mToolbar = view.findViewById(R.id.toolbar);
        storages = new String[]{getString(R.string.armaz_interno), getString(R.string.armaz_externo)};

        ((MainActivity) requireActivity()).setupToolbar(mToolbar, getString(R.string.configuracoes));
        // calculatorEnabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED == getComponentEnabledState(CalculatorActivity.class.getCanonicalName());
        mShared = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEdit = mShared.edit();
        ListView mListView = view.findViewById(R.id.list_config);
        mListView.setDivider(null);

        ArrayList<PreferenceItem> items = createItemsList();
        adapter = new SettingAdapter(items, this);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        return view;
    }

    private String getDialerCode() {
        return mShared.getString("secret_code", "#4321");
    }

    public ArrayList<PreferenceItem> createItemsList() {
        ArrayList<PreferenceItem> items = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            PreferenceItem item = new PreferenceItem();
            switch (i) {
                case 0:
                    item.title = getString(R.string.preferecias_gerais);
                    item.type = PreferenceItem.SECTION_TYPE;
                    break;
                case 1:
                    item.id = PreferenceItem.ID.PASSWORD;
                    item.icon_res_id = R.drawable.ic_key;
                    item.title = getString(R.string.mudar_senha);
                    item.type = PreferenceItem.ITEM_TYPE;
                    break;
                case 2:
                    item.id = PreferenceItem.ID.LANGUAGE;
                    item.icon_res_id = R.drawable.ic_language;
                    item.title = getString(R.string.idioma);
                    item.type = PreferenceItem.ITEM_TYPE;
                    item.description = getLanguageDisplay();
                    break;
                case 3:
                    item.id = PreferenceItem.ID.APP_THEME;
                    item.type = PreferenceItem.ITEM_TYPE;
                    item.icon_res_id = R.drawable.ic_palette;
                    item.title = getString(R.string.tema_applicativo);
                    item.description = ThemeConfig.getCurrentThemeName(requireContext());
                    break;
                case 4:
                    item.type = PreferenceItem.SECTION_TYPE;
                    item.title = getString(R.string.preferecias_avancadas);
                    break;
                case 5:
                    item.id = PreferenceItem.ID.STORAGE;
                    item.type = PreferenceItem.ITEM_TYPE;
                    item.icon_res_id = getStorageIcon();
                    item.title = getString(R.string.local_armazenamento);
                    item.description = getStorageName();
                    break;
                case 6:
                    item.id = PreferenceItem.ID.APP_ICON;
                    item.title = getString(R.string.disfarce_calculadora);
                    item.icon_res_id = R.drawable.ic_calculator_variant;
                    item.type = PreferenceItem.ITEM_SWITCH_TYPE;
                    item.description = getString(R.string.ocultar_descricao);
                    item.checked = calculatorEnabled;
                    break;
                case 7:
                    item.id = PreferenceItem.ID.SCREENSHOT;
                    item.icon_res_id = R.drawable.ic_cellphone_screenshot;
                    item.type = PreferenceItem.ITEM_SWITCH_TYPE;
                    item.title = getString(R.string.permitir_captura_tela);
                    item.description = getString(R.string.menos_seguro_se_habilitado);
                    item.checked = MyPreferences.getAllowScreenshot(requireContext());
                    break;
                case 9:
                    item.title = getString(R.string.preferecias_sobre);
                    item.type = PreferenceItem.SECTION_TYPE;
                    break;
                case 8:
                    SharedPreferences sharedPrefs = MyPreferences.getSharedPreferences(getActivity());
                    boolean checked = sharedPrefs.getBoolean(MyPreferences.KEY_FINGERPRINT, false);

                    item.id = PreferenceItem.ID.FINGERPRINT;
                    item.title = "Use fingerprint";
                    item.type = PreferenceItem.ITEM_SWITCH_TYPE;
                    item.icon_res_id = R.drawable.ic_fingerprint;
                    item.description = "Enable fingerprint unlock";
                    item.checked = checked;
                    break;
                case 10:
                    item.id = PreferenceItem.ID.ABOUT;
                    item.icon_res_id = R.drawable.ic_about;
                    item.title = getString(R.string.app_name);
                    item.type = PreferenceItem.ITEM_TYPE;
                    try {
                        item.description = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                    break;
            }
            items.add(item);
        }
        return items;
    }

    private void setAllEggsFound() {
        MyPreferences.getSharedPreferencesEditor(requireContext()).putBoolean("eggs_found", true).commit();
    }

    private boolean allEggsFound() {
        return MyPreferences.getSharedPreferences(getActivity()).getBoolean("eggs_found", false);
    }

    private void enterDebugActivity() {
        Intent intent = new Intent(getContext(), DeveloperActivity.class);
        requireActivity().startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        return position == 8;
    }

    @Override
    public void onItemClick(AdapterView adapter, View view, int position, long id) {
        PreferenceItem.ID itemId = SettingFragment.this.adapter.getItem(position).id;

        if (itemId == PreferenceItem.ID.PASSWORD) {
            Intent intent = new Intent(getContext(), CreatePattern.class);
            intent.setAction(CreatePattern.ENTER_RECREATE);
            requireActivity().startActivity(intent);
        } else if (itemId == PreferenceItem.ID.LANGUAGE) {
            showLanguageDialog();
        } else if (itemId == PreferenceItem.ID.APP_THEME) {
            showThemeDialog();
        } else if (itemId == PreferenceItem.ID.STORAGE) {
            showDialogStorage();
        } else if (itemId == PreferenceItem.ID.SCREENSHOT) {
            SwitchCompat mySwitch = view.findViewById(R.id.my_switch);
            boolean checked = !mySwitch.isChecked();
            MyPreferences.setAllowScreenshot(checked, requireContext());
            Window window = requireActivity().getWindow();
            int flags = WindowManager.LayoutParams.FLAG_SECURE;

            if (checked) {
                window.clearFlags(flags);
            } else {
                window.addFlags(flags);
            }
            setItemChecked(this.adapter, mySwitch, position, checked);
        } else if (itemId == PreferenceItem.ID.DIALER_CODE) {
            changeCodeDialog();
        } else if (itemId == PreferenceItem.ID.ABOUT) {
            showAbout();
        } else if (itemId == PreferenceItem.ID.FINGERPRINT) {
            SwitchCompat mySwitch = view.findViewById(R.id.my_switch);
            SharedPreferences sharedPrefs = MyPreferences.getSharedPreferences(getActivity());
            boolean checked = !mySwitch.isChecked();
            if (supportFingerprint()) {
                if (sharedPrefs.edit().putBoolean(MyPreferences.KEY_FINGERPRINT, checked).commit())
                    setItemChecked(this.adapter, mySwitch, position, checked);
            }
        }
    }

    private void setItemChecked(@NonNull SettingAdapter adapter, SwitchCompat mySwitch, int position, boolean checked) {
        PreferenceItem item = adapter.getItem(position);
        mySwitch.setChecked(checked);
        item.checked = checked;
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void showThemeDialog() {
        ArrayList<SimpleDialog.MenuItem> items = ThemeConfig.getMenuList(requireContext());
        SimpleDialog dialog = new SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU);
        dialog.setTitle(getString(R.string.escolha_tema)).setMenuItems(items, (parent, view, position, id) -> {
            dialog.cancel();
            int themeIndex = ThemeConfig.getThemeIndex(requireContext());
            int currentTheme = MainActivity.CURRENT_THEME;
            int newTheme = ThemeConfig.resolveTheme(getContext(), position);
            boolean needRefresh = newTheme != currentTheme;
            if (position != themeIndex) {
                ThemeConfig.setThemeIndex(position, getContext());
            }

            if (needRefresh) {
                refreshActivity();
                return;
            }
            //"must update description"
            updateItemDescription(PreferenceItem.ID.APP_THEME, items.get(position).name);
        });
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        openGithub();
    }

    @Override
    public void onPause() {
        super.onPause();
        egg = 0;
    }

    public void configureDialog(AlertDialog dialog) {
        DialogUtils.configureDialog(dialog);
    }

    public void openGithub() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/calheiros"));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException err) {
            Toast.makeText(getContext(), "Can't open URL", Toast.LENGTH_LONG).show();
        }
    }

    public void showDialogStorage() {
        int storagePosition = Storage.getStoragePosition(requireContext());
        ArrayList<SimpleDialog.MenuItem> options = new ArrayList<>();
        options.add(new SimpleDialog.MenuItem(getString(R.string.armaz_interno), R.drawable.ic_twotone_smartphone));
        if (Storage.getExternalStorage(requireContext()) != null)
            options.add(new SimpleDialog.MenuItem(getString(R.string.armaz_externo), R.drawable.ic_micro_sd));

        SimpleDialog dialog = new SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU);
        dialog.setTitle(getString(R.string.armazenamento));
        dialog.setMenuItems(options, (parent, view, position, id) -> {
            dialog.dismiss();
            if (position == storagePosition) {
                return;
            }
            Storage.setNewLocalStorage(position, requireContext());
            ((MainActivity) requireActivity()).mainFragment.reloadFragments();
            SimpleDialog.MenuItem item = options.get(position);
            updateItem(PreferenceItem.ID.STORAGE, item.name, item.icon);
        });
        dialog.show();
    }

    private void refreshActivity() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.setRestarting(true);
        final Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(MainActivity.ACTION_START_IN_PREFERENCES);
        intent.putExtra("calculator_enabled", calculatorEnabled);
        startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public void disableLauncherActivity(boolean disable) {
        requireActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), "com.jefferson.application.br.LuancherAlias"), disable ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public boolean supportFingerprint() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), "Nenhum hardware de biometria detectado! \nConsidere adquirir um smartphone melhor.", Toast.LENGTH_LONG).show();
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Intent enrollIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                }
                requireActivity().startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }
        return false;
    }

    public void setComponentEnabled(boolean enabled, String component) {
        requireActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), component), enabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public void changeCodeDialog() {
        final View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_call, null);
        final EditText editText = view.findViewById(R.id.editTextDialogUserInput);
        editText.append(getDialerCode());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), DialogUtils.getTheme(requireContext()));
        builder.setTitle("New code");
        builder.setPositiveButton(getString(R.string.salvar), (p1, p2) -> {
            String code = editText.getText().toString();

            if (code.length() < 3) {
                Toast.makeText(getContext(), "O Código não pode ser menor que 3 caractéres.", Toast.LENGTH_LONG).show();
            } else if (code.length() > 15) {
                Toast.makeText(getContext(), "O código não pode ter mais que 15 caractéres.", Toast.LENGTH_LONG).show();
            } else {
                mEdit.putString("secret_code", code).commit();
                updateItemDescription(PreferenceItem.ID.DIALER_CODE, code);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(getString(R.string.cancelar), null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        configureDialog(dialog);
        dialog.show();
    }

    private void updateItemDescription(PreferenceItem.ID dialerCode, String code) {
        updateItem(dialerCode, code, -1);
    }

    public int getComponentEnabledState(String componentName) {
        return requireActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(
                getContext(), componentName));
    }

    private String getStorageName() {
        return Storage.getStorageLocation(requireContext()).equals(Storage.INTERNAL) ? getString(R.string.armaz_interno) : getString(R.string.armaz_externo);
    }

    private int getStorageIcon() {
        return Storage.getStorageLocation(requireContext()).equals(Storage.INTERNAL) ? R.drawable.ic_twotone_smartphone : R.drawable.ic_micro_sd;
    }

    public void updateItem(PreferenceItem.ID id, String description, int icon) {
        for (int i = 0; i < adapter.getCount(); i++) {
            PreferenceItem item = adapter.getItem(i);
            if (item.id == id) {
                item.description = description;
                if (icon != -1) item.icon_res_id = icon;
                adapter.notifyDataSetChanged();
                //Toast.makeText(getContext(), "fount item to update! " + description, 1).show();
                break;
            }
        }
    }

    private String getLanguageDisplay() {
        String locale = MyPreferences.getSharedPreferences(getActivity()).getString(LocaleManager
                .LOCALE_KEY, LocaleManager.SYSTEM_LOCALE);

        switch (locale) {
            case LocaleManager.SYSTEM_LOCALE:
                return getString(R.string.padrao_do_sistema);
            case "en":
                return "English";
            case "pt":
                return "Portugu\u00eas";
            case "de":
                return "Deutsch";
            case "es":
                return "Espa\u00f1ol";
            case "ja":
                return "日本語";
        }
        return null;
    }

    private void showAbout() {
        SimpleDialog dialog = new SimpleDialog(requireActivity());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.credits_layout, null, false);
        TextView asciiTextView = view.findViewById(R.id.ascii_text_view);
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Raleway-Regular.ttf");

        asciiTextView.setTypeface(font);
        asciiTextView.setLetterSpacing(0);
        asciiTextView.setText(ASCIIArt.CHIKA_ART);
        view.findViewById(R.id.githubTextView).setOnClickListener(this);

        dialog.setContentView(view);
        dialog.setPositiveButton(android.R.string.ok, new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(@NonNull SimpleDialog dialog) {
                egg = 0;
                return true;
            }
        });

        if (!allEggsFound()) {
            dialog.setOnDismissListener(dialog1 -> {
                if (++egg == 7) {
                    setAllEggsFound();
                }
            });
        }

        if (allEggsFound()) {
            dialog.setNegativeButton("DEV", new SimpleDialog.OnDialogClickListener() {

                @Override
                public boolean onClick(SimpleDialog dialog) {
                    enterDebugActivity();
                    return true;
                }
            });
        }
        dialog.show();
    }

    private void showWarning() {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_check_box_view, null);
        final CheckBox mCheckBox = view.findViewById(R.id.dialogcheckbox);
        new AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.information)).setIcon(
                R.drawable.ic_information).setMessage(String.format("Vc pode abriar a aplicativo efetuando uma chamanda para o código %s", getDialerCode()))
                .setPositiveButton("fechar", null).setView(view).show().setOnDismissListener(dInterface -> {
                    if (mCheckBox.isChecked()) {
                        mEdit.putBoolean("dont_show_info_on_hidden", true);
                    }
                });
    }

    private void showLanguageDialog() {
        List<SimpleDialog.MenuItem> items = getLanguageMenuItems();
        SimpleDialog dialog = new SimpleDialog(requireActivity(), SimpleDialog.STYLE_MENU);
        dialog.setTitle(R.string.escolha_idioma);
        dialog.setMenuItems(items, new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                String locale;
                switch (position) {
                    case 1:
                        locale = "en";
                        break;
                    case 2:
                        locale = "es";
                        break;
                    case 3:
                        locale = "de";
                        break;
                    case 4:
                        locale = "pt";
                        break;
                    case 5:
                        locale = "ja";
                        break;
                    default:
                        locale = LocaleManager.SYSTEM_LOCALE;
                        break;
                }

                if (!locale.equals(MyPreferences.getSharedPreferences(requireContext()).getString(
                        LocaleManager.LOCALE_KEY, LocaleManager.SYSTEM_LOCALE))) {
                    LocaleManager.setNewLocale(getContext(), locale);
                    refreshActivity();
                }
            }
        });
        dialog.show();
    }

    private List<SimpleDialog.MenuItem> getLanguageMenuItems() {
        final String[] languages = {getString(R.string.padrao_do_sistema), "English", "Español", "Deutsch", "Português (Brasil)", "日本語"};
        int[] flags = new int[]{R.drawable.ic_auto_fix, R.drawable.flag_us, R.drawable.flag_es, R.drawable.flag_de, R.drawable.flag_br, R.drawable.flag_jp};
        List<SimpleDialog.MenuItem> items = new ArrayList<>();

        for (int i = 0; i < languages.length; i++) {
            items.add(new SimpleDialog.MenuItem(languages[i], flags[i], i == 0));
        }
        return  items;
    }
}
