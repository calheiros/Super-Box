/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.jefferson.application.br.R;

public class JProgressView extends View {

	private final Paint mPaint;
	private int progress = 0;
	private final Paint textPaint;

	public JProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		float density = getResources().getDisplayMetrics().density;
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setDither(true);
		textPaint.setColor(Color.WHITE);
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setTextSize(density * 18);

		mPaint = new Paint();
		mPaint.setColor(Color.parseColor("#50000000"));

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

		canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
		canvas.drawText(message, x, y, textPaint);
	}
}
