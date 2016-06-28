package com.btxtech.client.system;

import com.btxtech.system.ExceptionHandler;
import com.btxtech.system.SimpleScheduledFuture;
import com.google.gwt.user.client.Timer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Dependent
public class ClientSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    @Inject
    private ExceptionHandler exceptionHandler;
    private Timer timer;
    private long milliSDelay;
    private Runnable runnable;

    public void init(long milliSDelay, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.runnable = runnable;
    }

    @Override
    public void cancel() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
    }

    @Override
    public void start() {
        if (timer != null) {
            return;
        }
        timer = new Timer() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        };
        timer.scheduleRepeating((int) milliSDelay);
    }
}
