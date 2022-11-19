package com.jefferson.application.br;

import android.graphics.*;
import android.os.*;
import java.io.*;
import java.text.*;
import java.util.*;
import android.content.*;

public class SaveImage extends AsyncTask<Void,Void,Void>
{
	Bitmap bmp;
	Context context;
	public SaveImage(Bitmap bmp, Context context)
	{
		this.bmp = bmp;
		this.context = context;
	}
	@Override
	protected Void doInBackground(Void[] p1)
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File file = new File(Environment.getExternalStorageDirectory() + "/.application/data/Intruder", timeStamp + ".jpg");

		try
		{
			FileOutputStream out = new FileOutputStream(file);
			Canvas canvas = new Canvas(bmp);

			Paint paint = new Paint();
			paint.setColor(Color.WHITE); // Text Color
			paint.setStrokeWidth(context.getResources().getDisplayMetrics().density * 12); // Text Size
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
			// some more settings...

			canvas.drawBitmap(bmp, 0, 0, paint);
			canvas.drawText("Testing...", 10, 10, paint);
			// NEWLY ADDED CODE ENDS HERE ]

			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

}
