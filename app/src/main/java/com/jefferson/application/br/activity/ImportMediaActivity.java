package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.task.MonoTypePrepareTask;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.view.CircleProgressView;
import java.util.ArrayList;
import android.support.design.widget.Snackbar;

public class ImportMediaActivity extends MyCompatActivity implements JTask.OnUpdatedListener, JTask.OnBeingStartedListener, JTask.OnFinishedListener {

    private TextView prepareTextView;
    private CircleProgressView progressView;
    private TextView messageTextView;
    private ImportTask importTask;

    private ArrayList<String> mediaList;
    public static final String TYPE_KEY = "type_key";
    public static final String MEDIA_LIST_KEY = "media_list_key";
    public static final String POSITION_KEY = "position_key";
    public static String PARENT_KEY;
    private MonoTypePrepareTask prepareTask;
    private TextView titleTextView;
    private ImportMediaActivity.AnimateProgressText animateText;
    private boolean allowCancel;
    private TextView prepareTitleView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_media_layout);
        prepareTitleView =findViewById(R.id.import_media_title_preparation_text_view);
        prepareTextView = findViewById(R.id.import_media_prepare_text_view);
        messageTextView = findViewById(R.id.import_media_message_text_view);
        titleTextView = findViewById(R.id.import_media_title_move_text_view);
        progressView = findViewById(R.id.circle_progress_view);

        Intent intent = getIntent();
        mediaList = (ArrayList<String>) getIntent().getStringArrayListExtra(MEDIA_LIST_KEY);
        String parent = intent.getStringExtra(PARENT_KEY);
        String type = intent.getStringExtra(TYPE_KEY);

        prepareTask = new MonoTypePrepareTask(this, mediaList, type, parent);

        if (parent == null) {
            prepareTask.setDestination(Storage.getFolder(type == FileModel.IMAGE_TYPE ? Storage.IMAGE: Storage.VIDEO).getAbsolutePath());
        }

        prepareTask.setOnUpdatedListener(this);
        prepareTask.setOnFinishedListener(new JTask.OnFinishedListener() {

                private ImportMediaActivity.AnimateProgressText animateText;

                @Override
                public void onFinished() {
                    importTask = new ImportTask(ImportMediaActivity.this, prepareTask.getData() , null);
                    importTask.setOnUpdatedListener(ImportMediaActivity.this);
                    importTask.setOnbeingStartedListener(ImportMediaActivity.this);
                    importTask.setOnFinishedListener(ImportMediaActivity.this);
                    importTask.start();

                }
            }
        );
        prepareTask.start();
//        importTask = new ImportTask(this, null, null);
//        importTask.setOnUpdatedListener(this);
    }

    @Override
    public void onBeingStarted() {
        prepareTitleView.setText("Transferred");
        animateText = new AnimateProgressText(titleTextView, importTask);
        animateText.start();
    }

    @Override
    public void onFinished() {
        animateText.cancel();
        titleTextView.setText(getString(R.string.resultado));
        messageTextView.setTextColor(ContextCompat.getColor(this, R.color.pureGreen));
        messageTextView.setText("Success!");
    }

    @Override
    public void onUpdated(Object[] values) {
        int phrase = values[0];
        switch (phrase) {
            case 1:
                prepareTextView.setText(String.format(getString(R.string.importar_preparacao_msg), values[1], values[2]));
                break;
            case 2:
                if (values.length > 4) {
                    Toast.makeText(App.getAppContext(), "Invalid arguments: args = " + values.length, 0).show();
                    break;
                }
                Object message = values[1];
                if (message != null) {
                    messageTextView.setText((String)values[1]);
                }
                Object progress = values[2];
                if (progress != null) {
                    progressView.setProgress(progress);
                }
                Object max = values[3];
                if (max != null) {
                    progressView.setMax(max);
                }
                //progressView.setProgress(values[1]);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (prepareTask != null && prepareTask.status == JTask.Status.STARTED) {
            prepareTask.cancelTask();
        }
        if (importTask != null && importTask.status == JTask.Status.STARTED) {
            importTask.cancelTask();
        }
    }

    @Override
    public void onBackPressed() {

        if ((prepareTask != null && importTask != null) && prepareTask.status != JTask.Status.STARTED && importTask.status != JTask.Status.STARTED) {
            setResult(RESULT_OK);
            super.onBackPressed();
        } else {
            if (allowCancel) {
                if (importTask != null && importTask.status == JTask.Status.STARTED) {
                    importTask.interrupt();
                }
                if (prepareTask != null && prepareTask.status == JTask.Status.STARTED) {
                    prepareTask.cancelTask();
                }
                setResult(RESULT_OK);
                super.onBackPressed();
            } else {
                allowCancel = true;
                Snackbar.make(messageTextView,"Press back button again to cancel!", Snackbar.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            allowCancel = false;
                        }
                    }, 2000);
            }
        }
    }

//    private void showCancelDialog() {
//        SimpleDialog dialog = new SimpleDialog(this);
//        dialog.setTitle("Cancel");
//        dialog.setMessage("Do you want to cancel task?");
//        dialog.setPositiveButton(android.R.string.yes, new SimpleDialog.OnDialogClickListener(){
//
//                @Override
//                public boolean onClick(SimpleDialog dialog) {
//                    importTask.cancelTask();
//                    return true;
//                }
//            }
//        );
//        dialog.setNegativeButton(android.R.string.no, new SimpleDialog.OnDialogClickListener(){
//
//                @Override
//                public boolean onClick(SimpleDialog dialog) {
//                    importTask.continueWork();
//                    return true;
//                }
//            }
//        );
//        dialog.setCancelable(false);
//        dialog.show();
//    }

    private class AnimateProgressText extends Thread {

        private TextView textView;
        private String text;
        private String sufix = "";
        private JTask task;

        private Handler updateHandler = new Handler() {

            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                if (textView != null) {
                    textView.setText(text + sufix);
                }
            }
        };

        public AnimateProgressText(TextView textView, JTask task) {
            this.textView = textView;
            this.text = textView.getText().toString();
            this.task = task;
        }

        public void cancel() {
            this.textView = null;
            interrupt();
        }

        @Override
        public void run() {
            super.run();

            while (task.getStatus() == JTask.Status.STARTED) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                updateHandler.sendEmptyMessage(0);
                sufix = sufix.length() > 2 ? "" : (sufix += ".");
            }
        }
    }
}
