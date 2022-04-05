package com.jefferson.application.br.service;

import android.annotation.SuppressLint;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.jefferson.application.br.util.JDebug;
import android.widget.Toast;

@SuppressLint("OverrideAbstract")
public class NotificationListener extends NotificationListenerService {
    public static String TAG = NotificationListener.class.getSimpleName();
    @Override public void onNotificationPosted(StatusBarNotification sbm) { 
        /** * This condition will define how you will identify your test is running 
         * You can have an in memory variable regarding it, or persistant variable, 
         * Or you can use Settings to store current state. 
         * You can have your own approach */ 
        if(sbm.getPackageName().equals("com.whatsapp")) {
             JDebug.toast("Canceling Whatsapp Notification\nTAG: " + sbm.getTag(), Toast.LENGTH_LONG);
             cancelAllNotifications();
         }
    } 
}
