package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.task.MonoTypePrepareTask;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.view.CircleProgressView;
import java.util.ArrayList;
import android.content.res.Resources;

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
    private Button button;
    private int typeQuatityRes;

    private int flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_media_layout);
        getWindow().addFlags(flagKeepScreenOn);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        prepareTitleView = findViewById(R.id.import_media_title_preparation_text_view);
        prepareTextView = findViewById(R.id.import_media_prepare_text_view);
        messageTextView = findViewById(R.id.import_media_message_text_view);
        titleTextView = findViewById(R.id.import_media_title_move_text_view);
        progressView = findViewById(R.id.circle_progress_view);
        button = findViewById(R.id.import_media_button);

        Intent intent = getIntent();
        mediaList = (ArrayList<String>) getIntent().getStringArrayListExtra(MEDIA_LIST_KEY);
        String parent = intent.getStringExtra(PARENT_KEY);
        String type = intent.getStringExtra(TYPE_KEY);
        if (type != null) {
            typeQuatityRes = type.equals(FileModel.IMAGE_TYPE) ? R.plurals.importar_prepare_imagem_msg : R.plurals.importar_prepare_video_msg;
        }
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
    }

    public void buttonClick(View v) {
        if (isTaskNotRunning()) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean isTaskNotRunning() {
        return (prepareTask != null && importTask != null) && prepareTask.status != JTask.Status.STARTED && importTask.status != JTask.Status.STARTED;
    }

    @Override
    public void onBeingStarted() {
        prepareTitleView.setText(getString(R.string.transferido));
        animateText = new AnimateProgressText(titleTextView, importTask);
        animateText.start();
    }

    @Override
    public void onFinished() {
        getWindow().clearFlags(flagKeepScreenOn);
        animateText.cancel();
        
        titleTextView.setText(getString(R.string.resultado));
        Resources res = getResources();
        boolean criticalError = importTask.error() != null;
        int failures = importTask.getFailuresCount();
        int color = failures > 0  ? R.color.red : R.color.pureGreen;
        String msg = criticalError ? getString(R.string.erro_critico) : failures > 0 ? res.getQuantityString(
            R.plurals.falha_plural, failures, failures) : getString(R.string.sucesso);
        messageTextView.setTextColor(ContextCompat.getColor(this, color));
        messageTextView.setText(msg);
        
    }

    @Override
    public void onUpdated(Object[] values) {
        int phrase = values[0];
        switch (phrase) {
            case 1:
                String format = getResources().getQuantityString(typeQuatityRes, values[1], values[1], values[2]);
                Spanned styledText = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(format, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(format);
                prepareTextView.setText(styledText);
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

        if (isTaskNotRunning()) {
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
                Snackbar.make(messageTextView, "Press back button again to cancel!", Snackbar.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            allowCancel = false;
                        }
                    }, 2000);
            }
        }
    }

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
