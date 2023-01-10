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
import android.telephony.*;
import android.util.*;

public class MySMSMonitor
extends BroadcastReceiver
{
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
    @Override 
    public void onReceive(Context context, Intent intent) 
    {
        if(intent!=null && 
		   intent.getAction()!=null && 
		   ACTION.compareToIgnoreCase(intent.getAction())==0)
        {
            Object[]pduArray= (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pduArray.length];

            for (int i = 0; i<pduArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu ((byte[])pduArray [i]); 
            }
            Log.d("MySMSMonitor","SMS Message Received.");
        }
    }
}

