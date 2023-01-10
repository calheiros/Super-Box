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

import android.widget.*;
import android.util.*;
import android.content.*;
import android.view.*;
import android.view.ScaleGestureDetector.*;
import android.graphics.*;

public class JPhotoView extends androidx.appcompat.widget.AppCompatImageView {
    
	private float mScaleFactor = 1.f;
	private ScaleGestureDetector mScaleDetector;

	public JPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScaleDetector = new ScaleGestureDetector(context, new zoomListener());
	}
    
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.scale(mScaleFactor, mScaleFactor, canvas.getWidth() / 2, canvas.getHeight() / 2);
		super.onDraw(canvas);
		canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		return true;
	}

	private class zoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 2f));
			invalidate();
			return true;
		}
	}
}
