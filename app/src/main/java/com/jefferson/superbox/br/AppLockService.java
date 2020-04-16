package com.jefferson.superbox.br;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.widget.*;
import com.jefferson.superbox.br.adapter.*;
import com.jefferson.superbox.br.database.*;
import java.util.*;
import com.jefferson.superbox.br.util.*;

public class AppLockService extends Service {

	private static Timer timer = new Timer(); 
	public static String pActivity = null;
	public static final String ACTION_RESTART_SERVICE ="RestartBlockService";
	private WindowLockApps lockScreen;
	private ScreenOnOff mybroadcast;
	private AppsDatabase mDabase;
	//public static ArrayList<String> mLockedApps = new ArrayList<>();
	public static Handler toastHandler;
	public static AppLockService self;
	public static boolean toast = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("operação não implementada");
	}

	@Override
	public void onCreate() {
		AppsAdapter.service = this;
		mDabase = new AppsDatabase(this);
		lockScreen = new WindowLockApps(getApplicationContext(), mDabase);
		//mLockedApps = mDabase.getLockedApps();
		startService();

        mybroadcast = new ScreenOnOff();
		registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			lockScreen.updateView();
	    }
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mybroadcast);
		sendBroadcast(new Intent(ACTION_RESTART_SERVICE));
		super.onDestroy();
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
	class ToastHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			String ActivityOnTop = com.jefferson.superbox.br.util.Utils.getTopActivityApplication();
			if(toast) Toast.makeText(AppLockService.this,"Is true!",1).show();
			if (!ActivityOnTop.equals(pActivity)) {
				pActivity = ActivityOnTop;
				if (mDabase.getLockedApps().contains(ActivityOnTop) && !mDabase.isAppUnlocked(ActivityOnTop)) {

					if (lockScreen.isLocked()) {
						lockScreen.unlock();
					}
					lockScreen.lockApp(ActivityOnTop);

				} else {
					if (lockScreen.isLocked()) {
						lockScreen.unlock();
					}
				}
			}
		}
	}
	private void showMessage(String m) {
		Toast.makeText(this, m, Toast.LENGTH_LONG).show();
	}
	public class Update extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			showMessage("Hello");
		}
	}
}
