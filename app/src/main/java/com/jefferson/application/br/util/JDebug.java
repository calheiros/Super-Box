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
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jefferson.application.br.App;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class JDebug {

    private static final String PREFERENCE_NAME = "Debug";

    public static void writeLogFile(Context context, String fname, String error) {
        if(error == null || error.isEmpty()) {
            return;
        }
        String name = fname == null ? StringUtils.getFormattedDate() : fname;
        try {
            File logFile = new File(Storage.getInternalStorage(context) + "/.logs/" + name + ".txt");
            logFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(logFile);
            writer.write(error);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeLog(Context context, String err) {
        writeLogFile(context, null, err);
    }

    public static void writeLog(Throwable th, Context context) {
        writeLogFile(context,null, getStackeTrace(th));
    }

    public static String getStackeTrace(Throwable thow) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        Throwable throwable = thow;
        while (throwable != null) {
            throwable.printStackTrace(printWriter);
            throwable = throwable.getCause();
        }
        String result = writer.toString();
        printWriter.close();
        return result;
    }

    public static void toast(String msg) {
        toast(null, msg, Toast.LENGTH_SHORT);
    }

    public static void toast(Context context, String msg, int duration) {
        toast(context,null, msg, Toast.LENGTH_SHORT);
    }

    public static boolean isDebugOn(@NonNull Context context) {
        SharedPreferences prefs = MyPreferences.getSharedPreferences(context);
        return prefs.getBoolean(PREFERENCE_NAME, false);
    }

    public static void toast(Context context, final String tag, final String msg, final int duration) {

        if (isDebugOn(context)) new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                String text = tag == null ? msg : tag + ": " + msg;
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    public static void setDebug(boolean on, @NonNull Context context) {
        SharedPreferences prefs = MyPreferences.getSharedPreferences(context);
        prefs.edit().putBoolean(PREFERENCE_NAME, on).apply();
    }
}
