package com.jefferson.application.br.widget;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.jefferson.application.br.*;
import android.os.*;

public class JProgressView extends View {

	private int progress = 0;
	private Paint textPaint;
	private float density;

	public JProgressView(Context context, AttributeSet attrs) {

		super(context, attrs);
		density = getResources().getDisplayMetrics().density;

		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setDither(true);
		textPaint.setColor(Color.WHITE);
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setTextSize(density * 18);

	}

	public void setProgress(int progress) {
		this.progress = progress;
		invalidate();
	}
	@Override
	protected void onDraw(Canvas canvas) {

		String message = progress == 0 ? getContext().getString(R.string.carregando) : progress + "%";
		int x = (int)((getWidth() - textPaint.measureText(message)) / 2);
		int y = getHeight() / 2;

		Paint mPaint = new Paint();
		mPaint.setColor(Color.parseColor("#50000000"));

		canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
		canvas.drawText(message, x, y, textPaint);
	}
}
