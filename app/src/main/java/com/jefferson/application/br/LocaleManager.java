/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import com.jefferson.application.br.util.MyPreferences;
import java.util.Locale;

public class LocaleManager {
    
    public static final String SYSTEM_LOCALE = "system_locale";
    public static final String LOCALE_KEY = "locale";
    
    public static void configureLocale(Context c) {
        String language = getLanguage(c);
        if (language != null) {
            updateResources(c, language);
        }
    }

    public static void setNewLocale(Context c, String language) {
        if (persistLanguage(c, language)){
            configureLocale(c);
        }
    }

    public static String getLanguage(Context c) { 
		SharedPreferences sharedPreferences = MyPreferences.getSharedPreferences(c);
        String locale = sharedPreferences.getString(LOCALE_KEY, SYSTEM_LOCALE);
        
        if (SYSTEM_LOCALE.equals(locale)) {
            return getSystemLocale(c);
        } else {
            return locale;
        }
	}

    private static String getSystemLocale(Context c) {
        //return Locale.getDefault().getLanguage();
        Configuration config = c.getResources().getConfiguration();
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return config.locale.getLanguage(); 
        } else {
            return config.getLocales().get(0).getLanguage();
        }
       
    }

    private static boolean persistLanguage(Context c, String language) { 
        SharedPreferences mSharedPreferences = MyPreferences.getSharedPreferences(c);
		return mSharedPreferences.edit().putString(LOCALE_KEY, language).commit();
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
