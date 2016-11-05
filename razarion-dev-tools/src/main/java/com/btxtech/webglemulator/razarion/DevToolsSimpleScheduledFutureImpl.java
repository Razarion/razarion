package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonEnum;

import javax.inject.Inject;
import java.util.Optional;
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
    @Inject
    private PerfmonService perfmonService;
    private ScheduledFuture scheduledFuture;
    private ScheduledExecutorService scheduler;
    private long delayMilliS;
    private boolean repeating;
    private Optional<PerfmonEnum> perfmonEnum;
    private Runnable runnable;
    private DevToolFutureControl devToolFutureControl;

    public void init(ScheduledExecutorService scheduler, long delayMilliS, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable, DevToolFutureControl devToolFutureControl) {
        this.scheduler = scheduler;
        this.delayMilliS = delayMilliS;
        this.repeating = repeating;
        this.runnable = runnable;
        this.perfmonEnum = Optional.ofNullable(perfmonEnum);
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
            perfmonEnum.ifPresent(perfmonService::onEntered);
            runnable.run();
            if (devToolFutureControl != null) {
                devToolFutureControl.onAfterRun();
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        } finally {
            perfmonEnum.ifPresent(perfmonService::onLeft);
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
