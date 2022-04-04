package com.jefferson.application.br;

import android.app.Notification;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import com.jefferson.application.br.adapter.AppsAdapter;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.receiver.KeyWatcher;
import com.jefferson.application.br.util.JDebug;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import com.jefferson.application.br.activity.VerifyActivity;

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

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("operação não implementada");
	}

	@Override
	public void onCreate() {
        startForeground();
		AppsAdapter.service = this;
		database = new AppsDatabase(this);
		appLockWindow = new AppLockWindow(getApplicationContext(), database);
		//mLockedApps = mDabase.getLockedApps();
		startService();

        mybroadcast = new ScreenOnOff();
		registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        mHomeWatcher = new KeyWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new KeyWatcher.OnHomePressedListener() {

                @Override
                public void onRecentsPressed() {
                    recentsPressed();
                }

                @Override public void onHomePressed() {
                    homePressed();
                }
            }
        );
        mHomeWatcher.startWatch();
		super.onCreate();
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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

    private void homePressed() {

        if (appLockWindow.isLocked()) {
            appLockWindow.unlock();
        }
    }

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
        mHomeWatcher.stopWatch();
		unregisterReceiver(mybroadcast);
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

            String ActivityOnTop = com.jefferson.application.br.util.Utils.getTopActivityApplication();

            if (!ActivityOnTop.equals(pActivity)) {

                pActivity = ActivityOnTop;

                if (database.getLockedPackages().contains(ActivityOnTop) && !database.isAppUnlocked(ActivityOnTop)) {

					if (appLockWindow.isLocked()) {
						appLockWindow.unlock();
					}

					appLockWindow.lockApp(ActivityOnTop);
                   /* Intent intent = new Intent(App.getAppContext(), VerifyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
                    startActivity(intent);*/
				} else {
                    /*
                     if (lockScreen.isLocked()) {
                     lockScreen.unlock();
                     allowRemove = false;
                     Debug.toast(ActivityOnTop, Toast.LENGTH_LONG);
                     }*/
				}
			}
		}
	}

    private void showMessage(String m) {
		Toast.makeText(this, m, Toast.LENGTH_LONG).show();
	}
}
