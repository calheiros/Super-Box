package com.jefferson.application.br.util;
import android.content.Context;
import android.content.SharedPreferences;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;

public class MyPreferences {

    public static void setAppTheme(int themeId) {
        getSharedPreferencesEditor().putInt("app_theme", themeId).commit();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() +"_preferences", context.MODE_PRIVATE);
        return prefs;
    } 
   
    public static SharedPreferences getSharedPreferences() {
        Context context = App.getAppContext();
        return getSharedPreferences(context);
    }
    
    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }
    
    public static int getAppTheme() {
        return getSharedPreferences().getInt("app_theme", R.style.MainTheme);
    }
    
    
}
