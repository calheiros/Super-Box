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

package com.jefferson.application.br.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.jefferson.application.br.R;
import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {
    
    public static final String TAG = "CrushActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_layout);
        TextView text = (TextView) findViewById(R.id.crashlayoutTextView);
        String message = getIntent().getStringExtra("message");
        text.setText(message);
    }
}
