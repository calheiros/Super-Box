package com.jefferson.application.br.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import android.content.Intent;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.ContactsActivity;

public class DeveloperActivity extends MyCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_layout);
        Switch switchView = findViewById(R.id.developerlayoutSwitch);
        switchView.setChecked(JDebug.isDebugOn());
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton compButton, boolean checked) {
                    JDebug.setDebug(checked);
                    String msg = checked ? "Debug mode: ENABLED!" : "Debug mode: DISABLED!";
                    Toast.makeText(DeveloperActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    public void showAlertDialog(View v) {
        SimpleDialog simple = new SimpleDialog(this);
        simple.setTitle(R.string.ascii_shrug);
        simple.setMessage("Mensagem de teste");
        simple.setPositiveButton("ok", null);
        simple.show();
    }
    
    public void showProgressDialog(View v) {
        SimpleDialog simple = new SimpleDialog(this, SimpleDialog.PROGRESS_STYLE);
        simple.setTitle(R.string.ascii_shrug);
        simple.setMessage("Mensagem de teste");
        simple.setProgress(76);
        simple.setPositiveButton("ok", null);
        simple.setNegativeButton("cancelar", null);
        simple.show();
    }
    
    public void contacts(View v){      
        startActivity(new Intent(this, ContactsActivity.class));
    }
    
    public void notification(View v) {
        Intent inten = new Intent(this, NotificationActivity.class);
        startActivity(inten);
    }
    
    public void camera (View v){
        Intent inten = new Intent(this, Camera.class);
        startActivity(inten);
    }
    
    public void quit(View v) {
        finish();
    }
}
