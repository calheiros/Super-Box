package com.jefferson.application.br.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.widget.TextView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.view.CircleProgressView;

public class ImportMediaActivity extends MyCompatActivity {

    private CircleProgressView progressView;
    private TextView messageTextView;
    private Handler handler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            progressView.setProgress(msg.what);
            textView.setText("preparing " + msg.what + " of " + "100" + (msg.what == 100 ? " done." : ""));
        }
    };

    private Parcelable[] mediaList;
    private TextView textView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_media_layout);
        textView = findViewById(R.id.import_media_prepare_text_view);
        messageTextView = findViewById(R.id.import_media_prepare_text_view);
        progressView = findViewById(R.id.circle_progress_view);
        mediaList = getIntent().getParcelableArrayExtra("media_list");
        testProgress();
    }
    
    private void testProgress() {
        new Thread() {
            float progress = 0;
            @Override
            public void run() {
                while (progress <= 100) {
                    handler.sendEmptyMessage((int)progress);
                    progress++;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {}
                }
            }
        }.start();
    }
}
