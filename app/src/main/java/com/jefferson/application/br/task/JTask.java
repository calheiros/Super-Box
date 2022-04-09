package com.jefferson.application.br.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.jefferson.application.br.util.JDebug;
import java.io.Serializable;

abstract public class JTask implements JTaskListener {

    
    private static final int STATE_FINISHED = 3;
    private static final int STATE_INTERRUPTED = -1;
    private static final int STATE_BEING_STARTED = 1;
    private static final int STATE_UPDATED = 8;
    private static final int STATE_EXCEPTION_CAUGHT = 666;
    private static Exception exception = null;

    private  Handler mainHandler;
    private Thread workThread;
    
    public static enum Status {
        FINISHED,
        STARTED,
        INTERRUPTED
    }
    
    public Status status;
    
    public JTask() {
        this.workThread = new WorkThread();
        this.mainHandler = new MainHandler(Looper.getMainLooper());
    }
    
    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            int state = msg.getData().getInt("state");

            switch (state) {
                case STATE_FINISHED:
                    status = Status.FINISHED;
                    workThread.interrupt();
                    onFinished();
                    break;
                case STATE_BEING_STARTED:
                    status = Status.STARTED;
                    onBeingStarted();
                    workThread.start();
                    break;
                case STATE_INTERRUPTED:
                    status = Status.INTERRUPTED;
                    if (!isInterrupted()) {
                        workThread.interrupt();
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
    }

    private class WorkThread extends Thread {

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
    }
    
    public void setThreadPriority(int priority) {
        workThread.setPriority(priority);
    }

    private void sendState(final int state) {
        sendState(state, null);
    }
    
    public void start() {
        sendState(STATE_BEING_STARTED);
    }

    public void interrupt() {
        sendState(STATE_INTERRUPTED);
    }

    public boolean isInterrupted() {
        return workThread.isInterrupted();
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

    protected void onInterrupted() {}
    protected void onUpdated(Object[] get) {}
}

interface JTaskListener {

    void workingThread()
    void onBeingStarted()
    void onFinished()
    void onException(Exception e)
}
