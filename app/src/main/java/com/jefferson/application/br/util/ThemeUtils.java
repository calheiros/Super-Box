package com.jefferson.application.br.util;

import com.jefferson.application.br.R;

public class ThemeUtils {
    
    public static final String[] THEME_LIST = new String[] {"Default", "Light", "Light Blue"};

    public static int getDialogTheme() {
        int theme = getThemeIndex();
        if (theme == 0) {
            return R.style.CustomAlertDialog;
        } else {
            return R.style.CustomAlertDialogLight;
        }
    }

    public static int getTheme() {
        int  position = getThemeIndex();
        switch (position) {
            case 0:
                return R.style.MainTheme;
            case 1:
                return R.style.LightTheme;
            case 2:
                return R.style.LightBlueTheme;
            default:
                return R.style.MainTheme;
        }
    }

    public static int getThemeIndex() {
        return MyPreferences.getSharedPreferences().getInt("app_theme", 0);
    }
    
    public static void setThemeIndex(int themePosition) {
        MyPreferences.getSharedPreferencesEditor().putInt("app_theme", themePosition).commit();
    }

    public static String[] getThemeList() {
        return THEME_LIST;
    }
}
