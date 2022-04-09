package com.jefferson.application.br;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.jefferson.application.br.service.AppLockService;

public class OnUpgradeReceiver extends BroadcastReceiver {
	
    @Override
	public void onReceive(final Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AppLockService.class);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
	}
}
