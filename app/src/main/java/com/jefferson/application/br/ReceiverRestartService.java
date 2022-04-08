package com.jefferson.application.br;
import android.content.*;
import android.util.*;
import java.util.*;
import com.jefferson.application.br.util.ServiceUtils;

public class ReceiverRestartService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        if (ServiceUtils.isMyServiceRunning(AppLockService.class)) {
            ServiceUtils.startForegroundService(AppLockService.class);
        }
	}
}

