package com.jefferson.application.br;

import android.content.*;
import android.content.res.*;
import android.os.*;
import android.preference.*;
import java.util.*;
import android.widget.Toast;

public class LocaleManager {

    public static void configureLocale(Context c) {
        String language = getLanguage(c);
        if (language != null)
            updateResources(c, language);
    }

    public static void setNewLocale(Context c, String language) {
        persistLanguage(c, language);
        updateResources(c, language);
    }

    public static String getLanguage(Context c) { 
		SharedPreferences mSheredPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		return mSheredPreferences.getString("locale", null);
	}

    private static void persistLanguage(Context c, String language) { 
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		mSharedPreferences.edit().putString("locale", language).commit();
	}

    public static Context updateResources(Context context, String language) {
        Context newContext = context; Locale locale = new Locale(language);
        Locale.setDefault(locale); 
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { 
            config.setLocale(locale);
            newContext = context.createConfigurationContext(config);
        } else {
            config.locale = locale; resources.updateConfiguration(config, resources.getDisplayMetrics()); 
        } return newContext;
    }
}
