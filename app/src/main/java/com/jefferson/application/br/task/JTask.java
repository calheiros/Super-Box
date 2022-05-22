package com.jefferson.application.br.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

abstract public class JTask implements JTaskListener {

    private JTask.OnFinishedListener onFinishedListener;
    private static final int STATE_FINISHED = 3;
    private static final int STATE_INTERRUPTED = -1;
    private static final int STATE_BEING_STARTED = 1;
    private static final int STATE_UPDATED = 8;
    private static final int STATE_EXCEPTION_CAUGHT = 666;
    private static final int STATE_TASK_CANCELLED = 444;

    private boolean interrupted = false;
    private static Exception exception = null;
    private boolean revokeFinish = false;
    private  Handler mainHandler;
    private Thread workThread;
    private boolean cancelled = false;
    private OnUpdatedListener onUpdatedListener;
    private OnBeingStartedListener onBeingStartedListener;

    public static enum Status {
        FINISHED,
        STARTED,
        INTERRUPTED,
        CANCELLED
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
                    if (revokeFinish) {
                        break;
                    }
                    status = Status.FINISHED;
                    workThread.interrupt();

                    onFinished();
                    if (onFinishedListener != null) {
                        onFinishedListener.onFinished();
                    }
                    break;
                case STATE_BEING_STARTED:
                    status = Status.STARTED;
                    onBeingStarted();
                    if (onBeingStartedListener != null) {
                        onBeingStartedListener.onBeingStarted();
                    }
                    workThread.start();
                    break;
                case STATE_INTERRUPTED:
                    status = Status.INTERRUPTED;
                    onInterrupted();
                    break;
                case STATE_UPDATED:
                    Object[] data = (Object[]) msg.getData().getSerializable("data");
                    onUpdated(data);
                    if (onUpdatedListener != null) {
                        onUpdatedListener.onUpdated(data);
                    }
                    break;
                case STATE_EXCEPTION_CAUGHT:
                    onException(exception);
                    break;
                case STATE_TASK_CANCELLED:
                    status = Status.CANCELLED;
                    onTaskCancelled();
            }
        }
    }

    public void cancelTask() {
        cancelled = true;
        workThread.interrupt();
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public Exception getException() {
        return exception;
    }
    
    private class WorkThread extends Thread {

        @Override
        public void run() {
            try {
                workingThread();
            } catch (Exception e) {
                exception = e;
                revokeFinish(true);
                sendState(STATE_EXCEPTION_CAUGHT);
            } finally {
                int state = cancelled ? STATE_TASK_CANCELLED : STATE_FINISHED;
                sendState(state);
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public void revokeFinish(boolean revoked) {
        this.revokeFinish = revoked;
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
        interrupted = true;
        workThread.interrupt();
        sendState(STATE_INTERRUPTED);
    }

    public boolean isInterrupted() {
        return interrupted;
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

    public void setOnbeingStartedListener(OnBeingStartedListener listener) {
        this.onBeingStartedListener = listener;
    }

    public void setOnUpdatedListener(OnUpdatedListener listener) {
        this.onUpdatedListener = listener;
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        this.onFinishedListener = listener;
    }

    protected void onTaskCancelled() {

    }

    protected void onInterrupted() {

    }

    protected void onUpdated(Object[] get) {

    }

    public static interface OnFinishedListener {
        void onFinished()
    }

    public static interface OnBeingStartedListener {
        void onBeingStarted()
    }

    public static interface OnUpdatedListener {
        void onUpdated(Object[] values)
    }
}

interface JTaskListener {
    void workingThread()
    void onBeingStarted()
    void onFinished()
    void onException(Exception e)

}


