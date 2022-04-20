package com.jefferson.application.br.util;
import android.content.Context;
import android.content.SharedPreferences;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;

public class MyPreferences {

    private final static String CALCULATOR_CODE_KEY = "calculator_code";
    
    public static void putCalculatorCode(String input) {
        getSharedPreferencesEditor().putString(CALCULATOR_CODE_KEY, input).commit();
    }

    public static String getCalculatorCode() {
       return getSharedPreferences().getString(CALCULATOR_CODE_KEY, "4321");
    }

    public static int getThemePosition() {
        return getSharedPreferences().getInt("app_theme", 0);
    }

    public static void setThemePosition(int themePosition) {
        getSharedPreferencesEditor().putInt("app_theme", themePosition).commit();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", context.MODE_PRIVATE);
        return prefs;
    } 

    public static SharedPreferences getSharedPreferences() {
        Context context = App.getAppContext();
        return getSharedPreferences(context);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }

    public static int resolveThemeResId(int position){
        switch (position) {
            case 0:
                return R.style.MainTheme;
            case 1:
                return R.style.LightTheme;
            default:
                return R.style.MainTheme;
        }
    }
    
    public static int getThemeResId() {
        int position = getThemePosition();
        return resolveThemeResId(position);
    }
}
