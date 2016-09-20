package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 28.06.2016.
 */
public class DevToolsSimpleScheduledFutureImpl implements SimpleScheduledFuture, Runnable {
    @Inject
    private ExceptionHandler exceptionHandler;
    private ScheduledFuture scheduledFuture;
    private ScheduledExecutorService scheduler;
    private long delayMilliS;
    private boolean repeating;
    private Runnable runnable;
    private DevToolFutureControl devToolFutureControl;

    public void init(ScheduledExecutorService scheduler, long delayMilliS, boolean repeating, Runnable runnable, DevToolFutureControl devToolFutureControl) {
        this.scheduler = scheduler;
        this.delayMilliS = delayMilliS;
        this.repeating = repeating;
        this.runnable = runnable;
        setDevToolFutureControl(devToolFutureControl);
    }

    @Override
    public void start() {
        if (scheduledFuture != null) {
            return;
        }
        if (repeating) {
            scheduledFuture = scheduler.scheduleAtFixedRate(this, delayMilliS, delayMilliS, TimeUnit.MILLISECONDS);
        } else {
            scheduledFuture = scheduler.schedule(this, delayMilliS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void cancel() {
        if (scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(true);
        scheduledFuture = null;
    }

    @Override
    public void run() {
        try {
            if (!repeating) {
                scheduledFuture = null;
            }
            runnable.run();
            if (devToolFutureControl != null) {
                devToolFutureControl.onAfterRun();
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setDelayMilliS(long delayMilliS) {
        this.delayMilliS = delayMilliS;
    }

    public void setDevToolFutureControl(DevToolFutureControl devToolFutureControl) {
        this.devToolFutureControl = devToolFutureControl;
        if (devToolFutureControl != null) {
            devToolFutureControl.setFuture(this);
        }
    }

    public void execute() {
        runnable.run();
        if (devToolFutureControl != null) {
            devToolFutureControl.onAfterRun();
        }
    }
}
