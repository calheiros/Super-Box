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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.jefferson.application.br.R;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.View.OnLongClickListener;

public class PinActivity extends Activity implements OnLongClickListener {


    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_activity);
        editText = findViewById(R.id.pinActivityEditText);
        findViewById(R.id.pin_activityBackspaceButton).
            setOnLongClickListener(this);
    }

    public void putNumber(String num) {
        editText.append(num);
    }

    public void one(View v) {
        putNumber("1");
    }

    public void two(View v) {
        putNumber("2");
    }

    public void three(View v) {
        putNumber("3");
    }

    public void four(View v) {
        putNumber("4");
    }

    public void five(View v) {
        putNumber("5");
    }

    public void six(View v) {
        putNumber("6");
    }

    public void seven(View v) {
        putNumber("7");
    }

    public void eight(View v) {
        putNumber("8");
    }

    public void nine(View v) {
        putNumber("9");
    }

    public void zero(View v) {
        putNumber("0");
    }

    @Override
    public boolean onLongClick(View vie) {
        editText.setText(new String());
        return true;
    }

    private boolean isValidePassword(String password) {
        
        if ( password.length() < 4 ) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return false;
        }
        
        if ( password.length() > 20 ) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return false;
        }
        
       return true;
    }
    
    public void backspace(View v) {
        String text = editText.getText().toString();
        int size = text.length();

        if (size > 0) {
            editText.setText(text.substring(0, size - 1));
            editText.setSelection(size -1);
        }
    }
}
