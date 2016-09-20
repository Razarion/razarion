package com.btxtech.webglemulator.razarion;

/**
 * Created by Beat
 * 19.09.2016.
 */
public class DevToolFutureControl {
    private Runnable afterExecutionCallback;
    private DevToolsSimpleScheduledFutureImpl future;

    public void setAfterExecutionCallback(Runnable afterExecutionCallback) {
        this.afterExecutionCallback = afterExecutionCallback;
    }

    public void onAfterRun() {
        if (afterExecutionCallback != null) {
            afterExecutionCallback.run();
        }
    }

    public void modifyDelay(long delay) {
        future.cancel();
        future.setDelayMilliS(delay);
        future.start();
    }

    public void setFuture(DevToolsSimpleScheduledFutureImpl future) {
        this.future = future;
    }

    public void start() {
        future.start();
    }

    public void cancel() {
        future.cancel();
    }

    public void singleEexecute() {
        future.cancel();
        future.execute();
    }
}
