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

package com.jefferson.application.br.service;

import android.annotation.SuppressLint;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.util.JDebug;
import android.util.Log;

@SuppressLint("OverrideAbstract")
public class NotificationListener extends NotificationListenerService {
   
    public static String TAG = NotificationListener.class.getSimpleName();
    
    @Override 
    public void onNotificationPosted(StatusBarNotification sbm) {
         if (!JDebug.isDebugOn(this)) {
             return;
         }
         String name = sbm.getPackageName();
         JDebug.toast("Received Notification: " + name);
         
        if (name.equals("com.whatsapp") || "com.aide.ui".equals(name)) {
             JDebug.toast(this,"Canceling Notification\nTAG: " + name, Toast.LENGTH_LONG);
             cancelAllNotifications();
         }
    } 
}
