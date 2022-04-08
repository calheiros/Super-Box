package com.jefferson.application.br.receiver;

import android.content.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.util.*;

public class WifiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(ServiceUtils.isConnected(context)) {
			//App.app.requestNewAd();
		}
	}
}
