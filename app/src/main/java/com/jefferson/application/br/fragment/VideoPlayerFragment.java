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

package com.jefferson.application.br.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.jefferson.application.br.R;
import com.jefferson.application.br.ui.JVideoController;

import java.io.File;

public class VideoPlayerFragment extends Fragment implements OnClickListener, JVideoController.OnButtonPressedListener {

    private boolean videoNotPrepared;
    private View parentView;
    private VideoView mVideoView;
    private final String videoPath;
    private MediaController mediaController;
    private ImageView mThumbView;
    private JVideoController jController;

    public VideoPlayerFragment(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public void onPressed(boolean playing) {

        if (videoNotPrepared) {
            videoNotPrepared = false;
            prepare();
        }

        hideThumbView();
    }

    public void showThumbView() {

        if (mThumbView != null && mThumbView.getVisibility() != View.VISIBLE) {
            mThumbView.setVisibility(View.VISIBLE);
        }
    }

    public void setPlayOnCreate(boolean autoplay) {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (parentView == null) {
            videoNotPrepared = true;
            parentView = inflater.inflate(R.layout.video_view_fragment, container, false);
            mVideoView = parentView.findViewById(R.id.video_view);
            mThumbView = parentView.findViewById((R.id.video_view_fragment_thumb_view));

            File file = new File(videoPath);

            if (!file.exists()) {
                Toast.makeText(getContext(), "File does not exists " + videoPath, Toast.LENGTH_SHORT).show();
                return parentView;
            }

            jController = new JVideoController(mVideoView, (ViewGroup) parentView);
            jController.setOnButtonPressedListener(this);
            mVideoView.setOnPreparedListener(mp -> mp.setLooping(true));

            mVideoView.setOnErrorListener(
                    (mp, p2, p3) -> {
                        Toast.makeText(getContext(), getString(R.string.falha_video), Toast.LENGTH_LONG).show();
                        return true;
                    }
            );
        }
        Glide.with(requireContext()).load("file://" + videoPath).into(mThumbView);
        return parentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stop();
        if (jController != null && jController.isControllerActive()) {
            jController.pause();
        }
    }

    public void setSelected(boolean selected) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        startVideo();
    }

    private void hideThumbView() {

        if (mThumbView != null && mThumbView.getVisibility() != View.GONE) {
            mThumbView.setVisibility(View.GONE);
            mThumbView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
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
        stop();
        super.onPause();
    }

    public void stop() {
        showThumbView();

        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
            videoNotPrepared = true;
        }

        if (jController != null && jController.isControllerActive()) {
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
