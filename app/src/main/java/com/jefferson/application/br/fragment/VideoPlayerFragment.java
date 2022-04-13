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
import com.jefferson.application.br.util.JDebug;
import com.bumptech.glide.Glide;
import com.jefferson.application.br.ui.JVideoController;

public class VideoPlayerFragment extends Fragment implements OnClickListener, JVideoController.OnButtonPressedListener {

    @Override
    public void onPressed(boolean playing) {

        if (videoNotPrepared) {
            videoNotPrepared = false;
            prepare();
        }

        hideThumbView();
    }

    private boolean videoNotPrepared;
    private View parentView;
    private VideoView mVideoView;
    private String videoPath;
    private MediaController mediaController;
    private boolean playOnCreate;
    private ImageView mThumbView;
    private JVideoController jController;
    private boolean selected = false;
    public VideoPlayerFragment(String videoPath) {
        this.videoPath = videoPath;
    }

    public void showThumbView() {

        if (mThumbView != null && mThumbView.getVisibility() != View.VISIBLE) {
            mThumbView.setVisibility(View.VISIBLE);
        }
    }

    public void setPlayOnCreate(boolean autoplay) {
        this.playOnCreate = autoplay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (parentView == null) {
            videoNotPrepared = true;
            parentView = inflater.inflate(R.layout.video_view_fragment, null);
            mVideoView = parentView.findViewById(R.id.video_view);
            mThumbView = parentView.findViewById((R.id.video_view_fragment_thumb_view));
            //playButton = parentView.findViewById(R.id.video_view_play_button);
            File file = new File(videoPath);

            if (!file.exists()) {
                Toast.makeText(getContext(), "File does not exists " + videoPath, 1).show();
                return parentView;
            }
            //loadThumbnail(videoPath);
            //Glide.with(this).load(file).into(mThumbView);

            jController = new JVideoController(mVideoView);
            jController.setAnchor((ViewGroup)parentView);
            jController.setOnButtonPressedListener(this);

            //mediaController = new MediaController(getActivity());
            //mediaController.setAnchorView(parentView);
            //mVideoView.setMediaController(mediaController);
            jController.prepare();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

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
        }
        Glide.with(this).load("file://" + videoPath).into(mThumbView);
        return parentView;
    }
   
    @Override
    public void onDestroy() {
        super.onDestroy();
        //JDebug.toast("Destroyed");
        try {
            stop();
            if (jController != null) {
               if (jController.alive()) {
                   jController.pause();
                   JDebug.toast("Controller still alive");
               } else {
                   JDebug.toast("Controller is NOT alive!");
               }
            } else {
                JDebug.toast("Controller is NULL!");
            }
        } finally {

        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onResume() {
        super.onResume();
        //JDebug.toast("Resumed " + selected);
    }

    @Override
    public void onClick(View view) {
        startVideo();
    }

    private void hideThumbView() {

        if (mThumbView != null && mThumbView.getVisibility() != View.GONE) {
            mThumbView.setVisibility(View.GONE);
        }
    }

    public void prepare() {
        if (mVideoView != null) {
            mVideoView.setVideoURI(Uri.parse(videoPath));
            //mVideoView.start();
        }
    }

    public void startVideo() {
        hideThumbView();

        if (mVideoView != null) {
            mVideoView.setVideoURI(Uri.parse(videoPath));
            mVideoView.start();
        }
    }

    @Override
    public void onPause() {

        if (mVideoView != null) {
            stop();
        }
        
        super.onPause();
    }

    public void stop() {
        showThumbView();

        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
            videoNotPrepared = true;
        } 
        
        if (jController != null && jController.alive()){
            jController.pause();
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
