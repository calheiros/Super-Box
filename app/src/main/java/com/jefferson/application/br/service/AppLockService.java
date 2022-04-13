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
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.AppLockWindow;
import com.jefferson.application.br.R;
import com.jefferson.application.br.ScreenOnOff;
import com.jefferson.application.br.adapter.AppLockAdapter;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.receiver.KeyWatcher;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.util.EncrytionUtil;
import com.jefferson.application.br.util.JDebug;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.SecretKey;

public class AppLockService extends Service {

	private static Timer timer = new Timer(); 
	public static String pActivity = null;
	public static final String ACTION_RESTART_SERVICE ="RestartBlockService";
	private AppLockWindow appLockWindow;
	private ScreenOnOff mybroadcast;
	private AppsDatabase database;
	public static Handler toastHandler;
	public static AppLockService self;
	public static boolean toast = false;
	public KeyWatcher mHomeWatcher;
    public ArrayList<String> lockedApps;
    private UsageStatsManager usageStats;
    private BroadcastReceiver dataBusReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("operação não implementada");
	}

	@Override
	public void onCreate() {
        startForeground();
		AppLockAdapter.service = this;
		database = new AppsDatabase(this);
		appLockWindow = new AppLockWindow(getApplicationContext(), database);
        usageStats = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        lockedApps = database.getLockedPackages();

		startService();

        mybroadcast = new ScreenOnOff();
		registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

		super.onCreate();
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD.equals(action)) {
                String key = intent.getExtras().getString("key");

                if (key != null && appLockWindow != null) {
                    appLockWindow.setPassword(key);
                }

            } else if (App.ACTION_APPLOCK_SERVICE_UPDATE_DATA.equals(action)) {
                lockedApps = database.getLockedPackages();
            }
        }
        JDebug.toast("stat command " + intent);
		return START_STICKY;
	}

  /*  private void createDataBusReceiver() {
        dataBusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                JDebug.toast(action);

                if (App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD.equals(action)) {
                    File token = new File(getCacheDir(), "token");
                    SecretKey key = EncrytionUtil.getStoredKey(token);
                    if (key == null) {
                        JDebug.toast("Can not read key");
                        return;
                    }
                    String encryptedPass = intent.getStringExtra("password");
                    String realPassword = EncrytionUtil.getDecryptedString(key, encryptedPass);

                    if (realPassword != null) {
                        JDebug.toast("DECRYPTED PASSWORD" + realPassword);
                        try {
                            Integer.parseInt(realPassword);
                        } catch ( NumberFormatException e) {
                            JDebug.toast(e.getMessage());
                        }
                        appLockWindow.setPassword(realPassword);
                    } 
                } else if (App.ACTION_APPLOCK_SERVICE_UPDATE_DATA.equals(action)) {
                    lockedApps = database.getLockedPackages();
                }
            }
        };
        registerReceiver(dataBusReceiver, new IntentFilter(App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD));
        registerReceiver(dataBusReceiver, new IntentFilter(App.ACTION_APPLOCK_SERVICE_UPDATE_DATA));
    }
*/
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
			appLockWindow.refreshView();
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
        if (mybroadcast != null) {
            unregisterReceiver(mybroadcast);
        }
		sendBroadcast(new Intent(ACTION_RESTART_SERVICE));
		super.onDestroy();
	}

    private void recentsPressed() {

        if (appLockWindow.isLocked()) {
            appLockWindow.unlock();
        }
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

					if (appLockWindow.isLocked()) {
						appLockWindow.unlock();
					}

					appLockWindow.lock(activityOnTop);
                    /*Intent intent = new Intent(App.getAppContext(), VerifyActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
                     startActivity(intent);*/
				} else {

                    if (appLockWindow.isLocked() && !activityOnTop.isEmpty()) {
                        appLockWindow.unlock();
                        JDebug.toast(activityOnTop, Toast.LENGTH_LONG);
                    }
				}
			}
		}
	}

    private boolean onWhitelist(String name) {
        return false;
    }
}