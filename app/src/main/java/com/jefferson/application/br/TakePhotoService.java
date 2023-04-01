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

package com.jefferson.application.br;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Environment;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TakePhotoService extends Service implements SurfaceHolder.Callback {

    //a variable to store a reference to the Image View at the main.xml file
    //a variable to store a reference to the Surface View at the main.xml file
    private SurfaceView sv;
    //a bitmap to display the captured image
    private Bitmap bmp;
    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Parameters parameters;
    private WindowManager wm;
    private View vi;
    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            wm.removeView(vi);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(Environment.getExternalStorageDirectory() + "/.application/data/Intruder", timeStamp + ".jpg");
            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            // formattedDate have current date/time
            try {
                FileOutputStream out = new FileOutputStream(file);
                Canvas canvas = new Canvas(bmp);

                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(12);
                paint.setTextSize(80);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                canvas.drawBitmap(bmp, 0, 0, paint);
                canvas.drawText(formattedDate, 100, 100, paint);

                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();

            }
            stopSelf();

        }
    };
    private int cameraId;

    @Override
    public IBinder onBind(Intent p1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {

        super.onCreate();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1,
                1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.UNKNOWN);

        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        vi = LayoutInflater.from(this)
                .inflate(R.layout.camera_surface, null);

        sv = vi.findViewById(R.id.surfaceView);
        sHolder = sv.getHolder();
        //add the callback interface methods defined below as the Surface View callbacks
        sHolder.addCallback(this);
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

        wm.addView(vi, params);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        //get camera parameters
        parameters = mCamera.getParameters();

        //set camera parameters
        mCamera.setParameters(parameters);
        mCamera.startPreview();

        //sets what code should be executed after the picture is taken

        mCamera.takePicture(null, null, mCall);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw the preview.
        mCamera = Camera.open(cameraId);

        try {
            mCamera.setPreviewDisplay(holder);

        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //stop the preview
        mCamera.stopPreview();
        //release the camera
        mCamera.release();
        //unbind the camera from this object
        mCamera = null;
    }
}
