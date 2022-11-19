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
