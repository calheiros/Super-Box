package com.jefferson.application.br.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.jefferson.application.br.R;
import android.support.v7.app.AppCompatActivity;

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
