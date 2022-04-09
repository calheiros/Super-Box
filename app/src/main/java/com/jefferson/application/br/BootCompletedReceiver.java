package com.jefferson.application.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.service.AppLockService;

public class BootCompletedReceiver extends BroadcastReceiver
 {
	@Override
	public void onReceive(Context context, Intent arg1) {
	
		Log.w("boot_broadcast_poc", "starting service...");
		new AppsDatabase(context).clearUnlockedApps();
		context.startService(new Intent(context, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	    
	}
}

