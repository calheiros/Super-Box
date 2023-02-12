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

package com.jefferson.application.br.receiver;

import android.content.BroadcastReceiver; 
import android.content.Context; 
import android.content.Intent; 
import android.content.IntentFilter; 
import android.util.Log;

public class KeyWatcher { 
    static final String TAG = "hg"; 
    private Context context;
    private IntentFilter mFilter;
    private OnHomePressedListener mListener; 
    private InnerReceiver mReceiver; 
    
    public KeyWatcher(Context context) {
        this.context = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS); 
    } 

    public void setOnHomePressedListener(OnHomePressedListener listener) { 
        mListener = listener; mReceiver = new InnerReceiver(); }

    public void startWatch() {
        if (mReceiver != null) {
            context.registerReceiver(mReceiver, mFilter);
        } 
    }
    
    public void stopWatch() {
        if (mReceiver != null) { 
            context.unregisterReceiver(mReceiver);
        }
    }
    
    class InnerReceiver extends BroadcastReceiver { 
        final String SYSTEM_DIALOG_REASON_KEY = "reason"; final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"; @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); 
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY); if (reason != null) {
                    Log.e(TAG, "action:" + action + ",reason:" + reason); 
                    if (mListener != null) { 
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            mListener.onRecentsPressed();
                        }
                    }
                }
            }
        }
    }
    
    public interface OnHomePressedListener { 
        void onHomePressed(); 
        void onRecentsPressed(); 
    }
} 

