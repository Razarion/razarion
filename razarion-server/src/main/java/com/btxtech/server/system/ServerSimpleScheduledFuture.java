package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonEnum;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 19.04.2017.
 */
public class ServerSimpleScheduledFuture implements SimpleScheduledFuture {
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @Inject
    private ExceptionHandler exceptionHandler;
    // TODO  @Inject
    // TODO  private PerfmonService perfmonService;
    private Runnable runnable;
    private long milliSDelay;
    private ScheduledFuture scheduledFuture;
    private boolean repeating;
    // TODO   private PerfmonEnum perfmonEnum;

    public void init(long milliSDelay, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        // TODO this.perfmonEnum = perfmonEnum;
        this.runnable = runnable;
    }

    @Override
    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    @Override
    public void start() {
        if (this.scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (repeating) {
            scheduledFuture = scheduleExecutor.scheduleAtFixedRate(this::run, milliSDelay, milliSDelay, TimeUnit.MILLISECONDS);
        } else {
            scheduledFuture = scheduleExecutor.schedule(runnable, milliSDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void run() {
        try {
            // TODO FIX :PerfmonService.onEntered(): onEntered has already been called for BOT_TICKER
            // TODO Optional.ofNullable(perfmonEnum).ifPresent(perfmonService::onEntered);
            runnable.run();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        } finally {
            // TODO Optional.ofNullable(perfmonEnum).ifPresent(perfmonService::onLeft);
        }
    }
}
