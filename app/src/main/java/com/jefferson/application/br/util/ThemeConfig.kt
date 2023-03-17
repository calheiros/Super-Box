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
package com.jefferson.application.br.util

import android.content.Context
import android.content.res.Configuration
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.app.SimpleDialog

object ThemeConfig {
    @JvmStatic
    fun getDialogTheme(context: Context?): Int {
        val theme = MainActivity.currentTheme
        return if (theme == R.style.MainTheme) {
            R.style.CustomAlertDialog
        } else {
            R.style.CustomAlertDialogLight
        }
    }

    fun isDarkThemeOn(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun getTheme(context: Context): Int {
        val index = getThemeIndex(context)
        return resolveTheme(context, index)
    }

    fun resolveTheme(context: Context, index: Int): Int {
        return when (index) {
            0 -> if (isDarkThemeOn(context)) R.style.MainTheme else R.style.ThemeBlueLight
            2 -> R.style.ThemeBlueLight
            3 -> R.style.ThemeGreenLight
            4 -> R.style.Theme_RedDark
            else -> R.style.MainTheme
        }
    }

    fun getThemeIndex(context: Context?): Int {
        return MyPreferences.getSharedPreferences(context!!).getInt("app_theme", 1)
    }

    fun setThemeIndex(themePosition: Int, context: Context?): Boolean {
        return MyPreferences.getSharedPreferencesEditor(context!!)
            .putInt("app_theme", themePosition).commit()
    }

    fun getThemesOptions(context: Context): Array<String> {
        return arrayOf(
            context.getString(R.string.automatico),
            context.getString(R.string.escuro),
            context.getString(R.string.claro),
            context.getString(R.string.claro_verde),
            "Red (Dark)"
        )
    }

    fun getMenuList(context: Context): ArrayList<SimpleDialog.MenuItem> {
        val options = getThemesOptions(context)
        val menu = ArrayList<SimpleDialog.MenuItem>()
        val icons = intArrayOf(
            R.drawable.ic_auto_fix,
            R.drawable.ic_circle_purple,
            R.drawable.ic_circle_blue,
            R.drawable.ic_circle_green,
            R.drawable.ic_circle_red
        )
        for (i in options.indices) {
            menu.add(SimpleDialog.MenuItem(options[i], icons[i], i == 0))
        }
        return menu
    }

    fun getCurrentThemeName(context: Context): String {
        val i = getThemeIndex(context)
        return getThemesOptions(context)[i]
    }
}