package com.jefferson.application.br.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.jefferson.application.br.util.JDebug;
import java.io.Serializable;

abstract public class JTask implements JTaskListener {

    private Thread mainThread;
    private static final int STATE_FINISHED = 3;
    private static final int STATE_INTERRUPTED = -1;
    private static final int STATE_BEING_STARTED = 1;
    private static final int STATE_UPDATED = 8;
    private static final int STATE_EXCEPTION_CAUGHT = 666;
    private static Exception exception = null;

    private  Handler mainHandler;

    public JTask() {
        this.mainThread = createThread();
        this.mainHandler = creataHandler();
    }

    private Handler creataHandler() {
        return new Handler(Looper.getMainLooper()) {

            @Override
            public void dispatchMessage(Message msg) {
                int state = msg.getData().getInt("state");

                switch (state) {
                    case STATE_FINISHED:
                        mainThread.interrupt();
                        onFinished();
                        break;
                    case STATE_BEING_STARTED:
                        onBeingStarted();
                        mainThread.start();
                        break;
                    case STATE_INTERRUPTED:
                        if (!isInterrupted()) {
                            mainThread.interrupt();
                            onInterrupted();
                        }
                        break;
                    case STATE_UPDATED:
                        Object[] data = (Object[]) msg.getData().getSerializable("data");
                        onUpdated(data);
                        break;
                    case STATE_EXCEPTION_CAUGHT:
                        onException(exception);
                        break;
                }
            }
        };
    }

    public void setThreadPriority(int priority) {
        mainThread.setPriority(priority);
    }

    private void sendState(final int state) {
        sendState(state, null);
    }

    private Thread createThread() {
        Thread workThread = new Thread() {

            @Override
            public void run() {
                try {
                    workingThread();
                } catch (Exception e) {
                    exception = e;
                    sendState(STATE_EXCEPTION_CAUGHT);
                    return;
                }
                sendState(STATE_FINISHED);
            }
        };
        return workThread;
    }

    public void start() {
        sendState(STATE_BEING_STARTED);
    }

    public void interrupt() {
        sendState(STATE_INTERRUPTED);
    }

    public boolean isInterrupted() {
        return mainThread.isInterrupted();
    }

    protected void sendUpdate(Object... objs) {
        sendState(STATE_UPDATED, objs);
    }

    private void sendState(int state, Object[] args) {
        Bundle bundle = new Bundle();
        Message msg = new Message();
        bundle.putInt("state", state);
        if (args != null) {
            bundle.putSerializable("data", args);
        }
        msg.setData(bundle);
        mainHandler.sendMessage(msg);
    }
    
    protected void onInterrupted(){}
    protected void onUpdated(Object[] get){}
}

interface JTaskListener {

    void workingThread()
    void onBeingStarted()
    void onFinished()
    void onException(Exception e)
}
