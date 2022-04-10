package com.jefferson.application.br.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;
import com.jefferson.application.br.R;
import java.util.Timer;
import java.util.TimerTask;
import com.jefferson.application.br.util.StringUtils;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;

public class JVideoController implements OnSeekBarChangeListener, OnClickListener, OnTouchListener {

    private View controllerView;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            boolean show = (controllerView.getVisibility() != View.VISIBLE);
            showController(show);
        }
        return false;
    }
    private MediaPlayer mediaPlayer;
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
    private ImageView controllerButton;
    private boolean paused;
    private OnPlayButtonPressedListener onPlayButtonPressedListener;

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

    private void showController(boolean show) {
        controllerView.setVisibility(show ?View.VISIBLE: View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        boolean isPlaying = mVideoView.isPlaying();
        
        if (onPlayButtonPressedListener != null) {
            onPlayButtonPressedListener.onPressed(isPlaying);
        }
        
        if (isPlaying) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
    }

    public void prepare(MediaPlayer mp) {
        mediaPlayer = mp;
        handler = new MyHandler();
        timerTask = new MyTask();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 100);
        createControllerView();
    }

    public void destroy() {

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }
    }

    public void setOnPlayButtonPressed(OnPlayButtonPressedListener listener) {
        this.onPlayButtonPressedListener = listener;
    }

    private void createControllerView() {
        if (mVideoView != null) {
            Context context = mVideoView.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            controllerView = inflater.inflate(R.layout.jvideo_controller_layout, null);
            anchorView.addView(controllerView);
            mSeekBar = controllerView.findViewById(R.id.jvideo_controller_seekbar);
            textView = controllerView.findViewById(R.id.jvideocontrollerlayoutTextView);
            controllerButton = controllerView.findViewById(R.id.jcontroller_view_button);
            mSeekBar.setOnSeekBarChangeListener(this);
            anchorView.setOnTouchListener(this);
            setButtonPaused(!mVideoView.isPlaying());
            controllerButton.setOnClickListener(this);
        }
    }

    private void setButtonPaused(boolean set) {
        int resId = set ? R.drawable.ic_video_play : R.drawable.ic_video_pause;
        controllerButton.setImageResource(resId);
        this.paused = set;
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

        private String max;


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
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        mSeekBar.setProgress(position, true);
                    } else {
                        mSeekBar.setProgress(position);
                    }
                }
                if (max == null)
                    max = StringUtils.getFormatedTime(String.valueOf(duration));
                String current = StringUtils.getFormatedTime(String.valueOf(position));
                textView.setText(current + " / " + max);
            }
            setButtonPaused(!mVideoView.isPlaying());
        }
    }

    public class MyTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public static interface OnPlayButtonPressedListener {
        void onPressed(boolean playing)
    }
}
