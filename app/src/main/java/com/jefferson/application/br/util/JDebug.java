package com.jefferson.application.br.util;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.jefferson.application.br.App;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class JDebug {

    private static String PREFERENCE_NAME = "Debug";

    public static void writeLogFile(String fname, String error) {
        String name = fname == null ? StringUtils.getFormattedDate() : fname;
        try {
            File logFile = new File(Storage.getInternalStorage() + "/.logs/" + name + ".txt");
            logFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(logFile);
            writer.write(error);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeLog(String err) {
        writeLogFile(null, err);
    }

    public static void writeLog(Throwable th) {
        writeLogFile(null, getStackeTrace(th));
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

    public static void toast(String msg, int duration) {
        toast(null, msg, Toast.LENGTH_SHORT);
    }

    public static boolean isDebugOn() {
        SharedPreferences prefs = MyPreferences.getSharedPreferences();
        boolean debugOn = prefs.getBoolean(PREFERENCE_NAME, false);
        return debugOn;
    }

    public static void toast(final String tag, final String msg, final int duration) {

        if (isDebugOn())
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                                         @Override
                                                         public void run() {
                                                             String text = tag == null ? msg : tag + ": " + msg;
                                                             Toast.makeText(App.getAppContext(), text, duration).show();
                                                         }
                                                     }
            );
    }

    public static void setDebug(boolean on) {
        SharedPreferences prefs = MyPreferences.getSharedPreferences();
        prefs.edit().putBoolean(PREFERENCE_NAME, on).commit();
    }
}