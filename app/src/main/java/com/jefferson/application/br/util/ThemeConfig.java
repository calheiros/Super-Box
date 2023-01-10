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

package com.jefferson.application.br.util;

import android.content.Context;
import android.content.res.Configuration;

import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.app.SimpleDialog;

import java.util.ArrayList;

public class ThemeConfig {

    public static int getDialogTheme(Context context) {
        int theme = MainActivity.CURRENT_THEME;

        if (theme == R.style.MainTheme) {
            return R.style.CustomAlertDialog;
        } else {
            return R.style.CustomAlertDialogLight;
        }
    }

    public static boolean isDarkThemeOn(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getTheme(Context context) {
        int index = getThemeIndex();
        return resolveTheme(context, index);
    }
    
    public static int resolveTheme(Context context, int index) {
        switch (index) {
            case 0:
                return (isDarkThemeOn(context) ? R.style.MainTheme: R.style.ThemeBlueLight);
            case 2:
                return R.style.ThemeBlueLight;
            case 3:
                return R.style.ThemeGreenLight;
            default:
                return R.style.MainTheme;
        }
    }
    
    public static int getThemeIndex() {
        return MyPreferences.getSharedPreferences().getInt("app_theme", 1);
    }

    public static boolean setThemeIndex(int themePosition) {
       return MyPreferences.getSharedPreferencesEditor().putInt("app_theme", themePosition).commit();
    }

    public static String[] getThemesOptions(Context context) {
        return new String[] {
                context.getString(R.string.automatico),
                context.getString(R.string.escuro),
                context.getString(R.string.claro),
                context.getString(R.string.claro_verde)
        };
    }

    public static ArrayList<SimpleDialog.MenuItem> getMenuList(Context context) {
        String[] options = getThemesOptions(context);
        ArrayList<SimpleDialog.MenuItem> menu = new ArrayList<>();

        int[] icons = new int[] {
                R.drawable.ic_auto_fix,
                R.drawable.ic_circle_purple,
                R.drawable.ic_circle_blue,
                R.drawable.ic_circle_green
        };

        for (int i = 0; i < options.length; i++) {
            menu.add(new SimpleDialog.MenuItem(options[i], icons[i], i == 0));
        }

        return menu;
    }

    public static String getCurrentThemeName(Context context) {
        int i = getThemeIndex();
        return getThemesOptions(context)[i];
    }
}
