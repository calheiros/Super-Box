package com.jefferson.application.br.app;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.library.NumberProgressBar;

public class SimpleDialog  {

	public static final int PROGRESS_STYLE = 123;
	public static final int ALERT_STYLE = 321;

    private ViewGroup parentView;
	private NumberProgressBar progressBar;
	private TextView contentText;
	private TextView contentTitle;
	private SimpleDialog progressBarDialog;
	private long maxBytes;
	private long currentBytes;
	private int progress;
    private boolean dismissed = false;
    private Button positiveButton;
    private Button negativeButton;
    private JDialog jdialog;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressBar.setProgress(msg.what);
		}
	};

    private Context context;
    private View editTextLayout;
    public static final int INPUT_STYLE = 444;

	public SimpleDialog(Context context, int style) {
        this.context = context;
	    createView(style);
	}

    public SimpleDialog(Context context) {
        this.context = context;
	    createView(0);
	}

    public void cancel() {
        jdialog.cancel();
    }

    public void setCanceledOnTouchOutside(boolean cancelable) {
        jdialog.setCanceledOnTouchOutside(cancelable);
    }

	public long getCurrentBytes() {
		return currentBytes;
	}

	public void registerBytes(long count) {
		this.currentBytes = count;
	}

	public void setMaxBytes(long max) {
		this.maxBytes = max;
	}

	public long getMaxBytes() {
		return maxBytes;
	}

	public int getMax() {
		return progressBar.getMax();
	}

    public SimpleDialog setSingleLineMessage(boolean single) {
        //deprecated
        //contentText.setSingleLine(single);
        if (single) { 
            contentText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            contentText.setMaxLines(1);
        } else {
            contentText.setMaxLines(256);
            contentText.setEllipsize(TextUtils.TruncateAt.END);

        }
        return this;
    }

	private void createView(int style) {
		progressBarDialog = this;
		parentView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_progress_view, null);
		progressBar = parentView.findViewById(R.id.number_progress_bar);
		//extraView = contentView.findViewById(R.id.extra_view);
		contentTitle = parentView.findViewById(R.id.title_text_view);
		contentText = parentView.findViewById(R.id.message_text_view);
		positiveButton = parentView.findViewById(R.id.dialogPositiveButton);
		negativeButton = parentView.findViewById(R.id.dialogNegativebutton);
        editTextLayout = parentView.findViewById(R.id.dialog_edit_text_layout);

        this.jdialog = new JDialog(context, style == INPUT_STYLE);

        int color  = ContextCompat.getColor(context, R.color.colorAccent);
        progressBar.setMax(100);
        progressBar.setReachedBarColor(color);
        progressBar.setProgressTextColor(color);
		setStyle(style);
    }

	public void setStyle(int style) {
        boolean show = style == PROGRESS_STYLE ? true: false;
		showNegativeButton(false);
		showPositiveButton(false);
        showTextMessage(false);
        setSingleLineMessage(false);
        showProgressBar(show);
        //showEditText(style == INPUT_STYLE);
	}

    private void showTextMessage(boolean show) {
        contentText.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showEditText(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        editTextLayout.setVisibility(visibility);
    }

	public SimpleDialog setProgress(int progress) {
        mHandler.sendEmptyMessage(progress);
		this.progress = progress;
        return this;
    }

	public SimpleDialog setContentView(View view) {
        parentView.addView(view, 3, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		return this;
	}

	public int getProgress() {
        return progress;
    }

    public SimpleDialog setCancelable(boolean cancelable) {
        this.jdialog.setCancelable(false);
        return this;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.jdialog.setOnDismissListener(listener);
    }

    public void dismiss() {
        if (jdialog != null) {
            this.jdialog.dismiss();
        }
    }

    public boolean isDismissed() {
        return dismissed;
    }

	public SimpleDialog showProgressBar(boolean show) {
		progressBar.setVisibility(show ? View.VISIBLE: View.GONE);
		return this;
	}

	public SimpleDialog showPositiveButton(boolean show) {
		positiveButton.setVisibility(show ? View.VISIBLE: View.GONE);
		return this;
	}

	public SimpleDialog showNegativeButton(boolean show) {
		negativeButton.setVisibility(show ? View.VISIBLE: View.GONE);
		return this;
	}

	public SimpleDialog setMax(int value) {
		progressBar.setMax(value);
		return this;
	}

    public SimpleDialog setTitle(int titleId) {
        setTitle(context.getString(titleId));
        return this;
    }

    public Context getContext() {
        return context;
    }

	public SimpleDialog setTitle(String title) {
        if (contentTitle.getVisibility() != View.VISIBLE)
		    contentTitle.setVisibility(View.VISIBLE);
		contentTitle.setText(title);
		return this;
	}

	public SimpleDialog setMessage(String text) {
        if (contentText.getVisibility() != View.VISIBLE)
		    contentText.setVisibility(View.VISIBLE);
		contentText.setText(text);
		return this;
	}

    public SimpleDialog show() {
        //this.mAlertDialog = builder.create();
        //mAlertDialog.setContentView(contentView);

        this.jdialog.show();
        return this;
    }

    public void setMessage(CharSequence message) {
        setMessage(String.valueOf(message));
    }

    public void setTitle(CharSequence title) {
        setTitle(String.valueOf(title));
    }

	public SimpleDialog setPositiveButton(String buttonText, OnDialogClickListener listener) {
        if (positiveButton.getVisibility() != View.VISIBLE)
            positiveButton.setVisibility(View.VISIBLE);
		positiveButton.setText(buttonText);
		positiveButton.setOnClickListener(new OnClickListener(listener));
		return this;
	}

	public SimpleDialog setNegativeButton(String buttonText, OnDialogClickListener listener) {
        if (negativeButton.getVisibility() != View.VISIBLE)
		    negativeButton.setVisibility(View.VISIBLE);
		negativeButton.setText(buttonText);
		negativeButton.setOnClickListener(new OnClickListener(listener));
		return this;
	}

	private class OnClickListener implements View.OnClickListener {

		private OnDialogClickListener listener;
        public OnClickListener(OnDialogClickListener listener) {
			this.listener = listener;
		}

        @Override
		public void onClick(View view) {
            if (listener != null) {
			    if (!listener.onClick(progressBarDialog)) return;
			}
            jdialog.dismiss();
		}
	}

    private class JDialog extends AlertDialog {
        private boolean requestKeyboard = false;

        public JDialog(Context context, boolean requestKeyboard) {
            super(context);
            this.requestKeyboard = requestKeyboard;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(parentView);

            Window window = getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); 
            window.setBackgroundDrawableResource(R.drawable.dialog_bg_inset); 

            if (requestKeyboard) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }

        }

        @Override
        public void dismiss() {
            super.dismiss();
            dismissed = true;
        }

        @Override
        public void cancel() {
            super.cancel();
            dismissed = true;
        }
    }
   
	abstract public static class OnDialogClickListener {
	    public abstract boolean onClick(SimpleDialog dialog);
	}
}
