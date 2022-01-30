package com.jefferson.application.br.activity;

import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;

import java.io.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class VideoPlayerActivity extends MyCompatActivity {
	
	private VideoView mVideoView;
	private Handler controllerHandler;
	private SeekBar mSeek;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		winParams.flags |=  bits;
        win.setAttributes(winParams);

		setContentView(R.layout.view_video);
		mVideoView = (VideoView) findViewById(R.id.my_video_view);
        mSeek = (SeekBar) findViewById(R.id.video_progress);

		Intent i = getIntent();
	    int position = i.getExtras().getInt("position");

		ArrayList<String> filepath = i.getStringArrayListExtra("filepath");
        controllerHandler = new Handler();

		File file = new File(filepath.get(position));
	
		mVideoView.setVideoURI(Uri.parse(file.getAbsolutePath()));
		mVideoView.requestFocus();
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.start();

		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
			{
				@Override
				public void onCompletion(MediaPlayer mp) {
				 mp.start();
				}
			});
		mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){

				@Override
				public boolean onError(MediaPlayer p1, int p2, int p3) {
					return false;
				}
			}); 
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }
    
}
