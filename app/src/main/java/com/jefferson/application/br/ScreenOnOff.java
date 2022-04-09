package com.jefferson.application.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.service.AppLockService;

public class ScreenOnOff extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			new AppsDatabase(context).clearUnlockedApps();
			AppLockService.pActivity = null;
		}
    }
}
