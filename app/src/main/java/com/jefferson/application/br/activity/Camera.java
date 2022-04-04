package com.jefferson.application.br.activity;

import android.hardware.*;
import android.hardware.Camera.*;
import android.media.*;
import android.os.*;
import android.support.v7.app.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.jefferson.application.br.*;
import java.io.*;
import java.util.*;

import android.hardware.Camera;
import org.w3c.dom.*;
import android.support.v4.provider.*;
import com.jefferson.application.br.util.Storage;

public class Camera extends MyCompatActivity implements SurfaceHolder.Callback {
    SurfaceHolder holder;
	SurfaceView surface;
    Camera mCamera;
	MediaRecorder mRecorder;
	Button mButton;
	boolean recording = false;
	Camera.Parameters parameters;
    Camera.PictureCallback call = new Camera.PictureCallback(){

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

		}
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		surface = (SurfaceView)findViewById(R.id.surfaceView);
		mButton = (Button)findViewById(R.id.take);
        mCamera = Camera.open();

		holder = surface.getHolder();
        holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					if (recording) {
						mRecorder.stop();
						mRecorder.release();
						recording = false;
					} else {
						recording = true;
					    mRecorder.start();
					}
				}
			});
	}
	private void releaseCamera() {
		if (mCamera != null) {
	        mCamera.lock();
			mCamera.release();
			mCamera = null;
		}
	}
    
	private void initRecorder() {
		mRecorder = new MediaRecorder();
		mCamera.unlock();
		mRecorder.setCamera(mCamera);
        mRecorder.setPreviewDisplay(holder.getSurface());

		mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile cpHigh = CamcorderProfile
			.get(CamcorderProfile.QUALITY_HIGH);
        mRecorder.setProfile(cpHigh);
        mRecorder.setOutputFile("/sdcard/videocapture_example.mp4");
        mRecorder.setMaxDuration(50000); 
        mRecorder.setMaxFileSize(15000000); 
		File file = new File(Storage.getExternalStorage());

		Toast.makeText(getApplicationContext(), Long.toString(file.getFreeSpace() / (1024 * 1024)) + " mb", 1).show();
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener(){

				@Override
				public void onError(MediaRecorder mr, int p2, int p3) {
					Toast.makeText(getApplicationContext(), "error", 1).show();
				}
			});
		try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mRecorder.release();
        } catch (IOException e) {
            e.printStackTrace();
            mRecorder.release();
        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder mholder) {
		try {
			mCamera.setPreviewDisplay(mholder);
		} catch (IOException e) {}
	}

	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int width, int height) {
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}

	    parameters = mCamera.getParameters();
		Size size = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), height, width);

        parameters.setPreviewSize(size.width, size.height);
	    parameters.getSupportedPreviewSizes();
		mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
        mCamera.startPreview(); 
		initRecorder();
    }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (recording) {
			mRecorder.stop();
			recording = false;
		}
		mRecorder.release();
		releaseCamera();

	}
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
