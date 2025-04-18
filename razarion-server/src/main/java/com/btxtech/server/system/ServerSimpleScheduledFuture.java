package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class ServerSimpleScheduledFuture implements SimpleScheduledFuture {
    private final Logger logger = LoggerFactory.getLogger(ServerSimpleScheduledFuture.class);
    private final ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(1);
    private Runnable runnable;
    private long milliSDelay;
    private ScheduledFuture<?> scheduledFuture;
    private boolean repeating;

    public void init(long milliSDelay, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
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
            runnable.run();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }
}
