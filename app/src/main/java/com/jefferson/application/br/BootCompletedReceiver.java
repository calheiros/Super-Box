package com.jefferson.application.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.util.ServiceUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    
	@Override
	public void onReceive(Context context, Intent arg1) {

		Log.w("boot_broadcast_poc", "starting service...");
		new AppsDatabase(context).clearUnlockedApps();
        ServiceUtils.startForegroundService(AppLockService.class);
		//context.startService(new Intent(context, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}

