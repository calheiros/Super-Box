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
import java.util.Date;
import java.io.IOException;

public class JDebug {

    private static String PREFERENCE_NAME = "Debug";

    public static void writeLog(String error) {
        try {
            File logFile = new File(Storage.getInternalStorage() + "/.logs/test_log.txt");
            logFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(logFile);
            writer.write(error);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void writeLog(Throwable throwable) {
        writeLog(getStackeTrace(throwable));
    }

    public static String getLogName() {
        Date date = new Date();
        return date.toString() + ".txt";
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
            new Handler(Looper.getMainLooper()).post(new Runnable(){

                    @Override
                    public void run() {
                        String text = tag == null ? msg: tag + ": " + msg;
                        Toast.makeText(App.getAppContext(), text, duration).show();
                    }
                }
            );
    }

    public static void setDebug(boolean on) {
        SharedPreferences prefs =  MyPreferences.getSharedPreferences();
        prefs.edit().putBoolean(PREFERENCE_NAME, on).commit();
    }

    public static String varDump(Object var) {

        String result = "unknown";
        try {
            int i = var;
            return "Integer => " + i;
        } catch (ClassCastException e) {}

        try { 
            String i = (String)var;
            return "String => " + i;
        } catch (ClassCastException e) {}

        try {
            boolean i = (Boolean) var;
            return "Boolean => " + i;
        } catch (ClassCastException e) {}

        if (isLong(var)) {
            return "Long => " + var;
        }

        if (isDouble(var)) {
            return "Double => " + var;
        }
        try {
            Object[] i =(Object[]) var;
            return "Array Object => " + i.toString();
        } catch (ClassCastException e) {

        }
        return result;
    }

    public static boolean isDouble(Object var) {
        return var instanceof double;
    }

    public static boolean isLong(Object type) {
        return type instanceof long;

    }
}
