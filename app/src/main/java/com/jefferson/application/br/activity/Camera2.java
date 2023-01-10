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

package com.jefferson.application.br.activity;

/*
import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import com.jefferson.application.br.*;
import com.otaliastudios.cameraview.*;
import android.support.design.widget.FloatingActionButton;
import com.jefferson.application.br.R;
import android.view.View.*;
import android.widget.*;
(%÷
public class Camera2 extends AppCompatActivity
{
	public static final String IMAGE_PATH = "/application/data/imagens";
	CameraView cameraView;
	FloatingActionButton mfab;
	CameraListener cameraListener = new CameraListener() {

		@Override
		public void onPictureTaken(byte[] jpeg)
		{
			super.onPictureTaken(jpeg);
			
			String secondary_sd = System.getenv("SECONDARY_STORAGE");
			Toast.makeText(getApplicationContext(),secondary_sd,1).show();

		}
	 
	};
	@Override
	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
	
		setContentView(R.layout.camera2);
		cameraView = (CameraView) findViewById(R.id.camera);
	    mfab = (FloatingActionButton)findViewById(R.id.camera_fab);
		
		cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
		cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // tap to focus
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		cameraView.setCameraListener(cameraListener);
	    
		mfab.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view)
				{
			      cameraView.capturePicture();
				}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		cameraView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cameraView.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraView.destroy();
	}
}*/
