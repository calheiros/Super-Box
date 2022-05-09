package com.jefferson.application.br;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.widget.*;
//import com.facebook.drawee.backends.pipeline.*;
//import com.facebook.imagepipeline.common.*;
//import com.facebook.imagepipeline.core.*;
//import com.facebook.imagepipeline.request.*;
import com.jefferson.application.br.widget.*;

import android.support.v4.app.Fragment;
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.facebook.drawee.components.*;
import android.view.ScaleGestureDetector.*;
import android.view.View.*;

public class ImageDetailFragment extends Fragment implements OnTouchListener {

	JPhotoView mImageView;
	private static String IMAGE_DATA_EXTRA = "image_data";
    View view;
	private String mImageUri;
	private float mScaleFactor = 1.f;
	private ScaleGestureDetector mScaleDetector;
	//SimpleDraweeView draweeView;
	public static Fragment newInstance(String get) {
		final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, get);
        f.setArguments(args);

        return f;
	}

	public ImageDetailFragment() {

	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUri = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
		mScaleDetector = new ScaleGestureDetector(getContext(), new genericEvent());
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        // final View v = inflater.inflate(R.layout.view_image, container, false);
		/*SimpleDraweeView draweeView =(SimpleDraweeView) v.findViewById(R.id.my_image_view);
         draweeView.setImageURI(Uri.parse("file://" + mImageUri));
         draweeView.setOnTouchListener(this);*/
        return null;
    }
    @Override
	public boolean onTouch(View view, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//new loadImage(mImageUrl, mImageView, getContext()).execute();

	}
	private class genericEvent extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			if  (mScaleFactor > 3)
				mScaleFactor = 3f;
			if  (mScaleFactor < 1)
				mScaleFactor = 1f;

//			draweeView.setScaleX(mScaleFactor);
//			draweeView.setScaleY(mScaleFactor);
			return true;
		}

	}
	public class loadImage extends AsyncTask<Void, Void, Bitmap> {   
	    JPhotoView imageView;
		String path;
		Context context;
        ProgressDialog progD;
		ProgressBar mProgress;
        public loadImage(String path, JPhotoView imageView, Context context) {
			this.path = path;
			this.imageView = imageView;
            this.context = context;
			//mProgress = (ProgressBar)view.findViewById(R.id.viewImageProgress);
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			imageView.setImageBitmap(result);
			mProgress.setVisibility(View.GONE);

		}

		@Override
		protected Bitmap doInBackground(Void[] p1) {
			Bitmap bmp = null;
			try {   
				bmp = CodeManager.getResizedBitmap(1000, path);

				ExifInterface exif = new ExifInterface(path);
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				Log.d("EXIF", "Exif: " + orientation);
				Matrix matrix = new Matrix();

				if  (orientation == 6) {
					matrix.postRotate(90);
				} else if (orientation == 3) {
					matrix.postRotate(180);
				} else if (orientation == 8) {
					matrix.postRotate(270);
				}
				bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true); 

			} catch (OutOfMemoryError e) {

			} catch (Exception e) {

			}
		   	return bmp;
		}
	}
}
