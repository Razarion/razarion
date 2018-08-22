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
    private Logger logger = Logger.getLogger(ClientSimpleScheduledFutureImpl.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PerfmonService perfmonService;
    private Long timerId;
    private double milliSDelay;
    private boolean repeating;
    private Optional<PerfmonEnum> perfmonEnum;
    private Runnable runnable;
    private double expected;
    private DomGlobal.SetTimeoutCallbackFn callback;

    public void init(double milliSDelay, boolean repeating, PerfmonEnum perfmonEnum, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        this.perfmonEnum = Optional.ofNullable(perfmonEnum);
        this.runnable = runnable;
    }

    @Override
    public void cancel() {
        if (timerId == null) {
            return;
        }
        DomGlobal.clearTimeout(timerId);
        timerId = null;
    }

    @Override
    public void start() {
        if (timerId != null) {
            return;
        }
        expected = System.currentTimeMillis() + milliSDelay;
        callback = p0 -> {
            double timeDrift = System.currentTimeMillis() - expected;
            if (timeDrift > milliSDelay) {
                logger.severe("ClientSimpleScheduledFutureImpl: something really bad happened. Maybe the browser (tab) was inactive? possibly special handling to avoid futile \"catch up\" run. PerfmonEnum: " + perfmonEnum);
            }
            try {
                if (!repeating) {
                    timerId = null;
                }
                perfmonEnum.ifPresent(perfmonService::onEntered);
                runnable.run();
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            } finally {
                perfmonEnum.ifPresent(perfmonService::onLeft);
            }
            if (repeating) {
                expected += milliSDelay;
                DomGlobal.setTimeout(callback, Math.max(0.0, milliSDelay - timeDrift));
            }
        };
        DomGlobal.setTimeout(callback, milliSDelay);
    }
}
