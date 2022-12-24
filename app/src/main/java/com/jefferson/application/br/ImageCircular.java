package com.jefferson.application.br;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import android.widget.*;

public class ImageCircular extends androidx.appcompat.widget.AppCompatImageView {

    public ImageCircular(Context context) {
        super(context);
    }

    public ImageCircular(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageCircular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth(), h = getHeight();

        Bitmap roundBitmap =CodeManager.getCroppedBitmap(bitmap);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

     }
	}
