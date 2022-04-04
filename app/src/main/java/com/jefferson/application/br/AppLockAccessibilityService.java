package com.jefferson.application.br;

import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.util.*;

public class AppLockAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static String TAG = AppLockAccessibilityService.class.getName();
	private String activityOnTop;
	public static String pActivity="";
	AppsDatabase appsDb;
	private AppLockWindow window;

	private ScreenOnOff mybroadcast;

	@Override
	public void onCreate() {
		super.onCreate();
		
		appsDb = new AppsDatabase(this);
		window = new AppLockWindow(this, appsDb);
		mybroadcast = new ScreenOnOff();

		registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));

	}
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
		
        activityOnTop = event.getPackageName().toString();
		
        if (activityOnTop.equals(Utils.getTopActivityApplication()) && !activityOnTop.equals(pActivity)) {
			pActivity = activityOnTop;
			
			if (appsDb.getLockedPackages().contains(activityOnTop) && !appsDb.isAppUnlocked(activityOnTop)) {
				if (window.isLocked()) {
					window.unlock();
				}
				window.lockApp(activityOnTop);
			} else {
				
				if (window.isLocked()) {
					JDebug.toast(event.getClassName().toString());
                    JDebug.toast(event.eventTypeToString(event.getEventType()));

                    
					window.unlock();
				}
			}
		}
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
		Toast.makeText(this, "key", Toast.LENGTH_LONG).show();
		return super.onKeyEvent(event);

	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			window.refreshView();
	    }
		super.onConfigurationChanged(newConfig);
	}
	
}
