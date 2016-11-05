package com.btxtech.client.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.google.gwt.user.client.Timer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Dependent
public class ClientSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PerfmonService perfmonService;
    private Timer timer;
    private long milliSDelay;
    private boolean repeating;
    private Optional<PerfmonEnum> perfmonEnum;
    private Runnable runnable;

    public void init(long milliSDelay, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        this.perfmonEnum = Optional.ofNullable(perfmonEnum);
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
                    perfmonEnum.ifPresent(perfmonService::onEntered);
                    runnable.run();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                } finally {
                    perfmonEnum.ifPresent(perfmonService::onLeft);
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
