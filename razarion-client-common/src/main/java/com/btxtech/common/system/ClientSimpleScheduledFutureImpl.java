package com.btxtech.common.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import elemental2.dom.DomGlobal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Dependent
public class ClientSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    private static final int MAX_OVERRUN_COUNT = 100;
    private Logger logger = Logger.getLogger(ClientSimpleScheduledFutureImpl.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PerfmonService perfmonService;
    private Double timerId;
    private double milliSDelay;
    private boolean repeating;
    private Optional<PerfmonEnum> perfmonEnum;
    private Runnable runnable;
    private double expected;
    private DomGlobal.SetTimeoutCallbackFn callback;
    private int overrunCount;

    public void init(double milliSDelay, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        this.perfmonEnum = Optional.ofNullable(perfmonEnum);
        this.runnable = runnable;
    }

    @Override
    public void start() {
        if (timerId != null) {
            return;
        }
        expected = System.currentTimeMillis() + milliSDelay;
        callback = p0 -> {
            long startTime = System.currentTimeMillis();
            double timeDrift = startTime - expected;
            try {
                if (repeating) {
                    if (timeDrift > milliSDelay) {
                        overrunCount++;
                        if (overrunCount >= MAX_OVERRUN_COUNT) {
                            logger.severe("ClientSimpleScheduledFutureImpl: something really bad happened. Maybe the browser (tab) was inactive? possibly special handling to avoid futile \"catch up\" run. "
                                    + " timeDrift=" + timeDrift
                                    + " expected=" + expected
                                    + " timerId=" + timerId
                                    + " perfmonEnum=" + perfmonEnum.orElse(null));
                            overrunCount = 0;
                        }
                    } else {
                        overrunCount = 0;
                    }
                } else {
                    timerId = null;
                }
                perfmonEnum.ifPresent(perfmonService::onEntered);
                long startExecutionTime = System.currentTimeMillis();
                runnable.run();
                long executionTime = System.currentTimeMillis() - startExecutionTime;
                if (repeating && executionTime > milliSDelay) {
                    logger.severe("ClientSimpleScheduledFutureImpl: Payload execution took longer than delay. "
                            + " timerId=" + timerId
                            + " execution time=" + executionTime
                            + " delay=" + milliSDelay
                            + " perfmonEnum=" + perfmonEnum.orElse(null));
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            } finally {
                perfmonEnum.ifPresent(perfmonService::onLeft);
            }
            if (repeating) {
                expected += milliSDelay;
                long totalTime = System.currentTimeMillis() - startTime;
                // timerId can be cleared if cancel() runs in parallel with the callback
                if (timerId != null) {
                    // TODO Doesn't work https://developer.mozilla.org/de/docs/Web/API/WindowTimers/setTimeout
                    // TODO delay is bigger as expected
                    DomGlobal.setTimeout(callback, Math.max(10, milliSDelay - timeDrift));
                }
                if (totalTime + 10 > milliSDelay) {
                    logger.severe("ClientSimpleScheduledFutureImpl: Total execution took longer than delay. "
                            + " timerId=" + timerId
                            + " total time=" + totalTime
                            + " delay=" + milliSDelay
                            + " perfmonEnum=" + perfmonEnum.orElse(null));
                }
            }
        };
        timerId = DomGlobal.setTimeout(callback, milliSDelay);
    }

    @Override
    public void cancel() {
        if (timerId == null) {
            return;
        }
        DomGlobal.clearTimeout(timerId);
        timerId = null;
    }
}
