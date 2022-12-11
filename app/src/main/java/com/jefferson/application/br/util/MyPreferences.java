package com.jefferson.application.br.util;
import android.content.Context;
import android.content.SharedPreferences;
import com.jefferson.application.br.App;

public class MyPreferences {
    public static final String KEY_BOOKMARK = "bookmark";
    public static final String KEY_FINGERPRINT = "fingerprint";
    private final static String KEY_ALLOW_SCREENSHOT = "allow_screenshot";
    private final static String KEY_CALCULATOR_CODE = "calculator_code";

    public static boolean getAllowScreenshot() {
        return getSharedPreferences().getBoolean(KEY_ALLOW_SCREENSHOT, false);
    }

    public static void setAllowScreenshot(boolean allow) {
        getSharedPreferencesEditor().putBoolean(KEY_ALLOW_SCREENSHOT, allow).commit();
    }

    public static void putCalculatorCode(String input) {
        getSharedPreferencesEditor().putString(KEY_CALCULATOR_CODE, input).commit();
    }

    public static String getCalculatorCode() {
       return getSharedPreferences().getString(KEY_CALCULATOR_CODE, "4321");
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    } 

    public static SharedPreferences getSharedPreferences() {
        Context context = App.getAppContext();
        return getSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }
}
