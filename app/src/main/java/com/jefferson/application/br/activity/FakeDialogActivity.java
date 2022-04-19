package com.jefferson.application.br.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.jefferson.application.br.App;

public class FakeDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String appName = getIntent().getStringExtra("app_name");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format("Infelizmente, %s parou.", appName));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            }
        );
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            }
        );
        builder.create().show();
    }
}
