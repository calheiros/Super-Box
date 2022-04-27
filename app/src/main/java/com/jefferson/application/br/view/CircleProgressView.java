package com.jefferson.application.br.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.jefferson.application.br.R;

public class CircleProgressView extends View {

    private float progress;
    private RectF rect;
    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private int padding;

    private float progressWidth;
    
    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        defineVariables();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(Color.BLACK);
        canvas.drawArc(rect, 144, 252, false, backgroundPaint);
        canvas.drawArc(rect, 144, progress * 2.52f , false, progressPaint);
        //draw progress text
        RectF bounds = new RectF(rect);
        // measure text width
        String progressText = String.valueOf(Math.round(progress));
        bounds.right = textPaint.measureText(progressText, 0, progressText.length()); 
        // measure text height 
        bounds.bottom = textPaint.descent() - textPaint.ascent();
        bounds.left += (rect.width() - bounds.right) / 2.0f; bounds.top += (rect.height() - bounds.bottom) / 2.0f; 
        
        canvas.drawText(progressText, bounds.left, bounds.top - textPaint.ascent(), textPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resizeView(w,h);
    }
    
    private int getAttrColor(int res) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(res, typedValue, true);
        int color = typedValue.data;
        return color;
    }
    
    private void defineVariables() {
        int colorAccent = getAttrColor(R.attr.colorAccent);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new RectF();
        
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(colorAccent);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setAlpha(55);
        
        progressPaint.setColor(colorAccent);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStyle(Paint.Style.STROKE);
      
        textPaint.setColor(getAttrColor(R.attr.commonColorLight));
    }
    
    private void resizeView(int w, int h) {
        padding = w / 16;
        progressWidth = padding * 1.5f;
        backgroundPaint.setStrokeWidth(progressWidth);
        progressPaint.setStrokeWidth(progressWidth);
        textPaint.setTextSize(w / 4);
        rect.set(padding, padding, w - padding, h - padding);
    }
    
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
    
    public float getProgress() {
        return progress;
    }
}
