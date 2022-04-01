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

    private void checkPasswordVality() {
        String password = editText.getText().toString();
        
        if ( password.length() < 4 ) {
            
        }
        
        if ( password.length() > 20 ) {
            
        }
       
    }
    
    public void backspace(View v) {
        String text = editText.getText().toString();
        int size = text.length();

        if (size > 0) {
            editText.setText(text.substring(0, size - 1));
        }
    }
}
