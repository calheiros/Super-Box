package com.jefferson.application.br.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
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
import java.io.File;

public class DeveloperActivity extends MyCompatActivity {

    private static final int NOTIFICATION_REQUEST_CODE = 2;
    private static final String TAG = "Notifaction";
    private FileObserver observer;
    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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
        fileObserver();
    }
    
    public void toogleWifi(boolean state) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) 
            wifi.setWifiEnabled(true);
        else { 
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(panelIntent, 1); 
        }
    }
    
    public void enableWifi(View v) {
        toogleWifi(true);
        Toast.makeText(this, "Wifi ON", Toast.LENGTH_LONG).show();
    }

    public void disableWifi(View vi) {
        toogleWifi(false);
        Toast.makeText(this, "Wifi OFF", Toast.LENGTH_LONG).show();
    }

    public void showAlertDialog(View v) {
        SimpleDialog simple = new SimpleDialog(this);
        simple.setTitle(R.string.unicode_shrug);
        simple.setMessage("Mensagem de teste");
        simple.setPositiveButton("ok", null);
        simple.show();
    }

    public void showProgressDialog(View v) {
        SimpleDialog simple = new SimpleDialog(this, SimpleDialog.STYLE_PROGRESS);
        simple.setTitle(R.string.unicode_shrug);
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
        boolean bypass = true;
        if (bypass || !NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()) .contains(getApplicationContext().getPackageName())) { 
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
        Intent i = new Intent(this, VerifyActivity.class); Bundle b = null; 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { 
            //
            b = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), // 
                                                     v.getHeight()).toBundle(); 
            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888); 
            bitmap.eraseColor(Color.parseColor("#308cf8")); 
            b = ActivityOptions.makeThumbnailScaleUpAnimation(v, bitmap, 0, 0).toBundle(); 
        } 
        startActivity(i, b);
        //startActivity(new);
    }

    public void quit(View v) {
        
        Intent intent = new Intent(this, ImportMediaActivity.class);
        startActivity(intent);
    }

    private void fileObserver() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        observer = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? new MyObserver(file) : new MyObserver(file.getAbsolutePath());
        observer.startWatching();
    }

    private class MyObserver extends FileObserver {

        public MyObserver(String path) {
            super(path);
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        public MyObserver(File file) {
            super(file);
        }

        @Override
        public void onEvent(int event, String name) {
            JDebug.toast(name);
        }
    }

    public void testThread(View v) {
        final SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.STYLE_PROGRESS);
        dialog.setTitle("thread teste");
        dialog.setMax(100);
        dialog.show();

        new JTask() {

            @Override
            public void onException(Exception e) {
                dialog.resetDialog();
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
                dialog.setProgress((int) args[0]);
            }

            @Override
            protected void onInterrupted() {
                Toast.makeText(DeveloperActivity.this, "Interrupted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeingStarted() {
                Toast.makeText(DeveloperActivity.this, "STARTED!\nx = " + x, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinished() {
                dialog.setPositiveButton("close", null);
                Toast.makeText(DeveloperActivity.this, "FINISHED!\nx = " + x, Toast.LENGTH_SHORT).show();

            }
        }.start();
    }
}
