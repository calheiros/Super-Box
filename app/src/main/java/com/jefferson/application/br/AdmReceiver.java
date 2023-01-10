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

import android.app.admin.*;
import android.content.*;
import android.util.*;
import android.widget.*;
import android.preference.*;
import com.jefferson.application.br.util.MyPreferences;

public class AdmReceiver extends DeviceAdminReceiver { 

	@Override  
	public void onEnabled(Context context, Intent intent) {      
		SharedPreferences config = MyPreferences.getSharedPreferences(context);
		SharedPreferences.Editor editor = config.edit();
		editor.putBoolean("ADMIN_ENABLED", true).commit();
	}
    
	@Override   
	public CharSequence onDisableRequested(Context context, Intent intent) {   
        DevicePolicyManager deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceManger.lockNow();
		return context.getString(R.string.admin_receiver_status_disable_warning);   
	}
    
	@Override  
	public void onDisabled(Context context, Intent intent) {     
		SharedPreferences config = MyPreferences.getSharedPreferences(context);
		SharedPreferences.Editor editor = config.edit();
		editor.putBoolean("ADMIN_ENABLED", false).putBoolean("Capture Enabled", false).commit();
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);

		SharedPreferences config = MyPreferences.getSharedPreferences(context);
		boolean enabled = config.getBoolean("Capture Enabled", false);
		DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        int failedAttempts = mgr.getCurrentFailedPasswordAttempts();
		int captureNumber = config.getInt("Tentativas", 2);

        if (failedAttempts >= captureNumber && enabled) {
			Intent in = new Intent(context, TakePhotoService.class);
			in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
			context.startService(in);
		}
	}

	@Override
	public void onPasswordSucceeded(Context ctxt, Intent intent) {
		String tag = "tag";
		Log.v(tag, "this massage from success");
	}
}

    
