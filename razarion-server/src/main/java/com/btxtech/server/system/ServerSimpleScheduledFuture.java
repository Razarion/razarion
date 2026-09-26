package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One timer, on a thread of its own - and that thread ends with the timer.
 * <p>
 * It used to be one {@code Executors.newScheduledThreadPool(1)} per instance, never shut down. A
 * pool's core thread does not time out, so every timer ever scheduled left a parked thread behind:
 * PROD 23.09.2026, 15 h after a start, 2366 pools of one thread each ("pool-N-thread-1"), about one
 * every 23 s - the bots switching between their active and inactive interval each schedule a new
 * timer and restart their ticker. The JVM had 2429 threads and the container 1.96 GB against a
 * heap that stayed flat at 1.2 GB; the stacks were the growth.
 * <p>
 * The executor is now created on {@link #start()} and shut down on {@link #cancel()} and after a
 * one-shot run. A timer that is started again gets a new one. Threads carry their type in the
 * name.
 */
@Component
@Scope("prototype")
public class ServerSimpleScheduledFuture implements SimpleScheduledFuture {
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(ServerSimpleScheduledFuture.class);
    private ScheduledExecutorService scheduleExecutor;
    private Runnable runnable;
    private long milliSDelay;
    private ScheduledFuture<?> scheduledFuture;
    private boolean repeating;
    private SimpleExecutorService.Type type;

    public void init(long milliSDelay, boolean repeating, SimpleExecutorService.Type type, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        this.type = type;
        this.runnable = runnable;
    }

    @Override
    public synchronized void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        shutdownExecutor();
    }

    @Override
    public synchronized void start() {
        if (this.scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (scheduleExecutor == null) {
            scheduleExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "timer-" + type + "-" + THREAD_NUMBER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });
        }
        if (repeating) {
            scheduledFuture = scheduleExecutor.scheduleAtFixedRate(this::run, milliSDelay, milliSDelay, TimeUnit.MILLISECONDS);
        } else {
            ScheduledExecutorService executor = scheduleExecutor;
            scheduledFuture = scheduleExecutor.schedule(() -> {
                try {
                    runnable.run();
                } finally {
                    // shutdown(), not shutdownNow(): this runs on the executor's own thread, which
                    // ends once this task returns. The runnable may already have started a new
                    // timer on a new instance; that one has its own executor.
                    executor.shutdown();
                }
            }, milliSDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void shutdownExecutor() {
        if (scheduleExecutor != null) {
            // Not shutdownNow(): cancel(true) above already interrupted a running task, and cancel()
            // is called from inside the task itself (BotRunner.killTimer from the BotTimer).
            scheduleExecutor.shutdown();
            scheduleExecutor = null;
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
