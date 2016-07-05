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
    private Runnable runnable;

    public void init(ScheduledExecutorService scheduler, long delayMilliS, Runnable runnable) {
        this.scheduler = scheduler;
        this.delayMilliS = delayMilliS;
        this.runnable = runnable;
    }

    @Override
    public void start() {
        if (scheduledFuture != null) {
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(this, delayMilliS, delayMilliS, TimeUnit.MILLISECONDS);
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
            runnable.run();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}
