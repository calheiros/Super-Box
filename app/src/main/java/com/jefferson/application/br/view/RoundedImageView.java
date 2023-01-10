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

package com.jefferson.application.br.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundedImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final float DEFAULT_RADIUS = 10.0f;
    private RectF rect;
    private Path clipPath;
    float radius = 0; // angle of round corners

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clipPath = new Path();
        rect = new RectF(0, 0, w, h);

        if (radius == 0) {
            setRadius(DEFAULT_RADIUS, false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    public void setRadius(float radius) {
        setRadius(radius, true);
    }

    private void setRadius(float radius, boolean invalidate) {
        this.radius = getResources().getDisplayMetrics().density * radius;
        if (invalidate)
            invalidate();
    }
}
