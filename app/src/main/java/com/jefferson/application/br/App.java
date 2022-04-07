package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.os.*;

import com.facebook.drawee.backends.pipeline.*;
import com.facebook.imagepipeline.core.*;
import com.facebook.imagepipeline.decoder.*;
import com.jefferson.application.br.activity.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.util.*;
import android.widget.*;
import android.util.Log;

public class App extends Application implements Thread.UncaughtExceptionHandler {

	public static final String EXCEPTION_FOUND = "_exception_found";
    public static final String EXCEPTION_LOG = "_exception_log";
    public static String TAG;
    private static App application;
	public static final String INTERSTICAL_ID = "ca-app-pub-3062666120925607/8580168530";
	public static final String INTERSTICAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712";
    public static final String ACTION_REPORT_CRASH = "com.jefferson.application.action.REPORT_CRASH";
    
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private Handler mHandler = new Handler();;
    private SharedPreferences mSharedPrefs;
    public static boolean localeConfigured = false;
    private ArrayList<MyCompatActivity> activities = new ArrayList<>();
	private boolean timing = false;
    public AppLockService appLockService;
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			if (isAnyNotRunning()) {
				destroyActivities();
			}
			App.this.timing = false;
        }
    };

    public static final String ACTION_OPEN_FROM_DIALER = "com.jefferson.application.action.ACTION_OPEN_FROM_DIALER";

    private boolean isAnyNotRunning() {
        for (MyCompatActivity activity : activities) {
            if (activity.isAlive()) return false;
        }
        return true;
    }

    public void remove(MyCompatActivity activity) {
        activities.remove(activity);
    }

    public boolean isTiming() {
        return timing;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable thow) {
        try {
            String error = JDebug.getStackeTrace(thow);
            JDebug.writeLog(error);
            startCrashActivity(error);
            destroyActivities();
            System.exit(2);
            //android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            JDebug.writeLog(e.getCause());
        }
        mDefaultExceptionHandler.uncaughtException(thread, thow);
    }

    private void startCrashActivity(String error) {
        Intent intent = new Intent(this, CrashActivity.class);
        intent.putExtra("message", error);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(am.ELAPSED_REALTIME_WAKEUP, 200, pending);
    }

    public void onCreate() {
        application = this;
        super.onCreate();
        mSharedPrefs = getSharedPreferences(EXCEPTION_LOG, MODE_PRIVATE);
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        //MobileAds.initialize(this, "ca-app-pub-3062666120925607~3930477089");
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
            .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
            .setResizeAndRotateEnabledForNetwork(true)
            .setDownsampleEnabled(true)
            .build();
        Fresco.initialize(this, config);
        startServiceNotRunning();
    }

    private void startServiceNotRunning() {
        if (!Utils.isMyServiceRunning(AppLockService.class)) {
            Intent intent = new Intent(this, AppLockService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    public static Context getAppContext() {
        return application;
    }

    public void putActivity(MyCompatActivity activity, String str) {
        this.activities.add(activity);
    }

    public static App getInstance() {
        return application;
    }

    public void destroyActivities() {
        for (Activity activity : activities) {
            if (!activity.isDestroyed()) {
                activity.finish();
            }
        }
        activities.clear();
    }

    public void stopCount() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void startCount(int millis) {
        long upTime = SystemClock.uptimeMillis();
        mHandler.postAtTime(mRunnable, upTime + millis);
        timing = true;
    }
    public void startCount() {
        startCount(60000);
        timing = true;
    }
}
