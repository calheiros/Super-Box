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

package com.jefferson.application.br.util;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
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
import android.content.Intent;

public class ServiceUtils {

	public static String getTopActivityApplication() {
		String currentApp = "";
        
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			UsageStatsManager usm = (UsageStatsManager)App.getInstance().getSystemService(Context.USAGE_STATS_SERVICE);
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

	public static boolean isMyServiceRunning(Class<?> serviceClass) {
		for (ActivityManager.RunningServiceInfo service : ((ActivityManager)App.getInstance().getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isConnected(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

    public static String getForegroundPackage(UsageStatsManager usageStatsManager) {
        String packageName = null;
        final long INTERVAL = 1000 * 10;
        final long end = System.currentTimeMillis(); 
        final long begin = end - INTERVAL; 
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            switch (event.getEventType()) { 
                case UsageEvents.Event.MOVE_TO_FOREGROUND: 
                    packageName = event.getPackageName();
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(packageName)) {
                        packageName = null; 
                    }
            }
        } 
        return packageName;
    }
    
    public static void startForegroundService(Class<?> service) {
        Context context = App.getAppContext();
        Intent intent = new Intent(context, service);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
