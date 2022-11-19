package com.jefferson.application.br;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.util.ServiceUtils;

public class ReceiverRestartService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        ServiceUtils.startForegroundService(AppLockService.class);
	}
}

