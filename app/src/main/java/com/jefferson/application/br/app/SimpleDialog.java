package com.jefferson.application.br.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.jefferson.application.br.library.NumberProgressBar;
import com.jefferson.application.br.R;
import android.widget.Button;
import android.support.v4.content.ContextCompat;
import android.view.Window;

public class SimpleDialog extends AlertDialog {

	public static final int PROGRESS_STYLE = 123;
	public static final int ALERT_STYLE = 321;

    private View contentView;
	private NumberProgressBar progressBar;
	private TextView contentText;
	private TextView contentTitle;
	private SimpleDialog progressBarDialog;
	private FrameLayout extraLayout;
	private long maxBytes;
	private long currentBytes;
	private int progress;
    private Button positiveButton;
    private Button negativeButton;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressBar.setProgress(msg.what);
		}
	};

	public SimpleDialog(Context context, int style) {
		super(context);
	    create(style);
	}

    public SimpleDialog(Context context) {
		super(context);
	    create(0);
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
        contentText.setSingleLine(single);
        return this;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(contentView);
	}

	private void create(int style) {
		progressBarDialog = this;
		contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_progress_view, null);
		progressBar = contentView.findViewById(R.id.number_progress_bar);
		extraLayout = contentView.findViewById(R.id.extra_view);
		contentTitle = contentView.findViewById(R.id.title_text_view);
		contentText = contentView.findViewById(R.id.message_text_view);
		positiveButton = contentView.findViewById(R.id.dialogPositiveButton);
		negativeButton = contentView.findViewById(R.id.dialogNegativebutton);

        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setBackgroundDrawableResource(R.drawable.dialog_bg_inset);
        int color  = ContextCompat.getColor(getContext(), R.color.colorAccent);
        progressBar.setMax(100);
        progressBar.setReachedBarColor(color);
        progressBar.setProgressTextColor(color);
		setStyle(style);
    }

	public void setStyle(int style) {
        boolean show = style == PROGRESS_STYLE ? true: false;
		showNegativeButton(false);
		showPositiveButton(false);
        setSingleLineMessage(false);
        showProgressBar(show);
	}

	public SimpleDialog setProgress(int progress) {
        mHandler.sendEmptyMessage(progress);
		this.progress = progress;
        return this;
    }

	public SimpleDialog addContentView(View view) {
		extraLayout.setVisibility(View.VISIBLE);
		extraLayout.addView(view);
		return this;
	}

	public int getProgress() {
        return progress;
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

    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
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
            dismiss();
		}
	}

	abstract public static class OnDialogClickListener {
	    public abstract boolean onClick(SimpleDialog dialog);
	}
}
