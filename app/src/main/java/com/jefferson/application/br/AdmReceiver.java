package com.jefferson.application.br;

import android.app.admin.*;
import android.content.*;
import android.util.*;
import android.widget.*;
import android.preference.*;

public class AdmReceiver extends DeviceAdminReceiver { 

	@Override  
	public void onEnabled(Context context, Intent intent) {      
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = config.edit();
		editor.putBoolean("ADMIN_ENABLED", true).commit();

	}   
	@Override   
	public CharSequence onDisableRequested(Context context, Intent intent) {   

		return context.getString(R.string.admin_receiver_status_disable_warning);   
	}   
	@Override  
	public void onDisabled(Context context, Intent intent) {     

		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = config.edit();
		editor.putBoolean("ADMIN_ENABLED", false).putBoolean("Capture Enabled", false).commit();


	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);

		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		boolean enabled = config.getBoolean("Capture Enabled", false);
		DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        int no = mgr.getCurrentFailedPasswordAttempts();
		int captureNumber = config.getInt("Tentativas", 2);

        if (no >= captureNumber && enabled) {
			Intent in = new Intent(context, Take_photo.class);
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

    
