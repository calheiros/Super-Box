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
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import com.jefferson.application.br.util.JDebug;

public class JVideoController implements OnSeekBarChangeListener, OnClickListener, OnTouchListener {

    private View controllerView;
    private Animation animFadeIn;
    private Animation animFadeOut;
    private TextView endTextView;

    private Handler handler;
    private Handler controllerHandler;
    private Runnable controllerRunnable;
    
    private MediaPlayer mediaPlayer;
    private boolean tracking;
    public static final String TAG = "JVideoController";
    private VideoView mVideoView;
    private MyTask timerTask;
    private SeekBar mSeekBar;
    private ViewGroup anchorView = null;
    private Timer timer;
    private int duration = -1;
    private TextView startTextView;
    private ImageView controllerButton;
    // private boolean paused;
    private OnPlayButtonPressedListener onPlayButtonPressedListener;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        boolean invisible = (controllerView.getVisibility() != View.VISIBLE);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            controllerHandler.removeCallbacks(controllerRunnable);
            showController(invisible);
            return true;
        } else if (event.getAction() == event.ACTION_UP) {
            if (!invisible) {
                hideDelayed(2000);
            }
            //JDebug.toast("ACTION_UP");
        }
        return false;
    }
    
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

    @Override
    public void onClick(View view) {
        controllerHandler.removeCallbacks(controllerRunnable);
        boolean isPlaying = mVideoView.isPlaying();

        if (onPlayButtonPressedListener != null) {
            onPlayButtonPressedListener.onPressed(isPlaying);
        }

        if (isPlaying) {
            mVideoView.pause();
            if (controllerView.getVisibility() != View.VISIBLE) {
                showController(true);
            }
        } else {
            mVideoView.start();
            hideDelayed(1000);
        }
    }

    public void prepare(MediaPlayer mp) {
        mediaPlayer = mp;
        animFadeIn = AnimationUtils.loadAnimation(anchorView.getContext(), R.anim.jcontroller_fade_in);
        animFadeOut = AnimationUtils.loadAnimation(anchorView.getContext(), R.anim.jcontroller_fade_out);
        controllerRunnable = new Runnable() {

            @Override
            public void run() {
                if (controllerView != null && controllerView.getVisibility() == View.VISIBLE) {
                    showController(false);
                }
            }
        };
        controllerHandler = new Handler();

        if (handler == null) {
            handler = new MyHandler();
        }

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }

        timerTask = new MyTask();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 100);

        if (controllerView == null) {
            createControllerView();
        }
    }

    public void hideDelayed(int millis) {
        controllerHandler.postDelayed(controllerRunnable, millis);
    }
    
    public void destroy() {

        if (timer != null) {
            timer.cancel();
        }

        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = null;
        timer = null;
        handler = null;
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
            startTextView = controllerView.findViewById(R.id.jcontroller_start_TextView);
            endTextView = controllerView.findViewById(R.id.jcontroller_end_TextView);
            controllerButton = controllerView.findViewById(R.id.jcontroller_view_button);
            mSeekBar.setOnSeekBarChangeListener(this);
            anchorView.setOnTouchListener(this);
            toogleButton(!mVideoView.isPlaying());
            controllerButton.setOnClickListener(this);
        }
    }

    private void showController(boolean show) {
        controllerView.setVisibility(show ? View.VISIBLE: View.INVISIBLE);
        controllerView.startAnimation(show ? animFadeIn : animFadeOut);
    }

    private void toogleButton(boolean set) {
        int resId = set ? R.drawable.ic_video_play : R.drawable.ic_video_pause;
        controllerButton.setImageResource(resId);
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int postion, boolean tr) {
             //do nothing
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
        tracking = true;
        controllerHandler.removeCallbacks(controllerRunnable);
        
        if (controllerView.getVisibility() != View.VISIBLE){
            showController(true);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        hideDelayed(2000);
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
                    mSeekBar.setVisibility(View.VISIBLE);
                    mSeekBar.setMax(duration);
                    max = StringUtils.getFormatedTime(String.valueOf(duration));
                    endTextView.setText(max);
                }

                int position = mVideoView.getCurrentPosition();
                //int progress = (int)(100 - (((double)duration - (double)position) * (100 / (double)duration)));

                if (!tracking) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        mSeekBar.setProgress(position, true);
                    } else {
                        mSeekBar.setProgress(position);
                    }
                }
                String current = StringUtils.getFormatedTime(String.valueOf(position));
                startTextView.setText(current);
            }
            toogleButton(!mVideoView.isPlaying());
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
