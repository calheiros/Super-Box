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
package com.jefferson.application.br.fragment

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.switcher.ViewVisibilitySwitch
import com.jefferson.application.br.ui.JVideoController
import com.jefferson.application.br.ui.JVideoController.OnButtonPressedListener
import java.io.File

class VideoPlayerFragment(
    private val videoPath: String,
    private val optionsTrigger: ViewVisibilitySwitch
) : Fragment(), View.OnClickListener, OnButtonPressedListener {

    private var videoNotPrepared = false
    private var parentView: View? = null
    private var videoView: VideoView? = null
    private var thumbView: ImageView? = null
    private var controller: JVideoController? = null

    override fun onPressed(playing: Boolean) {
        if (videoNotPrepared) {
            videoNotPrepared = false
            prepare()
        }
        hideThumbView()
    }

    private fun showThumbnail() {
        if (thumbView?.visibility != View.VISIBLE) {
            thumbView?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        if (parentView == null) {
            videoNotPrepared = true
            parentView = inflater.inflate(R.layout.video_view_fragment, container, false)
            videoView = parentView?.findViewById(R.id.video_view)
            thumbView = parentView?.findViewById(R.id.video_view_fragment_thumb_view)
            val file = File(videoPath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exists $videoPath", Toast.LENGTH_SHORT)
                    .show()
                return parentView
            }
            controller = JVideoController(videoView, parentView as ViewGroup?, optionsTrigger)
            controller?.setOnButtonPressedListener(this)
            videoView?.setOnPreparedListener { mp: MediaPlayer ->
                mp.isLooping = true
            }
            videoView?.setOnErrorListener{ _: MediaPlayer?, _: Int, _: Int ->
                Toast.makeText(context, getString(R.string.falha_video), Toast.LENGTH_LONG).show()
                true
            }
        }
        Glide.with(requireContext()).load("file://$videoPath").into(thumbView!!)
        return parentView
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        if (controller?.isControllerActive == true) {
            controller?.pause()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.video_view -> {
                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startVideo()
    }

    private fun hideThumbView() {
        if (thumbView != null && thumbView?.visibility != View.GONE) {
            thumbView?.visibility = View.GONE
            thumbView?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
        }
    }

    private fun prepare() {
        if (videoView != null) {
            videoView?.setVideoURI(Uri.parse(videoPath))
            //mVideoView.start();
        }
    }

    private fun startVideo() {
        hideThumbView()
        if (videoView != null) {
            videoView?.setVideoURI(Uri.parse(videoPath))
            videoView?.start()
        }
    }

    override fun onPause() {
        stop()
        super.onPause()
    }

    private fun stop() {
        showThumbnail()
        if (videoView?.isPlaying == true) {
            videoView?.stopPlayback()
            videoNotPrepared = true
        }
        if (controller?.isControllerActive == true) {
            controller?.pause()
        }
    }

}