package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.jefferson.application.br.ContactsActivity;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.JDebug;
import android.support.v4.app.NotificationManagerCompat;

public class DeveloperActivity extends MyCompatActivity {

    private static final int NOTIFICATION_REQUEST_CODE = 2;

    private String TAG = "Notifaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_layout);
        Switch switchView = (Switch) findViewById(R.id.developerlayoutSwitch);
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

    public void openPin(View v) {
        Intent intent = new Intent(this, PinActivity.class);
        startActivity(intent);
    }

    public void contacts(View v) {      
        startActivity(new Intent(this, ContactsActivity.class));
    }

    public void notification(View v) {
        if (!NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()) .contains(getApplicationContext().getPackageName())) { 
            //We dont have access 
            Intent intent= new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"); //For API level 22+ you can directly use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
            startActivityForResult(intent, NOTIFICATION_REQUEST_CODE); 
        } else { //Your own logic 
            Log.d(TAG, "You have Notification Access"); 
            JDebug.toast("You have Notification Access");
        }
        //Intent inten = new Intent(this, NotificationActivity.class);
        //startActivity(inten);
    }

    public void camera(View v) {
        Intent inten = new Intent(this, Camera.class);
        startActivity(inten);
    }

    public void quit(View v) {
        finish();
    }


    public void testThread(View v) {
        final SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.PROGRESS_STYLE);
        dialog.setTitle("thread teste");
        dialog.setMax(100);
        dialog.show();

        new JTask() {

            @Override
            public void onException(Exception e) {
                dialog.setStyle(SimpleDialog.ALERT_STYLE);
                dialog.setTitle("Execeção ocorrida!");
                dialog.setMessage("Error caught! " + e.getMessage());
                dialog.setPositiveButton("okay", null);
            }

            long x = 0;
            int progress;

            @Override
            public void workingThread() {

                for (progress = 0; progress <= 100; progress ++) {
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (progress >= 56) {
                        throw new RuntimeException("Teste de Exceção!");
                    }

                    sendUpdate(progress);
                }
            }

            @Override
            protected void onUpdated(Object... args) {
                dialog.setProgress(args[0]);
            }

            @Override
            protected void onInterrupted() {
                Toast.makeText(DeveloperActivity.this, "Interrupted!", 1).show();
            }

            @Override
            public void onBeingStarted() {
                Toast.makeText(DeveloperActivity.this, "STARTED!\nx = " + x, 0).show();
            }

            @Override
            public void onFinished() {
                dialog.setPositiveButton("close", null);
                Toast.makeText(DeveloperActivity.this, "FINISHED!\nx = " + x, 0).show();

            }
        }.start();
    }
}
