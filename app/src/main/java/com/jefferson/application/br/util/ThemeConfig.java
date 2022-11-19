package com.jefferson.application.br.util;

import android.content.Context;
import android.content.res.Configuration;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;

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
                return (isDarkThemeOn(context) ? R.style.MainTheme: R.style.LightBlueTheme);
            case 1:
                return R.style.MainTheme;
            case 2:
                return R.style.LightBlueTheme;
            case 3:
                return R.style.LightGreenTheme;
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

    public static String[] getThemeList(Context context) {
        return new String[] {
            context.getString(R.string.automatico), 
            context.getString(R.string.escuro),
            context.getString(R.string.claro), 
            context.getString(R.string.claro_verde)
        };
    }
}
