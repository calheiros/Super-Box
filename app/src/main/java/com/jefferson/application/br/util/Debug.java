package com.jefferson.application.br.util;
import android.widget.Toast;
import com.jefferson.application.br.App;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Debug {
    
    private static String PREFERENCE_NAME = "Debug";
    
    public static void msg(String msg) {
        if (isDebugOn())
            Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static boolean isDebugOn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        boolean debugOn = prefs.getBoolean(PREFERENCE_NAME, false);
        return debugOn;
    }
    
    public static void msg(String tag, String msg) {
        if (isDebugOn())
            Toast.makeText(App.getAppContext(), tag + ": " + msg, Toast.LENGTH_LONG).show();
    }
    
    public static void setDebug(boolean on) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        prefs.edit().putBoolean(PREFERENCE_NAME, on).commit();
    }
}
