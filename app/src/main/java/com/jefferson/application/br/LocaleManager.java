package com.jefferson.application.br;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import com.jefferson.application.br.util.MyPreferences;
import java.util.Locale;

public class LocaleManager {

    public static void configureLocale(Context c) {
        String language = getLanguage(c);
        if (language != null) {
            updateResources(c, language);
        }
    }

    public static void setNewLocale(Context c, String language) {
        persistLanguage(c, language);
        updateResources(c, language);
    }

    public static String getLanguage(Context c) { 
		SharedPreferences mSheredPreferences = MyPreferences.getSharedPreferences(c);
		return mSheredPreferences.getString("locale", Locale.getDefault().getLanguage());
	}

    private static void persistLanguage(Context c, String language) { 
        SharedPreferences mSharedPreferences = MyPreferences.getSharedPreferences(c);
		mSharedPreferences.edit().putString("locale", language).commit();
	}

    public static Context updateResources(Context context, String language) {
        Context newContext = context; 
        Locale locale = new Locale(language);
        Locale.setDefault(locale); 
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { 
            config.setLocale(locale);
            newContext = context.createConfigurationContext(config);
        } else {
            config.locale = locale; 
            resources.updateConfiguration(config, resources.getDisplayMetrics()); 
        } return newContext;
    }
}
