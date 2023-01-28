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

package com.jefferson.application.br.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.fragment.LockFragment;
import com.jefferson.application.br.fragment.MainFragment;
import com.jefferson.application.br.fragment.SettingFragment;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.util.BlurUtils;
import com.jefferson.application.br.util.IntentUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.ServiceUtils;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.ThemeConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class MainActivity extends MyCompatActivity implements View.OnLayoutChangeListener, NavigationView.OnNavigationItemSelectedListener,
        ImportTask.Listener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_START_IN_PREFERENCES = "com.jefferson.application.action.START_IN_PREFERENCES";
    public static final int IMPORT_FROM_GALLERY_CODE = 43;
    public static final String ACTION_UPDATE = "com.jefferson.application.action.UPDATE_FRAGMENTS";
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 12;

    private static final int GET_SDCARD_URI_CODE = 98;
    private static final String ADS_ID = "ca-app-pub-3062666120925607/2904985113";
    public static int CURRENT_THEME;
    private static MainActivity instance;
    @SuppressLint("HandlerLeak")
    private final Handler getSdCardUriHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            Toast.makeText(MainActivity.this, getString(R.string.selecionar_sdcard), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, msg.what);
        }
    };
    public MainFragment mainFragment;
    public boolean calculatorStateEnabled;
    int oldMargin;
    private BottomNavigationView buttonNavigationView;
    private BroadcastReceiver receiver;
    private LockFragment lockFragment;
    private SettingFragment settingFragment;
    private Fragment oldFrag;
    private int position;
    private AdView adview;
    private boolean restarting;
    private AdView squareAdview;

    public static MainActivity getInstance() {
        return instance;
    }

    public static AdView createSquareAdview(Context context) {
        AdView squareAdview = new AdView(context);
        squareAdview.setAdSize(new AdSize(300, 250));
        squareAdview.setAdUnitId(ADS_ID);
        squareAdview.loadAd(new AdRequest.Builder().build());
        return squareAdview;
    }

    private void updateCurrentFragment() {
        if (mainFragment != null) {
            int pagerPosition = mainFragment.getPagerPosition();
            updateFragment(pagerPosition);
        }
    }

    public void setupToolbar(Toolbar toolbar, CharSequence title) {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    @Override
    public void onBeingStarted() {
    }

    @Override
    public void onUserInteraction() {
    }

    @Override
    public void onInterrupted() {
        updateCurrentFragment();
    }

    @Override
    public void onFinished() {
        updateCurrentFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        MobileAds.initialize(this, initializationStatus -> squareAdview = createSquareAdview(MainActivity.this));
        setContentView(R.layout.main_activity);
        CURRENT_THEME = ThemeConfig.getTheme(this);
        buttonNavigationView = findViewById(R.id.navigationView);
        buttonNavigationView.setOnNavigationItemSelectedListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        calculatorStateEnabled = isCalculatorComponentEnabled();
        if (savedInstanceState != null) {
            startActivity(new Intent(this, VerifyActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        if (!MyPreferences.userAcceptedAgreement()) {
            showUserAgreement();
        }
        createFragments();
        createAdView();
        createReceiver();
        configureBlur();
    }

    private void showUserAgreement() {
        View view = getLayoutInflater().inflate(R.layout.user_agreement_layout, null);
        TextView textView = view.findViewById(R.id.user_agreement_text);
        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.STYLE_ALERT);
        dialog.setContentView(view);

        try {
            String termOfService = getTextFromRaw(R.raw.terms_of_service);
            textView.setText(termOfService);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialog.show();
    }

    private String getTextFromRaw(int raw_id) throws IOException {
        Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(raw_id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        reader.close();
        inputStream.close();
        return stringBuilder.toString();
    }

    private void configureBlur() {
        BlurView blurView = findViewById(R.id.blurView);
        BlurUtils.setupWith(blurView, this, 13f);
        blurView.addOnLayoutChangeListener(this);
    }

    public AdView getSquareAdView() {
        if (squareAdview == null) {
            createSquareAdview(this);
        }
        return squareAdview;
    }

    public void removeFolder(int folderPosition, int pagerPosition) {
        if (mainFragment != null) {
            mainFragment.removeFolder(folderPosition, pagerPosition);
        }
    }

    private void createReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateAllFragments();
            }
        };
        registerReceiver(receiver, filter);
    }

    private void createAdView() {
        adview = findViewById(R.id.ad_view);
        adview.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void updateFragment(int position) {
        if (mainFragment != null) {
            mainFragment.updateFragment(position);
        }
    }

    public void updateAllFragments() {
        if (mainFragment != null) {
            mainFragment.updateAllFragments();
        }
    }

    public boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                return true;
            }
        }
        return false;
    }

    private void createFragments() {
        this.mainFragment = new MainFragment();
        this.lockFragment = new LockFragment();
        this.settingFragment = new SettingFragment();
        boolean startInSetting = ACTION_START_IN_PREFERENCES.equals(getIntent().getAction());

        if (startInSetting) {
            settingFragment.setCalculatorEnabled(getIntent().getBooleanExtra("calculator_enabled", false));
        } else {
            settingFragment.setCalculatorEnabled(calculatorStateEnabled);
        }
        changeFragment(startInSetting ? settingFragment : mainFragment);
        buttonNavigationView.getMenu().getItem(startInSetting ? 2 : 0).setChecked(true);
    }

    private boolean isCalculatorComponentEnabled() {
        return PackageManager.COMPONENT_ENABLED_STATE_ENABLED == getPackageManager().getComponentEnabledSetting(
                new ComponentName(this, "com.jefferson.application.br.CalculatorAlias"));
    }

    private void changeFragment(Fragment fragment) {
        if (fragment != getSupportFragmentManager().findFragmentById(R.id.fragment_container)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            if (oldFrag != null)
                transaction.detach(oldFrag);
            transaction.replace(R.id.fragment_container, fragment);
            transaction.attach(fragment);
            transaction.commit();
            oldFrag = fragment;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_item1) {
            changeFragment(mainFragment);
            return true;
        }
        if (id == R.id.main_item2) {
            changeFragment(lockFragment);
            return true;
        }
        if (id == R.id.item3) {
            changeFragment(settingFragment);
            return true;
        }
        if (id == R.id.item_4) {
            try {
                IntentUtils.shareApp(this);
            } catch (ActivityNotFoundException e) {
                activityNotFound();
            }
            return true;
        }
        if (id == R.id.item_5) {
            try {
                IntentUtils.reportBug(this);
            } catch (ActivityNotFoundException e) {
                activityNotFound();
            }
            return true;
        }
        return false;
    }

    public void activityNotFound() {
        Toast.makeText(this, "Nenhum app encontrado!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (ServiceUtils.isMyServiceRunning(AppLockService.class)) {
            startService(new Intent(this, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(instance, "Drawoverlay permision needed!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (resultCode == RESULT_OK) {

            if (requestCode == 69) {
                updateFragment(position);
                return;
            }

            if (requestCode == SettingFragment.CALCULATOR_CREATE_CODE_RESULT) {
                settingFragment.setCodeDescription(MyPreferences.getCalculatorCode());
                return;
            }

            if (requestCode == MainFragment.GET_FILE) {
                Uri uri = null;

                if (data != null) {
                    uri = data.getData();
                    Toast.makeText(this, uri.getPath(), Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (requestCode == GET_SDCARD_URI_CODE) {
                Uri uri = data.getData();

                if (Storage.checkIfSDCardRoot(uri)) {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Storage.storeExternalUri(uri.toString());
                }

            } else if (requestCode == IMPORT_FROM_GALLERY_CODE) {
                position = data.getIntExtra("position", -1);
                String type = data.getStringExtra("type");
                ArrayList<String> paths = data.getStringArrayListExtra("selection");

                Intent intent = new Intent(this, ImportMediaActivity.class);
                intent.putExtra(ImportMediaActivity.TYPE_KEY, type);
                intent.putExtra(ImportMediaActivity.MEDIA_LIST_KEY, paths);
                intent.putExtra(ImportMediaActivity.POSITION_KEY, position);
                this.startActivityForResult(intent, 69);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getSdCardUri(int code) {
        Toast.makeText(MainActivity.this, getString(R.string.selecionar_sdcard), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 54);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        SimpleDialog dialog = new SimpleDialog(this);
        dialog.setTitle(getString(R.string.confirmacao));
        dialog.setMessage(getString(R.string.quer_realmente_sair));
        dialog.setPositiveButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener() {

                    @Override
                    public boolean onClick(SimpleDialog dialog) {
                        finish();
                        return true;
                    }
                }
        );
        dialog.setNegativeButton(getString(R.string.nao), null).show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        adview.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adview.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        adview.destroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if (!restarting) {
            instance = null;
            boolean enabled = settingFragment.isCalculatorEnabledInSettings();

            if (enabled != isCalculatorComponentEnabled()) {
                settingFragment.disableLauncherActivity(enabled);
                settingFragment.setComponentEnabled(!enabled, "com.jefferson.application.br.CalculatorAlias");
                Toast.makeText(this, getString(R.string.aplicando_configuracoes), Toast.LENGTH_SHORT).show();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        if (oldMargin != v.getHeight()) {
            notifyBottomLayoutChanges(v);
        }
        oldMargin = v.getHeight();
    }

    private void notifyBottomLayoutChanges(View v) {
        if (mainFragment != null)
            mainFragment.notifyBottomLayoutChanged(v);

        if (lockFragment != null)
            lockFragment.notifyBottomLayoutChanged(v);
    }
}
