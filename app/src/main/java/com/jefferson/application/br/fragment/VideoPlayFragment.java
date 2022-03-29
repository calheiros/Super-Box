package com.jefferson.application.br.fragment;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.Debug;
import java.io.File;
import android.view.TouchDelegate;
import android.graphics.Rect;

public class VideoPlayFragment extends Fragment implements OnTouchListener {

    private View parentView;
    private VideoView mVideoView;
    private String videoPath;
    private MediaController mediaController;

    private boolean playOnCreate;

    public VideoPlayFragment(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setPlayOnCreate(boolean autoplay) {
        this.playOnCreate = autoplay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (parentView == null) {
            parentView = inflater.inflate(R.layout.video_view_fragment, null);
            mVideoView = parentView.findViewById(R.id.video_view);

            File file = new File(videoPath);
            if (!file.exists()) {
                Toast.makeText(getContext(), "File does not exists " + videoPath, 1).show();
                return parentView;
            }

            mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(parentView);
            mVideoView.setMediaController(mediaController);
            mVideoView.setOnPreparedListener(

                new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                }
            );

            mVideoView.setOnErrorListener(

                new MediaPlayer.OnErrorListener(){

                    @Override
                    public boolean onError(MediaPlayer mp, int p2, int p3) {

                        Toast.makeText(getContext(), "This video can not be played on this device using android API!", Toast.LENGTH_LONG).show();
                        mp.release();

                        return true;
                    }
                }
            );
            
            if (playOnCreate) {
                start();
            }
            
            Rect delegateArea = new Rect();
            parentView.getHitRect(delegateArea);
            Debug.toast("top " + delegateArea.top + "\nbottom " + delegateArea.bottom);
            TouchDelegate touchDelegate = new TouchDelegate(delegateArea, mVideoView);
            mVideoView.setTouchDelegate(touchDelegate);
        }

        return parentView;
    }

    @Override
    public boolean onTouch(View vi, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mediaController.isShowing()) {
                mediaController.hide();
            } else {
                mediaController.show();
            }
            return true;
        }
        return false;
    }


    public void start() {
        new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    if (mVideoView != null) {
                        mVideoView.setVideoURI(Uri.parse(videoPath));
                        mVideoView.start();
                        //mVideoView.requestFocus();
                    }
                }
            }, 100);

    }

    public void stop() {

        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        if (mediaController != null) {
            mediaController.hide();

        }
    }

    public void resume() {

        if (mVideoView != null) {
            mVideoView.resume();
            mVideoView.requestFocus();
        }
    }

    public void pause() {

        if (mVideoView != null) {
            mVideoView.pause();
        }
        /*
         if (mediaController != null) {
         mediaController.hide();
         }
         */
    }

    
    
}
