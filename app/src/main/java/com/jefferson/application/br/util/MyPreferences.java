package com.jefferson.application.br.util;
import android.content.SharedPreferences;
import android.content.Context;
import com.jefferson.application.br.App;

public class MyPreferences {
   
    public static SharedPreferences getSharedPreferences() {
        Context context = App.getAppContext();
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() +"_preferences", context.MODE_PRIVATE);
        return prefs;
    }
    
    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }
}
