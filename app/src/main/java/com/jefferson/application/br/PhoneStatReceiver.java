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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jefferson.application.br.activity.VerifyActivity;

public class PhoneStatReceiver extends BroadcastReceiver {

    SharedPreferences sharedPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String numberToLauncher = sharedPrefs.getString("secret_code", "#4321");
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
