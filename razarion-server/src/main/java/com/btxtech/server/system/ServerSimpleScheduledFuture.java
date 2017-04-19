package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 19.04.2017.
 */
public class ServerSimpleScheduledFuture implements SimpleScheduledFuture {
    private ManagedScheduledExecutorService scheduleExecutor;
    private Runnable runnable;
    private long delayMilliS;
    private ScheduledFuture scheduledFuture;

    public void init(ManagedScheduledExecutorService scheduleExecutor, Runnable runnable, long delayMilliS) {
        this.scheduleExecutor = scheduleExecutor;
        this.runnable = runnable;
        this.delayMilliS = delayMilliS;
    }

    @Override
    public void start() {
        if (scheduledFuture == null) {
            scheduledFuture = scheduleExecutor.scheduleAtFixedRate(runnable, 0, delayMilliS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }
}
