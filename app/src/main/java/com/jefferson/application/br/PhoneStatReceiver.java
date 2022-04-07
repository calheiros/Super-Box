package com.jefferson.application.br;

import android.content.*;
import android.preference.*;
import android.util.*;
import com.jefferson.application.br.activity.*;

public class PhoneStatReceiver extends BroadcastReceiver {
	
    SharedPreferences sharedPrefs;
    
    @Override
	public void onReceive(Context context, Intent intent) {
        
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) { 
			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			String numberToLauncher=sharedPrefs.getString("secret_code", "#4321");
			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);        

			Log.i("call OUT", phoneNumber); 
			
            if (phoneNumber.equals(numberToLauncher)) {
				Intent in = new Intent(context, VerifyActivity.class);
                in.setAction(App.ACTION_OPEN_FROM_DIALER);
				in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(in);
				setResultData(null);
			}
		}
	}
}
