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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.RequiresApi;

import com.jefferson.application.br.App;
import com.jefferson.application.br.AppLockWindow;
import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.AppLockAdapter;
import com.jefferson.application.br.database.AppLockDatabase;
import com.jefferson.application.br.receiver.KeyWatcher;
import com.jefferson.application.br.receiver.ScreenOnOff;
import com.jefferson.application.br.util.JDebug;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AppLockService extends Service {

    public static final String ACTION_RESTART_SERVICE = "RestartBlockService";
    public static String pActivity = null;
    public static Handler toastHandler;
    public static AppLockService self;
    public static boolean toast = false;
    private static final Timer timer = new Timer();
    public KeyWatcher mHomeWatcher;
    public ArrayList<String> lockedApps;
    private AppLockWindow lockWindow;
    private ScreenOnOff myBroadcast;
    private AppLockDatabase database;
    private UsageStatsManager usageStats;
    private BroadcastReceiver dataBusReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("operation not implemented!");
    }

    @Override
    public void onCreate() {
        startForeground();
        AppLockAdapter.service = this;
        database = new AppLockDatabase(this);
        lockWindow = new AppLockWindow(getApplicationContext(), database);
        usageStats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        lockedApps = database.getLockedPackages();

        startService();

        myBroadcast = new ScreenOnOff();
        registerReceiver(myBroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(myBroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD.equals(action)) {
                String key = intent.getExtras().getString("key");

                if (key != null && lockWindow != null) {
                    lockWindow.setPassword(key);
                }

            } else if (App.ACTION_APPLOCK_SERVICE_UPDATE_DATA.equals(action)) {
                lockedApps = database.getLockedPackages();
            }
        }
        JDebug.toast("stat command " + intent);
        return START_STICKY;
    }

    private void startForeground() {
        String ChannelId = "";
        Notification.Builder builder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelId = getNotificationChannelId("Super Box", "AppLock Service");
            builder = new Notification.Builder(this, ChannelId);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notification = builder.setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_super)
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(9999, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getNotificationChannelId(String id, String name) {
        NotificationChannel chan = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        chan.setLightColor(R.color.colorAccent);
        NotificationManager service = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return id;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lockWindow.refreshView();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        JDebug.toast("DESTROY CALLED!");

        if (dataBusReceiver != null) {
            unregisterReceiver(dataBusReceiver);
        }
        //mHomeWatcher.stopWatch();
        if (myBroadcast != null) {
            unregisterReceiver(myBroadcast);
        }
        sendBroadcast(new Intent(ACTION_RESTART_SERVICE));
        super.onDestroy();
    }

    private void startLauncher() {

        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            JDebug.toast(e.getMessage());
        }
    }

    private void startService() {
        toastHandler = new ToastHandler();
        timer.scheduleAtFixedRate(new MainTask(), 0, 400);
    }

    private boolean onWhitelist(String name) {
        return false;
    }

    public class MainTask extends TimerTask {

        public void run() {
            toastHandler.sendEmptyMessage(0);
        }
    }

    public class ToastHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            String activityOnTop = com.jefferson.application.br.util.ServiceUtils.getForegroundPackage(usageStats);

            if (activityOnTop == null) {
                return;
            }

            if (lockedApps == null) {
                lockedApps = database.getLockedPackages();
            }

            if (!activityOnTop.equals(pActivity)) {

                pActivity = activityOnTop;

                if (lockedApps.contains(activityOnTop) && !onWhitelist(activityOnTop)) {

                    if (lockWindow.isLocked()) {
                        lockWindow.unlock();
                    }

                    if (activityOnTop.equals("com.android.settings.Settings")) {
                        startLauncher();
                    }
                    lockWindow.lock(activityOnTop);
                } else {

                    if (lockWindow.isLocked() && !activityOnTop.isEmpty()) {
                        lockWindow.unlock();
                    }
                }
            }
        }
    }
}
