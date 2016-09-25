package com.btxtech.client.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.google.gwt.user.client.Timer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Dependent
public class ClientSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Timer timer;
    private long milliSDelay;
    private boolean repeating;
    private Runnable runnable;

    public void init(long milliSDelay, boolean repeating, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
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
                    if (!repeating) {
                        timer = null;
                    }
                    runnable.run();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        };
        if (repeating) {
            timer.scheduleRepeating((int) milliSDelay);
        } else {
            timer.schedule((int) milliSDelay);
        }
    }
}
