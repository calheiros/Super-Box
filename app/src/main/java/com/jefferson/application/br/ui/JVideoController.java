package com.jefferson.application.br.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;
import com.jefferson.application.br.R;
import java.util.Timer;
import java.util.TimerTask;

public class JVideoController implements MediaPlayer.OnPreparedListener, OnSeekBarChangeListener {

    private boolean tracking;
    public static final String TAG = "JVideoController";
    private VideoView mVideoView;
    private MyHandler handler;
    private MyTask timerTask;
    private SeekBar mSeekBar;
    private ViewGroup anchorView = null;
    private Timer timer;
    private int duration = -1;
    private TextView textView;

    public JVideoController(VideoView video, ViewGroup parentView) {
        this.mVideoView = video;
        this.anchorView = parentView;
    }

    public JVideoController(VideoView video) {
        this.mVideoView = video;
    }

    public void setAnchor(ViewGroup view) {
        this.anchorView = view;
    }

    private void start() {
        
    }

    public void prepare() {
        handler = new MyHandler();
        timerTask = new MyTask();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 100);
        createControllerView();
        mVideoView.setOnPreparedListener(this);
    }

    public void destroy() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
    }

    private void createControllerView() {
        if (mVideoView != null) {
            Context context = mVideoView.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.jvideo_controller_layout, anchorView);
            mSeekBar = anchorView.findViewById(R.id.jvideo_controller_seekbar);
            textView = anchorView.findViewById(R.id.jvideocontrollerlayoutTextView);
            mSeekBar.setOnSeekBarChangeListener(this);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int postion, boolean tr) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
        tracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        mVideoView.seekTo(seekbar.getProgress());
        tracking = false;
    }

    public class MyHandler extends Handler {


        @Override 
        public void dispatchMessage(Message message) {

            if (mVideoView.isPlaying()) {
                if (duration == -1) {
                    duration = mVideoView.getDuration();
                    mSeekBar.setMax(duration);
                }
                int position = mVideoView.getCurrentPosition();
                int progress = (int)(100 - (((double)duration - (double)position) * (100 / (double)duration)));
                if (!tracking) {
                    mSeekBar.setProgress(position, true);
                }
                textView.setText("Progress " + position + " | duration " + duration + " | percentage " + progress + "%" );
            } else {
                //JDebug.toast("Video is not playing!");
            }

        }
    }

    public class MyTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }
}
