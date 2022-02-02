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

    private static Context updateResources(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

		config.locale = locale;
		res.updateConfiguration(config, res.getDisplayMetrics());

        return context;
    }
}
