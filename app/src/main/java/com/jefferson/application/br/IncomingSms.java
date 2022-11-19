package com.jefferson.application.br;


import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.widget.*;

public class IncomingSms extends BroadcastReceiver
{
	// Get the object of SmsManager
	final SmsManager sms = SmsManager.getDefault();
	
	public void onReceive(Context context, Intent intent)
	{

		// Retrieves a map of extended data from the intent.
		final Bundle bundle = intent.getExtras();

		try
		{

			if (bundle != null)
			{

				final Object[] pdusObj = (Object[]) bundle.get("pdus");

				for (int i = 0; i < pdusObj.length; i++)
				{

					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
					String phoneNumber = currentMessage.getDisplayOriginatingAddress();

					String senderNum = phoneNumber;
					String message = currentMessage.getDisplayMessageBody();

					Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

					
					// Show Alert
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(context, 
				    						 "senderNum: "+ senderNum + ", message: " + message, duration);
					if (phoneNumber.equals("555"))
					{
						toast.show();
					abortBroadcast();
					}

				} // end for loop
			} // bundle is null

		}
		catch (Exception e)
		{
			Log.e("SmsReceiver", "Exception smsReceiver" + e);

		}
	}    
}

