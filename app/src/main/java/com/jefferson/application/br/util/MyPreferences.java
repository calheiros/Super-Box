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

}
