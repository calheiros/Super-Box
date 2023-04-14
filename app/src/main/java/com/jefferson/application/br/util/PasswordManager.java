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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PasswordManager {

    public File file;

    private static final String PIN_KEY = "pin_key";
    private static String PATTERN_KEY = "pattern";
    private static final String KEY_PASSWORD_TEXT = "password_text";
    
    public void setPasswordToFile(String password) {

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            out.write(password.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {

        }
    }
    
    public static String getPinCode(Context context) {
        return MyPreferences.getSharedPreferences(context).getString(PIN_KEY, "");
    }

    public static void setPinCode(Context context, String pin) {
        MyPreferences.getSharedPreferences(context).edit().putString(PIN_KEY, pin).apply();
    }

    public static void setPatternCode(Context context, String pattern) {
        MyPreferences.getSharedPreferences(context).edit().putString(PATTERN_KEY, pattern).apply();
    }
    
    public static String getPatternCode(Context context) {
        return MyPreferences.getSharedPreferences(context).getString(PATTERN_KEY, "");
    }

    public static String getTextPassword(Context context) {
       return MyPreferences.getSharedPreferences(context).getString(KEY_PASSWORD_TEXT, "");
    }
    public static void setTextPassword(Context context, String password) {
        MyPreferences.getSharedPreferences(context).edit()
                .putString(KEY_PASSWORD_TEXT, password)
                .apply();
    }
}
