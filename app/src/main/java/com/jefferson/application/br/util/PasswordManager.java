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
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class PasswordManager {

    public File file;
    private SharedPreferences sharedPrefers;
    private static final String PIN_KEY = "pin_key";
    public static String PATTERN_KEY = "pattern";

    public PasswordManager() {
        
        this.file = new File(Storage.getInternalStorage(), ".SECRETY_KEY");
        this.sharedPrefers = MyPreferences.getSharedPreferences();
    }
    
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
    
    public String getPinCode() {
        return sharedPrefers.getString(PIN_KEY, "");
    }

    public boolean setPinCode(String pin) {
        return sharedPrefers.edit().putString(PIN_KEY, pin).commit();
    }

    public boolean setPassword(String password) {
        return sharedPrefers.edit().putString(PATTERN_KEY, password).commit();
    }
    
    public String getInternalPassword() {
        return sharedPrefers.getString(PATTERN_KEY, "");
    }
}
