package com.jefferson.application.br.util;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import com.jefferson.application.br.*;
import java.io.*;

public class PicturesUtils
{
	public static void load(ImageView imageView, String path)
	{
      new AsyncTaskImage(path,imageView).execute();
	}
}

class AsyncTaskImage extends AsyncTask<Integer,Void,Bitmap>
{
	String path;
	ImageView imageView;

    public AsyncTaskImage(String path, ImageView image)
	{
		this.path = path;
		this.imageView = image;
	}

	@Override
	protected Bitmap doInBackground(Integer... params)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		bitmap = CodeManager.getResizedBitmap( 1400, path);

		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result)
    {
		super.onPostExecute(result);
		imageView.setImageBitmap(result);
	}

}
