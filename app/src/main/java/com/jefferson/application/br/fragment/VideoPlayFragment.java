package com.jefferson.application.br.fragment;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import com.jefferson.application.br.R;
import java.io.File;
import com.jefferson.application.br.util.Debug;

public class VideoPlayFragment extends Fragment implements OnTouchListener, OnClickListener {

    private View parentView;
    private VideoView mVideoView;
    private String videoPath;
    private MediaController mediaController;
    private boolean playOnCreate;
    private ImageView mThumbView;
    private View overlayView;
    private View playButton;
    private Bitmap bmp;
    
    public VideoPlayFragment(String videoPath) {
        this.videoPath = videoPath;
    }

    public void showVideoOverlay() {

        if (overlayView != null && overlayView.getVisibility() != View.VISIBLE) {
            overlayView.setVisibility(View.VISIBLE);
        }
    }

    public void setPlayOnCreate(boolean autoplay) {
        this.playOnCreate = autoplay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (parentView == null) {
            parentView = inflater.inflate(R.layout.video_view_fragment, null);
            mVideoView = parentView.findViewById(R.id.video_view);
            mThumbView = parentView.findViewById((R.id.video_view_fragment_thumb_view));
            overlayView = parentView.findViewById(R.id.video_view_overlay);
            playButton = parentView.findViewById(R.id.video_view_play_button);
            File file = new File(videoPath);

            if (!file.exists()) {
                Toast.makeText(getContext(), "File does not exists " + videoPath, 1).show();
                return parentView;
            }

            loadThumbnail(videoPath);
            overlayView.setOnClickListener(this);
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
                        Toast.makeText(getContext(), getString(R.string.falha_video), Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
            );

            if (playOnCreate) {
                startVideoReproduction();
            }
        }

        return parentView;
    }

    @Override 
    public void setUserVisibleHint(boolean isVisibleToUser) { 
        super.setUserVisibleHint(isVisibleToUser); 

        if (!isVisibleToUser) {
            showVideoOverlay();
            stop();
        }
    }

    private void loadThumbnail(final String path) {

        final Handler handler = new Handler() {
            public void 
            dispatchMessage(Message msf) {
                mThumbView.setImageBitmap(bmp);
            }
        };

        new Thread(){
            @Override
            public void run() {
                bmp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        startVideoReproduction();
    }

    private void hideVideoOverlay() {
        if (overlayView != null && overlayView.getVisibility() != View.GONE) {
            overlayView.setVisibility(View.GONE);
        }
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

    public void startVideoReproduction() {
        hideVideoOverlay();

        if (mVideoView != null) {
            mVideoView.setVideoURI(Uri.parse(videoPath));
            mVideoView.start();
            mVideoView.requestFocus();

        }
    }

    @Override
    public void onPause() {

        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                stop();
            } 
            showVideoOverlay();
        }

        super.onPause();
    }

    public void stop() {

        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }

        if (mediaController != null && mediaController.isShowing()) {
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

        if (mediaController != null) {
            mediaController.hide();
        }
    }
}
