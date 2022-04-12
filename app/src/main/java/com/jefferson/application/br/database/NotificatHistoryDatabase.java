package com.jefferson.application.br.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class NotificatHistoryDatabase extends SQLiteOpenHelper {

    public static final String TAG = "NotificatHistoryDatabase";
    private final static String DATABASE_NAME = "NotificationDatabase";
    private final static int DATABASE_VERSION = 1;

    public NotificatHistoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
        
    @Override
    public void onCreate(SQLiteDatabase db) {
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
