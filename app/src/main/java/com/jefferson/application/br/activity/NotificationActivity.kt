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
package com.jefferson.application.br.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.jefferson.application.br.R

class NotificationActivity : MyCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_layout)
        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(
                applicationContext.packageName
            )
        ) {
            //not have access
            val intent =
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS") //For API level 22+ you can directly use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(intent, NOTIFICATION_REQUEST_CODE)
        }
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 7
        private const val TAG = "NotificationActivity"
    }
}