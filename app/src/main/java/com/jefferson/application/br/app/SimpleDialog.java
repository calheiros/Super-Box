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
import com.jefferson.application.br.Library.NumberProgressBar;
import com.jefferson.application.br.R;

public class SimpleDialog extends AlertDialog {

	public static final int PROGRESS_STYLE = 123;
	public static final int ALERT_STYLE = 321;
	private View contentView;
	private NumberProgressBar progressBar;
	private TextView contentText;
	private TextView contentTitle;
	private View positive_container, negative_container;
	private TextView bt_positive, bt_negative;
	private SimpleDialog progress_bar_dialog;
	private FrameLayout extra_contentext_view;
	private long maxBytes;
	private long currentBytes;
	private int progress;
	private Handler mHandler = new Handler(){

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

	public void setStyle(int style) {
		configure(style);
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(contentView);
	}
	private void create(int style) {

		progress_bar_dialog = this;
		contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_progress_view, null);
		progressBar = (NumberProgressBar) contentView.findViewById(R.id.number_progress_bar);
		extra_contentext_view = (FrameLayout) contentView.findViewById(R.id.extra_content_view);
		contentTitle = (TextView) contentView.findViewById(R.id.contentTitle);
		contentText = (TextView) contentView.findViewById(R.id.content_text_view);
		bt_positive = (TextView) contentView.findViewById(R.id.positive_view);
		bt_negative = (TextView) contentView.findViewById(R.id.negative_view);

		progressBar.setMax(100);
		positive_container = contentView.findViewById(R.id.positive_bt_container);
		negative_container = contentView.findViewById(R.id.negative_bt_container);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		configure(style);
    }

	private void configure(int style) {

		contentText.setVisibility(View.GONE);
		contentTitle.setVisibility(View.GONE);
		showNegativeButton(false);
		showPositiveButton(false);

		if (style == ALERT_STYLE) {
            showProgressBar(false);
        } else if (style == PROGRESS_STYLE) {
            showProgressBar(true);
        }
	}

	public SimpleDialog setProgress(int progress) {

        mHandler.sendEmptyMessage(progress);
		this.progress = progress;
        return this;
    }


	public SimpleDialog addContentView(View view) {
		extra_contentext_view.setVisibility(View.VISIBLE);
		extra_contentext_view.addView(view);
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
		positive_container.setVisibility(show ? View.VISIBLE: View.GONE);
		return this;
	}

	public SimpleDialog showNegativeButton(boolean show) {
		negative_container.setVisibility(show ? View.VISIBLE: View.GONE);
		return this;
	}
    
	public SimpleDialog setMax(int value) {
		progressBar.setMax(value);
		return this;
	}
    
	public SimpleDialog setContentTitle(String title) {
		contentTitle.setVisibility(View.VISIBLE);
		contentTitle.setText(title);
		return this;
	}
	public SimpleDialog setContentText(String text) {
		contentText.setVisibility(View.VISIBLE);
		contentText.setText(text);
		return this;
	}

	public SimpleDialog setPositiveButton(String buttonText, OnDialogClickListener listener) {
	    positive_container.setVisibility(View.VISIBLE);
		bt_positive.setText(buttonText);
		bt_positive.setOnClickListener(new OnClick(listener));

		return this;
	}
    
	public SimpleDialog setNegativeButton(String buttonText, OnDialogClickListener listener) {
		negative_container.setVisibility(View.VISIBLE);
		bt_negative.setText(buttonText);
		bt_negative.setOnClickListener(new OnClick(listener));
		return this;
	}
    
	private class OnClick implements View.OnClickListener {

		private OnDialogClickListener listener;
        public OnClick(OnDialogClickListener listener) {
			this.listener = listener;
		}
		@Override
		public void onClick(View view) {
			if (listener != null) {
				if (listener.onClick(progress_bar_dialog)) 
					dismiss();
			} else {
				dismiss();
			}
		}
	}
	abstract public static class OnDialogClickListener {
	    public abstract boolean onClick(SimpleDialog dialog);
	}
}
