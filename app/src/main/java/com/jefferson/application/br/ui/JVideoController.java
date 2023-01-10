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

package com.jefferson.application.br.ui;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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
    private OnButtonPressedListener onPlayButtonPressedListener;
    private Boolean ButtonStatePlaying = false;

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
        init_();
    }

    public JVideoController(VideoView video) {
        this.mVideoView = video;
        init_();
    }

    public void setAnchor(ViewGroup view) {
        this.anchorView = view;
    }

    public boolean isControllerActive() {
        return timer != null && timerTask != null;
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
            setPlaying(false);

            if (controllerView.getVisibility() != View.VISIBLE) {
                showController(true);
            }
        } else {
            mVideoView.start();
            setPlaying(true);
            hideDelayed(1000);
        }
    }

    public void pause() {
        //JDebug.toast("Controller paused");

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        setButtonStatePlaying(false);
    }

    private void resume() {
        //JDebug.toast("Controller resumed!");

        if (handler == null) {
            handler = new MyHandler();
        }

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timerTask = new MyTask();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 100);
    }

    public void init_() {
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
        createControllerView();
    }

    public void hideDelayed(int millis) {
        if (controllerHandler == null) {
            return;
        }
        controllerHandler.postDelayed(controllerRunnable, millis);
    }

    public void destroy() {

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        handler = null;
        controllerView = null;
        JDebug.toast("Destroyed!");
    }

    public void setOnButtonPressedListener(OnButtonPressedListener listener) {
        this.onPlayButtonPressedListener = listener;
    }

    private void createControllerView() {
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
        controllerButton.setOnClickListener(this);
        
        if (mVideoView.isPlaying()) {
            setPlaying(true);
        }
    }

    private void showController(boolean show) {
        controllerView.setVisibility(show ? View.VISIBLE: View.INVISIBLE);
        controllerView.startAnimation(show ? animFadeIn : animFadeOut);
    }

    private void setPlaying(boolean playing) {
        // setButtonStatePlaying(playing);

        if (playing) {
            resume();
        } else {
            pause();
        }
    }

    private void setButtonStatePlaying(boolean state) {
        if (controllerButton != null) {
            int resId = state ? R.drawable.ic_video_pause : R.drawable.ic_video_play;
            controllerButton.setImageResource(resId);
            this.ButtonStatePlaying = state;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int postion, boolean tr) {
        //do nothing
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
        tracking = true;
        controllerHandler.removeCallbacks(controllerRunnable);

        if (controllerView.getVisibility() != View.VISIBLE) {
            showController(true);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        hideDelayed(2000);
        mVideoView.seekTo(seekbar.getProgress());
        tracking = false;
    }

    private String max;
    
    public class MyHandler extends Handler {

        private boolean playing;

        @Override 
        public void dispatchMessage(Message message) {

            if (playing = mVideoView.isPlaying()) {

                if (duration == -1) {
                    duration = mVideoView.getDuration();
                    mSeekBar.setVisibility(View.VISIBLE);
                    mSeekBar.setMax(duration);
                    max = StringUtils.getFormattedVideoDuration(String.valueOf(duration));
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
                String current = StringUtils.getFormattedVideoDuration(String.valueOf(position));
                startTextView.setText(current);
            }

            if (playing != ButtonStatePlaying) {
                setButtonStatePlaying(playing);
            }
        }
    }

    public class MyTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public static interface OnButtonPressedListener {
        void onPressed(boolean playing);
    }
}
