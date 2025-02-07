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
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class MyPreferences {
    public static final String KEY_BOOKMARK = "bookmark";
    public static final String KEY_FINGERPRINT = "fingerprint";
    private final static String KEY_ALLOW_SCREENSHOT = "allow_screenshot";
    private final static String KEY_CALCULATOR_CODE = "calculator_code";
    private static final String KEY_USER_AGREEMENT = "user_agreement";

    public static boolean getAllowScreenshot(@NonNull Context context) {
        return getSharedPreferences(context).getBoolean(KEY_ALLOW_SCREENSHOT, false);
    }

    public static void setAllowScreenshot(boolean allow,@NonNull Context context) {
        getSharedPreferencesEditor(context).putBoolean(KEY_ALLOW_SCREENSHOT, allow).commit();
    }

    public static void putCalculatorCode(String input, @NonNull Context context) {
        getSharedPreferencesEditor(context).putString(KEY_CALCULATOR_CODE, input).commit();
    }

    public static String getCalculatorCode(@NonNull Context context) {
        return getSharedPreferences(context).getString(KEY_CALCULATOR_CODE, "4321");
    }

    public static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(@NonNull Context context) {
        return getSharedPreferences(context).edit();
    }

    public static boolean userAcceptedAgreement(@NonNull Context context) {
        return getSharedPreferences(context).getBoolean(KEY_USER_AGREEMENT, false);
    }
}
