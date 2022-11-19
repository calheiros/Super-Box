package com.jefferson.application.br.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jefferson.application.br.R;
import android.widget.Toast;

public class ManageSpaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
          Toast.makeText(this, "ACTION: " + getIntent().getAction(), Toast.LENGTH_LONG).show();
    }
}
