package com.jefferson.application.br;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import com.jefferson.application.br.database.AppLockDatabase;
import com.jefferson.application.br.util.JDebug;
import java.util.ArrayList;

public class AppLockAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static String TAG = AppLockAccessibilityService.class.getName();
	private String activityOnTop;

	AppLockDatabase database;
	private AppLockWindow appLockWindow;

	private ScreenOnOff mybroadcast;

    private String pActivity = "";

	@Override
	public void onCreate() {
		super.onCreate();

		database = new AppLockDatabase(this);
		appLockWindow = new AppLockWindow(this, database);
		mybroadcast = new ScreenOnOff();

		registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

	}
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        Log.d("ABC-", accessibilityEvent.getPackageName() + " -- " + accessibilityEvent.getClassName());
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) { 
            if (accessibilityEvent.getPackageName() != null && accessibilityEvent.getClassName() != null) {
                ComponentName componentName = new ComponentName( 
                    accessibilityEvent.getPackageName().toString(), 
                    accessibilityEvent.getClassName().toString()); 
                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null; 
                if (isActivity)  {
                    mangeActivity(accessibilityEvent.getPackageName());
                    Log.i("CurrentActivity", componentName.flattenToShortString());
                    //activityChanged(accessibilityEvent.getPackageName());
                }
            } 
        }

    }
    private void mangeActivity(CharSequence name) {
        activityOnTop = name.toString();

        if (!activityOnTop.equals(pActivity)) {

            pActivity = activityOnTop;

            if (database.getLockedPackages().contains(activityOnTop) && !appLockWindow.getPassedApp().equals(activityOnTop)) {

                if (appLockWindow.isLocked()) {
                    if (appLockWindow.getLockePackageName().equals(activityOnTop)) {
                        return;
                    } 
                    appLockWindow.unlock();
                }
                appLockWindow.lock(activityOnTop);

            } else {
                if (appLockWindow.isLocked()) {
                    Log.i(getClass().getSimpleName(), "UNLOCKED on: " + activityOnTop);
                    appLockWindow.unlock();
                    JDebug.toast(activityOnTop, Toast.LENGTH_LONG);
                }
                //}
            }
        }
    }

    private void activityChanged(CharSequence flattenToShortString) {
        activityOnTop = String.valueOf(flattenToShortString);
        ArrayList<String> lockedApps = database.getLockedPackages();

        if (!appLockWindow.getPassedApp().equals(activityOnTop) && lockedApps.contains(activityOnTop)) {
            if (appLockWindow.isLocked()) {
                String name = appLockWindow.getLockePackageName();
                if (!activityOnTop.equals(name)) {
                    appLockWindow.unlock();
                    appLockWindow.lock(activityOnTop);
                }
            } else {
                appLockWindow.lock(activityOnTop);
            }
        } else if (appLockWindow.isLocked()) {
            appLockWindow.unlock();
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) { 
        try {
            return getPackageManager().getActivityInfo(componentName, 0); 
        } catch (PackageManager.NameNotFoundException e) {
            return null; } 
    }

    @Override protected void onServiceConnected() { 
        super.onServiceConnected(); 
        //Configure these here for compatibility with API 13 and below. 
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED; 
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC; 
        if (Build.VERSION.SDK_INT >= 16) //Just in case this helps 
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS; 
        setServiceInfo(config); 
    }
    @Override
    public void onInterrupt() {
        Log.e(TAG , " interrupt");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mybroadcast);
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		JDebug.toast("key", Toast.LENGTH_LONG);
		return super.onKeyEvent(event);

	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			appLockWindow.refreshView();
	    }
		super.onConfigurationChanged(newConfig);
	}

}
