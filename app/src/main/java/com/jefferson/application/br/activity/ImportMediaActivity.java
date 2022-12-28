package com.jefferson.application.br.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.jefferson.application.br.App;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.task.FileModelBuilderTask;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.view.CircleProgressView;

import java.util.ArrayList;
import java.util.Objects;

public class ImportMediaActivity extends MyCompatActivity implements JTask.OnUpdatedListener, JTask.OnBeingStartedListener, JTask.OnFinishedListener {

    public static final String TYPE_KEY = "type_key";
    public static final String MEDIA_LIST_KEY = "media_list_key";
    public static final String POSITION_KEY = "position_key";
    public static final String PARENT_KEY = "parent_key";
    public static final String MODELS_KEY = "models_key";
    private final int flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    private FrameLayout parent;
    private TextView prepareTextView;
    private CircleProgressView progressView;
    private TextView messageTextView;
    private ImportTask importTask;
    private FileModelBuilderTask builderTask;
    private TextView titleTextView;
    private ImportMediaActivity.AnimateProgressText animateText;
    private boolean allowCancel;
    private TextView prepareTitleView;
    private Button button;
    private int typeQuantityRes;
    private AdView adview;

    public static void removeParent(View v) {
        ViewParent parent = v.getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            group.removeView(v);// w  w w .j  a  va  2  s.co m
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_media_layout);
        getWindow().addFlags(flagKeepScreenOn);

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        prepareTitleView = (TextView) findViewById(R.id.import_media_title_preparation_text_view);
        prepareTextView = (TextView) findViewById(R.id.import_media_prepare_text_view);
        messageTextView = (TextView) findViewById(R.id.import_media_message_text_view);
        titleTextView = (TextView) findViewById(R.id.import_media_title_move_text_view);
        progressView = (CircleProgressView) findViewById(R.id.circle_progress_view);
        button = (Button) findViewById(R.id.import_media_button);

        MainActivity mainActivity = MainActivity.getInstance();
        adview = Objects.requireNonNull(mainActivity == null ?
                MainActivity.createSquareAdview(this) : mainActivity.getSquareAdView());
        removeParent(adview);
        parent = findViewById(R.id.ad_view_layout);
        parent.addView(adview);
        startImportTask();
    }

    private void startImportTask() {
        Intent intent = getIntent();
        ArrayList<FileModel> files = intent.getParcelableArrayListExtra(MODELS_KEY);

        if (files != null) {
            typeQuantityRes = R.plurals.quantidade_arquivo_total;
            startImportTask(files);
        } else {
            ArrayList<String> filesPath = (ArrayList<String>) getIntent().getStringArrayListExtra(MEDIA_LIST_KEY);
            String parent = intent.getStringExtra(PARENT_KEY);
            String type = intent.getStringExtra(TYPE_KEY);
            builderTask = new FileModelBuilderTask(this, filesPath, type, parent);
            if (type != null) {
                typeQuantityRes = type.equals(FileModel.IMAGE_TYPE) ? R.plurals.quantidade_imagem_total : R.plurals.quantidade_video_total;
            }
            if (parent == null) {
                builderTask.setDestination(Storage.getFolder(FileModel.IMAGE_TYPE.equals(type) ? Storage.IMAGE : Storage.VIDEO).getAbsolutePath());
            }
            builderTask.setOnUpdatedListener(this);
            builderTask.setOnFinishedListener(new JTask.OnFinishedListener() {

                                                  @Override
                                                  public void onFinished() {
                                                      startImportTask(builderTask.getData());
                                                  }
                                              }
            );
            builderTask.start();
            prepareTitleView.setText("Checking");
        }
    }

    private void startImportTask(ArrayList<FileModel> data) {
        importTask = new ImportTask(this, data, null);
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
        return (builderTask != null && importTask != null) && builderTask.status != JTask.Status.STARTED && importTask.status != JTask.Status.STARTED;
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

        Resources res = getResources();
        boolean criticalError = importTask.error() != null;
        int failures = importTask.getFailuresCount();
        int color = failures > 0 || importTask.isInterrupted() ? ContextCompat.getColor(this, R.color.red) : getAttrColor(R.attr.commonColor);
        String msg = criticalError ? getString(R.string.erro_critico) : failures > 0 ? res.getQuantityString(
                R.plurals.falha_plural, failures, failures) :
                importTask.isInterrupted() ? "Cancelled!" : getString(R.string.transferencia_sucesso);


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
        int phrase = (int) values[0];
        switch (phrase) {
            case 1:
                String format = getResources().getQuantityString(typeQuantityRes, (int) values[1], values[1], values[2]);
                Spanned styledText = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(format, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(format);
                prepareTextView.setText(styledText);
                break;
            case 2:
                if (values.length > 4) {
                    Toast.makeText(App.getAppContext(), "Invalid arguments: args = " + values.length, Toast.LENGTH_SHORT).show();
                    break;
                }
                Object message = values[1];
                if (message != null) {
                    messageTextView.setText((String) values[1]);
                }
                Object progress = values[2];
                if (progress != null) {
                    progressView.setProgress((double) progress);
                }
                Object max = values[3];
                if (max != null) {
                    progressView.setMax((double) max);
                }
                break;
            case -2:
                showNoSpaceAlert(importTask, values[1].toString());
                break;
        }
    }

    private void showNoSpaceAlert(ImportTask task, String message) {
        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.STYLE_ALERT);
        dialog.setTitle("Aviso");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Continuar", new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(SimpleDialog dialog) {
                task.stopWaiting();
                return true;
            }
        });

        dialog.setNegativeButton(getString(R.string.cancelar), null);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (task.isWaiting()) {
                    task.interrupt();
                    task.stopWaiting();
                }
            }
        });
        dialog.show();

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
        if (importTask != null && importTask.isWaiting()) {
            importTask.cancelTask();
            Toast.makeText(this, "task canceled in hero mode", Toast.LENGTH_SHORT).show();
        }
        //parent.removeView(adview);
        //adview.destroy();
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

        if (builderTask != null && builderTask.status == JTask.Status.STARTED) {
            builderTask.cancelTask();
        }
    }

    private static class AnimateProgressText extends Thread {

        private TextView textView;
        private final String text;
        private String suffix = "";
        private final Handler updateHandler = new Handler() {

            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                if (textView != null) {
                    textView.setText(text + suffix);
                }
            }
        };
        private final JTask task;

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
                suffix = suffix.length() > 2 ? "" : (suffix += ".");
            }
        }
    }
}
