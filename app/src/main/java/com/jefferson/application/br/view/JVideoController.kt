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
package com.jefferson.application.br.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.VideoView
import com.jefferson.application.br.R
import com.jefferson.application.br.switcher.ViewVisibilitySwitch
import com.jefferson.application.br.util.JDebug.toast
import com.jefferson.application.br.util.StringUtils.getFormattedVideoDuration
import java.util.*

class JVideoController : OnSeekBarChangeListener, View.OnClickListener {
    private val mVideoView: VideoView?
    private var visibilitySwitch: ViewVisibilitySwitch? = null
    private var controllerView: View? = null
    private var animFadeIn: Animation? = null
    private var animFadeOut: Animation? = null
    private var endTextView: TextView? = null
    private var handler: Handler? = null
    private var controllerHandler: Handler? = null
    private var controllerRunnable: Runnable? = null
    private var tracking = false
    private var timerTask: MyTask? = null
    private var mSeekBar: SeekBar? = null
    private var anchorView: ViewGroup? = null
    private var timer: Timer? = null
    private var duration = -1
    private var startTextView: TextView? = null
    private var controllerButton: ImageView? = null
    private var onPlayButtonPressedListener: OnButtonPressedListener? = null
    private var ButtonStatePlaying = false
    private var max: String? = null

    constructor(video: VideoView?, parentView: ViewGroup?) {
        mVideoView = video
        anchorView = parentView
        init()
    }

    constructor(video: VideoView?) {
        mVideoView = video
        init()
    }

    constructor(
        videoView: VideoView?, viewGroup: ViewGroup?,
        optionsTrigger: ViewVisibilitySwitch?
    ) {
        mVideoView = videoView
        anchorView = viewGroup
        visibilitySwitch = optionsTrigger
        init()
    }

    fun setAnchor(view: ViewGroup?) {
        anchorView = view
    }

    val isControllerActive: Boolean
        get() = timer != null && timerTask != null

    override fun onClick(view: View) {
        if (view.id == anchorView!!.id) {
            val invisible = controllerView!!.visibility != View.VISIBLE
            showController(invisible)
            return
        }
        if (view.id == controllerButton!!.id) {
            controllerHandler!!.removeCallbacks(controllerRunnable!!)
            val isPlaying = mVideoView!!.isPlaying
            if (onPlayButtonPressedListener != null) {
                onPlayButtonPressedListener!!.onPressed(isPlaying)
            }
            if (isPlaying) {
                mVideoView.pause()
                setPlaying(false)
                if (controllerView!!.visibility != View.VISIBLE) {
                    showController(true)
                }
            } else {
                mVideoView.start()
                setPlaying(true)
            }
        }
    }

    fun pause() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
        setButtonStatePlaying(false)
    }

    private fun resume() {
        if (handler == null) {
            handler = MyHandler()
        }
        if (timerTask != null) {
            timerTask!!.cancel()
        }
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
        }
        timerTask = MyTask()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timerTask, 0, 100)
        if (controllerView!!.visibility == View.VISIBLE) {
            controllerHandler!!.postDelayed(controllerRunnable!!, 2000)
        }
    }

    private fun init() {
        animFadeIn = AnimationUtils.loadAnimation(anchorView!!.context, R.anim.jcontroller_fade_in)
        animFadeOut =
            AnimationUtils.loadAnimation(anchorView!!.context, R.anim.jcontroller_fade_out)
        controllerRunnable = Runnable {
            if (controllerView != null && controllerView!!.visibility == View.VISIBLE) {
                showController(false)
            }
        }
        controllerHandler = Handler()
        createControllerView()
    }

    fun destroy() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
        handler = null
        controllerView = null
        toast("Destroyed!")
    }

    fun setOnButtonPressedListener(listener: OnButtonPressedListener?) {
        onPlayButtonPressedListener = listener
    }

    @SuppressLint("InflateParams")
    private fun createControllerView() {
        val context = mVideoView?.context
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        controllerView = inflater.inflate(R.layout.video_controller_layout, null)
        anchorView?.addView(controllerView)
        mSeekBar = controllerView?.findViewById(R.id.controller_seekbar)
        startTextView = controllerView?.findViewById(R.id.controller_timer_label)
        endTextView = controllerView?.findViewById(R.id.video_length_label)
        controllerButton = controllerView?.findViewById(R.id.controller_play_button)
        mSeekBar?.setOnSeekBarChangeListener(this)
        anchorView?.setOnClickListener(this)
        controllerButton?.setOnClickListener(this)
        setPlaying(mVideoView?.isPlaying == true)
        run { setPlaying(true) }
        visibilitySwitch?.showAnimation = animFadeIn
        visibilitySwitch?.hideAnimation = animFadeOut
    }

    private fun showController(show: Boolean) {
        controllerHandler?.removeCallbacks(controllerRunnable!!)
        controllerView?.visibility = if (show) View.VISIBLE else View.INVISIBLE
        controllerView?.startAnimation(if (show) animFadeIn else animFadeOut)
        if (show) {
            controllerHandler?.postDelayed(controllerRunnable!!, 2000)
            visibilitySwitch?.show()
        } else {
            visibilitySwitch?.hide()
        }
    }

    private fun setPlaying(playing: Boolean) {
        if (playing) {
            resume()
        } else {
            pause()
        }
    }

    private fun setButtonStatePlaying(state: Boolean) {
        if (controllerButton != null) {
            val resId = if (state) R.drawable.ic_video_pause else R.drawable.ic_video_play
            controllerButton!!.setImageResource(resId)
            ButtonStatePlaying = state
        }
    }

    override fun onProgressChanged(seekbar: SeekBar, postion: Int, tr: Boolean) {
        //do nothing
    }

    override fun onStartTrackingTouch(seekbar: SeekBar) {
        tracking = true
        controllerHandler?.removeCallbacks(controllerRunnable!!)
        if (controllerView?.visibility != View.VISIBLE) {
            showController(true)
        }
    }

    override fun onStopTrackingTouch(seekbar: SeekBar) {
        controllerHandler!!.postDelayed(controllerRunnable!!, 2000)
        mVideoView!!.seekTo(seekbar.progress)
        tracking = false
    }

    interface OnButtonPressedListener {
        fun onPressed(playing: Boolean)
    }

    inner class MyHandler : Handler() {
        override fun dispatchMessage(message: Message) {
            if (mVideoView == null) return
            val playing = mVideoView.isPlaying
            if (playing) {
                if (duration == -1) {
                    duration = mVideoView.duration
                    mSeekBar!!.visibility = View.VISIBLE
                    mSeekBar!!.max = duration
                    max = getFormattedVideoDuration(duration.toString())
                    endTextView!!.text = max
                }
                val position = mVideoView.currentPosition
                //int progress = (int)(100 - (((double)duration - (double)position) * (100 / (double)duration)));
                if (!tracking) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        mSeekBar!!.setProgress(position, true)
                    } else {
                        mSeekBar!!.progress = position
                    }
                }
                val current = getFormattedVideoDuration(position.toString())
                startTextView!!.text = current
            }
            if (playing != ButtonStatePlaying) {
                setButtonStatePlaying(playing)
            }
        }
    }

    inner class MyTask : TimerTask() {
        override fun run() {
            handler?.sendEmptyMessage(0)
        }
    }

    companion object {
        const val TAG = "JVideoController"
    }
}