package com.jefferson.application.br.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jefferson.application.br.App;
import com.jefferson.application.br.LocaleManager;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.StringUtils;
import com.jefferson.application.br.util.ThemeConfig;

public class MyCompatActivity extends androidx.appcompat.app.AppCompatActivity {

    public static final int REQUEST_WRITE_READ_PERMSSION_CODE = 13;
    public static String KEY;
    private boolean allowQuit;
    private App app;
    private boolean initialized;
    private PowerManager pm;
    private boolean running;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public boolean isAlive() {
        return running;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.app.isCounting()) {
            app.stopCount();
        }

        allowQuit = false;
        running = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void startActivity(Intent intent) {
        this.allowQuit = true;
        super.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LocaleManager.updateResources(newBase, LocaleManager.getLanguage(App.getAppContext()));
        super.attachBaseContext(context);
    }

    @Override
    public void startActivityForResult(Intent intent, int i) {
        this.allowQuit = true;
        super.startActivityForResult(intent, i);
    }

    @Override
    public void finish() {
        this.allowQuit = true;
        super.finish();
    }

    public int getAttrColor(int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public boolean hasNavBar(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onApplyCustomTheme();
        super.onCreate(savedInstanceState);
        if (!MyPreferences.getAllowScreenshot())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        app = (App) getApplication();
        KEY = StringUtils.getRandomString(8);
        app.putActivity(this, KEY);
        initialized = true;
    }

    protected void onApplyCustomTheme() {
        setTheme(ThemeConfig.getTheme(this));
    }

    public boolean haveWriteReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();

        } else {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }

    }

    public void requestWriteReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_WRITE_READ_PERMSSION_CODE);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Intent intent = new Intent(Intent.ACTION_APPLICATION_PREFERENCES);
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_READ_PERMSSION_CODE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.initialized) {
            app.remove(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;

        if (!pm.isScreenOn()) {
            app.startCount(5000);
        } else if (!allowQuit) {
            app.startCount();
        }
    }

    public void transparentStatusAndNavigation() {

        Window window = getWindow();
        int systemUiScrim = Color.parseColor("#40000000"); // 25% black{;
        int systemUiVisibility = 0;
        // Use a dark scrim by default since light status is API 23+
        int statusBarColor = systemUiScrim;
        //  Use a dark scrim by default since light nav bar is API 27+
        int navigationBarColor = systemUiScrim;
        WindowManager.LayoutParams winParams = window.getAttributes();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemUiVisibility = systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            statusBarColor = Color.TRANSPARENT;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility = systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            navigationBarColor = Color.TRANSPARENT;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibility = systemUiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags | (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(statusBarColor);
            window.setNavigationBarColor(navigationBarColor);
        }

        window.setAttributes(winParams);
    }
}
