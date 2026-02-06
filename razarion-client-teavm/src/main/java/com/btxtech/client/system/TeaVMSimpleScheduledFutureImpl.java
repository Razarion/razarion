package com.btxtech.client.system;

import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.client.jso.JsConsole;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import jakarta.inject.Inject;
import java.util.Optional;

public class TeaVMSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    private static final int MAX_OVERRUN_COUNT = 100;

    private final PerfmonService perfmonService;
    private Integer timerId;
    private double milliSDelay;
    private boolean repeating;
    private Optional<PerfmonEnum> perfmonEnum;
    private Runnable runnable;
    private double expected;
    private int overrunCount;

    @Inject
    public TeaVMSimpleScheduledFutureImpl(PerfmonService perfmonService) {
        this.perfmonService = perfmonService;
    }

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
        scheduleNext((int) milliSDelay);
    }

    private void scheduleNext(int delay) {
        timerId = setTimeout((TimerCallback) () -> executeCallback(), delay);
    }

    private void executeCallback() {
        long startTime = System.currentTimeMillis();
        double timeDrift = startTime - expected;
        try {
            if (repeating) {
                if (timeDrift > milliSDelay) {
                    overrunCount++;
                    if (overrunCount >= MAX_OVERRUN_COUNT) {
                        JsConsole.error("TeaVMSimpleScheduledFutureImpl: something really bad happened. "
                                + "timeDrift=" + timeDrift
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
                JsConsole.error("TeaVMSimpleScheduledFutureImpl: Payload execution took longer than delay. "
                        + " timerId=" + timerId
                        + " execution time=" + executionTime
                        + " delay=" + milliSDelay
                        + " perfmonEnum=" + perfmonEnum.orElse(null));
            }
        } catch (Throwable t) {
            JsConsole.warn("TeaVMSimpleScheduledFutureImpl callback error: " + t.getMessage());
        } finally {
            perfmonEnum.ifPresent(perfmonService::onLeft);
        }

        if (repeating && timerId != null) {
            expected += milliSDelay;
            int nextDelay = Math.max(10, (int) (milliSDelay - timeDrift));
            scheduleNext(nextDelay);
        }
    }

    @Override
    public void cancel() {
        if (timerId == null) {
            return;
        }
        clearTimeout(timerId);
        timerId = null;
    }

    @JSBody(params = {"callback", "delay"}, script = "return setTimeout(callback, delay);")
    private static native int setTimeout(TimerCallback callback, int delay);

    @JSBody(params = {"timerId"}, script = "clearTimeout(timerId);")
    private static native void clearTimeout(int timerId);

    @JSFunctor
    public interface TimerCallback extends JSObject {
        void onTimer();
    }
}
