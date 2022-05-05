package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.res.Resources;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.task.MonoTypePrepareTask;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.view.CircleProgressView;
import java.util.ArrayList;
import android.text.TextUtils;

public class ImportMediaActivity extends MyCompatActivity implements JTask.OnUpdatedListener, JTask.OnBeingStartedListener, JTask.OnFinishedListener {

    private TextView prepareTextView;
    private CircleProgressView progressView;
    private TextView messageTextView;
    private ImportTask importTask;
    private ArrayList<String> mediaList;

    public static final String TYPE_KEY = "type_key";
    public static final String MEDIA_LIST_KEY = "media_list_key";
    public static final String POSITION_KEY = "position_key";
    public static final String PARENT_KEY = "parent_key";

    private MonoTypePrepareTask prepareTask;
    private TextView titleTextView;
    private ImportMediaActivity.AnimateProgressText animateText;
    private boolean allowCancel;
    private TextView prepareTitleView;
    private Button button;
    private int typeQuatityRes;
    private int flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    private AdView adview;

    public static final String MODELS_KEY = "models_key";

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
        adview = (AdView)findViewById(R.id.ad_view);
        adview.loadAd(new AdRequest.Builder().build());

        Intent intent = getIntent();
        ArrayList <FileModel> data = null;

        if ((data = intent.getParcelableArrayListExtra(MODELS_KEY)) != null) {
            typeQuatityRes = R.plurals.quantidade_arquivo_total;
            startImportTask(data);
        } else {
            mediaList = (ArrayList<String>) getIntent().getStringArrayListExtra(MEDIA_LIST_KEY);
            String parent = intent.getStringExtra(PARENT_KEY);
            String type = intent.getStringExtra(TYPE_KEY);
            prepareTask = new MonoTypePrepareTask(this, mediaList, type, parent);
            
            if (type != null) {
                typeQuatityRes = type.equals(FileModel.IMAGE_TYPE) ? R.plurals.quantidade_imagem_total : R.plurals.quantidade_video_total;
            }
            if (parent == null) {
                prepareTask.setDestination(Storage.getFolder(type == FileModel.IMAGE_TYPE ? Storage.IMAGE: Storage.VIDEO).getAbsolutePath());
            }
            prepareTask.setOnUpdatedListener(this);
            prepareTask.setOnFinishedListener(new JTask.OnFinishedListener() {

                    private ImportMediaActivity.AnimateProgressText animateText;

                    @Override
                    public void onFinished() {
                        startImportTask(prepareTask.getData());
                    }
                }
            );
            prepareTask.start();
        }
    }

    private void startImportTask(ArrayList<FileModel> data) {
        importTask = new ImportTask(this, data , null);
        importTask.setOnUpdatedListener(this);
        importTask.setOnbeingStartedListener(this);
        importTask.setOnFinishedListener(this);
        importTask.start();
    }

    public void buttonClick(View v) {
        if (!isTaskNotRunning()) {
            interruptTask();
        }
        setResult(RESULT_OK);
        finish();
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
        if (progressView.getProgress() != 100) {
            progressView.setProgress(100);
        }
        
        getWindow().clearFlags(flagKeepScreenOn);
        animateText.cancel();

        Resources res = getResources();
        boolean criticalError = importTask.error() != null;
        int failures = importTask.getFailuresCount();
        int color = failures > 0  ? ContextCompat.getColor(this, R.color.red): getAttrColor(R.attr.commonColorDark) ;
        String msg = criticalError ? getString(R.string.erro_critico) : failures > 0 ? res.getQuantityString(
            R.plurals.falha_plural, failures, failures) : getString(R.string.transferencia_sucesso);

        titleTextView.setText(getString(R.string.resultado));
        messageTextView.setTextColor(color);
        messageTextView.setText(msg);
        messageTextView.setMaxLines(5);
        messageTextView.setEllipsize(TextUtils.TruncateAt.END);

        button.setTextColor(getAttrColor(R.attr.colorAccent));
        button.setText(getString(android.R.string.ok));
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
    public void onResume() {
        super.onResume();
        adview.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adview.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adview.destroy();
        //interruptTask();
    }

    @Override
    public void onBackPressed() {

        if (isTaskNotRunning()) {
            setResult(RESULT_OK);
            super.onBackPressed();
        } else {
            if (allowCancel) {
                interruptTask();
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

    private void interruptTask() {
        if (importTask != null && importTask.status == JTask.Status.STARTED) {
            importTask.interrupt();
        }
        if (prepareTask != null && prepareTask.status == JTask.Status.STARTED) {
            prepareTask.cancelTask();
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