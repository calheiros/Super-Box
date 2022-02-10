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

public class App extends Application implements Thread.UncaughtExceptionHandler {

	public static final String EXCEPTION_FOUND = "_exception_found";
    public static final String EXCEPTION_LOG = "_exception_log";
    public static String TAG;
    private static App application;
	public static final String INTERSTICAL_ID = "ca-app-pub-3062666120925607/8580168530";
	public static final String INTERSTICAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712";

    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private Handler mHandler = new Handler();;
    private SharedPreferences mSharedPrefs;
    public static boolean localeConfigured = false;
    private ArrayList<MyCompatActivity> activities = new ArrayList<>();
	private boolean timing = false;
	private Runnable mRunnable = new Runnable(){

		@Override
		public void run() {
			if (isAnyNotRunning()) {
				destroyActivities();
			}
			App.this.timing = false;
        }
    };
    
    private boolean isAnyNotRunning() {
        for (MyCompatActivity activity : activities) {
            if (activity.isAlive()) return false;
        }
        return true;
    }
    
    public AppLockService appLockService;

    public void remove(MyCompatActivity p0) {
        // TODO: Implement this method
        activities.remove(p0);
    }

    public boolean isTiming() {
        // TODO: Implement this method
        return timing;
    }

    @Override
    public void uncaughtException(Thread Thre, Throwable Thow) {
        mSharedPrefs.edit().putBoolean(EXCEPTION_FOUND, true).commit();
        //startActivity(new Intent(this, MainActivity.class));
        writeLog(Thow);
        mDefaultExceptionHandler.uncaughtException(Thre, Thow);
    }

    private void writeLog(Throwable throwable) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + 
                                 File.separator + "." + getApplicationInfo().packageName + File.separator + "logs", "log.txt");
            file.getParentFile().mkdirs();
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            FileWriter output = new FileWriter(file);

            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement trace = stackTrace[i];
                output.append(trace.toString());
            }
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {} catch (IOException e) {}
  
    }

    public void onCreate() {

        this.application = this;
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
        //createInterstitial();
    }

    public static Context getAppContext() {
        return application;
    }

    public void putActivity(MyCompatActivity activity, String str) {
        this.activities.add(activity);
    }


    public void finishActivities() {

        for (Activity activity : this.activities) {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                activity.finishAndRemoveTask();
            } else {
                activity.finish();
            }
        }
        activities.clear();
    }
    
    public static App getInstance() {
        return application;
    }

    public void destroyActivities() {
        for (Activity activity : activities) {
            activity.finish();
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
