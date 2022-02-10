package com.jefferson.application.br.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import com.jefferson.application.br.R;
import java.io.File;

public class VideoPlayFragment extends Fragment {

    private View view;
    private VideoView mVideoView;
    private String videoPath;
    private boolean autoplay;
    private MediaController mediaController;

    public VideoPlayFragment(String videoPath) {

        this.videoPath = videoPath;
        this.autoplay = false;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.video_view_fragment, null);
            mVideoView = view.findViewById(R.id.video_view);

            File file = new File(videoPath);
            if (!file.exists()) {
                Toast.makeText(getContext(), "File does not exists " + videoPath, 1).show();
                return view;
            }
            
            mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(mVideoView);
            mVideoView.setVideoURI(Uri.parse(file.getAbsolutePath()));
            mVideoView.setMediaController(mediaController);
            mVideoView.setOnCompletionListener( 

                new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.start();
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

            if (autoplay) {
                resume();
            }
        }

        return view;
    }

    public void resume() {

        if (mVideoView != null) {
            mVideoView.start();
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
