package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import com.jefferson.application.br.R;

public class NotificationActivity extends MyCompatActivity {

    private String TAG = "NotificationActivity";
    private static final int NOTIFICATION_REQUEST_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);
        if (!NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()) .contains(getApplicationContext().getPackageName())) { 
            //We dont have access 
            Intent intent= new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"); //For API level 22+ you can directly use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
            startActivityForResult(intent, NOTIFICATION_REQUEST_CODE); 
        } else { //Your own logic 
            Log.d(TAG, "You have Notification Access"); 
            
        }
    }

}
