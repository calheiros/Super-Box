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

