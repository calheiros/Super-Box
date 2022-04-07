package com.jefferson.application.br.util;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import com.jefferson.application.br.App;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Utils {

	public static String getTopActivityApplication() {

		String currentApp = "NULL";
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			UsageStatsManager usm = (UsageStatsManager)App.getInstance().getSystemService("usagestats");
			long time = System.currentTimeMillis();
			List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000 * 1000, time);
			if (appList != null && appList.size() > 0) {
				SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
				for (UsageStats usageStats : appList) {
					mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
				}
				if (mySortedMap != null && !mySortedMap.isEmpty()) {
					currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
				}
			}
		} else {
			ActivityManager mActivityManager = (ActivityManager)App.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
			ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
			currentApp = ar.topActivity.getPackageName();
		}
		return currentApp;
	}

	public static boolean isMyServiceRunning(Class serviceClass) {
		for (ActivityManager.RunningServiceInfo service : ((ActivityManager)App.getInstance().getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
    public static String getRunningPackage() {
        String topPackageName = "";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { 
            UsageStatsManager mUsageStatsManager = (UsageStatsManager)App.getAppContext().getSystemService("usagestats");
            long time = System.currentTimeMillis();
            // We get usage stats for the last 1 second 
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            // Sort the stats by the last time used 
            if (stats != null) { 
                SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats); 
                } 
                
                if (!mySortedMap.isEmpty()) { 
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName(); 
                }
            }
        }
        return topPackageName;
    }
    
	public static boolean isConnected(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivity.getActiveNetworkInfo();

		return netInfo != null && netInfo.isConnected();
	}
    
    public static String formatMillisecunds(long milliseconds) {

        String formated = null;

        return formated;
    }
}
