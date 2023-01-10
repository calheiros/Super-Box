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

package com.jefferson.application.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.jefferson.application.br.database.AppLockDatabase;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.util.ServiceUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    
	@Override
	public void onReceive(Context context, Intent arg1) {

		Log.w("boot_broadcast_poc", "starting service...");
		new AppLockDatabase(context).clearUnlockedApps();
        ServiceUtils.startForegroundService(AppLockService.class);
		//context.startService(new Intent(context, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}

