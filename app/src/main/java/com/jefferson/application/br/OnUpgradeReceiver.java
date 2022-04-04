package com.jefferson.application.br;
import android.content.*;
import android.widget.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.util.*;
import android.os.Build;

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
