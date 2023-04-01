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
package com.jefferson.application.br.task

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

abstract class JTask : JTaskListener {
    private var onFinishedListener: () -> Unit = {}
    var isInterrupted = false
        private set
    var exception: Exception? = null
        private set
    private var revokeFinish = false
    private val mainHandler: Handler
    private val workThread: Thread
    var isCancelled = false
        private set
    private var onUpdatedListener: OnUpdatedListener? = null
    private var onBeingStartedListener: OnBeingStartedListener? = null

    enum class Status {
        FINISHED, STARTED, INTERRUPTED, CANCELLED
    }

    var status: Status? = null

    init {
        workThread = WorkThread()
        mainHandler = MainHandler(Looper.getMainLooper())
    }

    private inner class MainHandler(looper: Looper?) : Handler(looper!!) {
        override fun dispatchMessage(msg: Message) {
            when (msg.data.getInt("state")) {
                STATE_FINISHED -> {
                    if (revokeFinish) {
                        return
                    }
                    status = Status.FINISHED
                    workThread.interrupt()
                    onFinished()
                    onFinishedListener.invoke()

                }
                STATE_STARTED -> {
                    status = Status.STARTED
                    onStarted()
                    if (onBeingStartedListener != null) {
                        onBeingStartedListener!!.onBeingStarted()
                    }
                    workThread.start()
                }
                STATE_INTERRUPTED -> {
                    status = Status.INTERRUPTED
                    onInterrupted()
                }
                STATE_UPDATED -> {
                    val data = msg.data.getSerializable("data") as Array<Any>?
                    onUpdated(data)
                    if (onUpdatedListener != null) {
                        onUpdatedListener!!.onUpdated(data)
                    }
                }
                STATE_EXCEPTION_CAUGHT -> onException(exception)
                STATE_TASK_CANCELLED -> {
                    status = Status.CANCELLED
                    onTaskCancelled()
                }
            }
        }
    }

    fun cancelTask() {
        isCancelled = true
        workThread.interrupt()
    }

    private inner class WorkThread : Thread() {
        override fun run() {
            try {
                workingThread()
            } catch (e: Exception) {
                exception = e
                revokeFinish(true)
                sendState(STATE_EXCEPTION_CAUGHT)
            } finally {
                val state = if (isCancelled) STATE_TASK_CANCELLED else STATE_FINISHED
                sendState(state)
            }
        }
    }

    fun revokeFinish(revoked: Boolean) {
        revokeFinish = revoked
    }

    fun setThreadPriority(priority: Int) {
        workThread.priority = priority
    }

    fun start() {
        sendState(STATE_STARTED)
    }

    fun interrupt() {
        isInterrupted = true
        workThread.interrupt()
        sendState(STATE_INTERRUPTED)
    }

    protected fun postUpdate(vararg objs: Any?) {
        sendState(STATE_UPDATED, objs)
    }

    private fun sendState(state: Int, args: Array<*>? = null) {
        val bundle = Bundle()
        val msg = Message()
        bundle.putInt("state", state)
        if (args != null) {
            bundle.putSerializable("data", args)
        }
        msg.data = bundle
        mainHandler.sendMessage(msg)
    }

    fun setOnStartedListener(listener: OnBeingStartedListener?) {
        onBeingStartedListener = listener
    }

    fun setOnUpdatedListener(listener: OnUpdatedListener?) {
        onUpdatedListener = listener
    }

    open fun setOnFinishedListener(listener: () -> Unit) {
        onFinishedListener = listener
    }

    protected open fun onTaskCancelled() {}
    protected open fun onInterrupted() {}
    protected open fun onUpdated(args : Array<out Any>?) {}
    interface OnFinishedListener {
        fun onFinished()
    }

    interface OnBeingStartedListener {
        fun onBeingStarted()
    }

    interface OnUpdatedListener {
        fun onUpdated(values: Array<Any>?)
    }

    companion object {
        private const val STATE_FINISHED = 3
        private const val STATE_INTERRUPTED = -1
        private const val STATE_STARTED = 1
        private const val STATE_UPDATED = 8
        private const val STATE_EXCEPTION_CAUGHT = 666
        private const val STATE_TASK_CANCELLED = 444
    }
}

internal interface JTaskListener {
    fun workingThread()
    fun onStarted()
    fun onFinished()
    fun onException(e: Exception?)
}