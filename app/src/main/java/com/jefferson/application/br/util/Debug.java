package com.jefferson.application.br.util;
import android.widget.Toast;
import com.jefferson.application.br.App;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.Looper;

public class Debug {

    private static String PREFERENCE_NAME = "Debug";

    public static void toast(String msg) {
        toast(null, msg, Toast.LENGTH_SHORT);
    }

    public static void toast(String msg, int duration) {
        toast(null, msg, Toast.LENGTH_SHORT);
    }

    public static boolean isDebugOn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        boolean debugOn = prefs.getBoolean(PREFERENCE_NAME, false);
        return debugOn;
    }

    public static void toast(final String tag, final String msg, final int duration) {

        if (isDebugOn())
            new Handler(Looper.getMainLooper()).post(new Runnable(){

                    @Override
                    public void run() {
                        String text = tag == null ? msg: tag + ": " + msg;
                        Toast.makeText(App.getAppContext(), text, duration).show();
                    }
                }
            );
    }

    public static void setDebug(boolean on) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        prefs.edit().putBoolean(PREFERENCE_NAME, on).commit();
    }
}
