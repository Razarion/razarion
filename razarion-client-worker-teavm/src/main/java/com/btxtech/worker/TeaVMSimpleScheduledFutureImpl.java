package com.btxtech.worker;

import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.worker.jso.JsConsole;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import jakarta.inject.Inject;
import java.util.Optional;

/**
 * TeaVM implementation of SimpleScheduledFuture
 * Uses JavaScript setTimeout/setInterval via @JSBody
 */
public class TeaVMSimpleScheduledFutureImpl implements SimpleScheduledFuture {
    private static final int MAX_OVERRUN_COUNT = 100;
    /**
     * Beyond this much lateness the missed runs are dropped instead of replayed. Fixed-rate
     * scheduling keeps the average rate honest against setTimeout's slop, which is worth having for
     * small drifts. But a dedicated worker is throttled together with its tab, and returning with
     * `expected` minutes in the past used to pin every following callback at the 10 ms floor until
     * the whole backlog had run - for the 100 ms engine tick that is a burst of hundreds of
     * simulation steps at once. Replaying them buys nothing: the client resynchronises with the
     * server anyway, and until it does, the burst is what freezes the tab.
     */
    private static final double MAX_CATCH_UP_MILLIS = 1000;
    private static final long OVERRUN_LOG_INTERVAL_MS = 10_000;

    private Integer timerId;
    private double milliSDelay;
    private boolean repeating;
    private Optional<SimpleExecutorService.Type> type;
    private Runnable runnable;
    private double expected;
    private int overrunCount;
    private int executionOverrunCount;
    private long lastOverrunLogTime;

    @Inject
    public TeaVMSimpleScheduledFutureImpl() {
    }

    public void init(double milliSDelay, boolean repeating, SimpleExecutorService.Type type, Runnable runnable) {
        this.milliSDelay = milliSDelay;
        this.repeating = repeating;
        this.type = Optional.ofNullable(type);
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
                                + " type=" + type.orElse(null));
                        overrunCount = 0;
                    }
                } else {
                    overrunCount = 0;
                }
            } else {
                timerId = null;
            }

            long startExecutionTime = System.currentTimeMillis();
            runnable.run();
            long executionTime = System.currentTimeMillis() - startExecutionTime;

            if (repeating && executionTime > milliSDelay) {
                // A long A* search or pathing tick can routinely push the engine over its 100 ms
                // budget. Throttled by time, not by a run of consecutive overruns: an engine that
                // overruns every second tick never has a run longer than one, so counting runs
                // would report every single time. First overrun goes out immediately, then at most
                // one line per interval, carrying how many happened in between.
                executionOverrunCount++;
                long now = System.currentTimeMillis();
                if (lastOverrunLogTime == 0 || now - lastOverrunLogTime >= OVERRUN_LOG_INTERVAL_MS) {
                    JsConsole.warn("TeaVMSimpleScheduledFutureImpl: Payload execution took longer than delay ("
                            + executionOverrunCount + " overruns since the last of these lines)."
                            + " timerId=" + timerId
                            + " execution time=" + executionTime
                            + " delay=" + milliSDelay
                            + " type=" + type.orElse(null));
                    lastOverrunLogTime = now;
                    executionOverrunCount = 0;
                }
            }
        } catch (Throwable t) {
            JsConsole.warn("TeaVMSimpleScheduledFutureImpl callback error: " + t.getMessage());
        } finally {
        }

        if (repeating && timerId != null) {
            if (timeDrift > MAX_CATCH_UP_MILLIS) {
                JsConsole.warn("TeaVMSimpleScheduledFutureImpl: dropped " + missedRuns(timeDrift)
                        + " missed runs after a stall of " + (long) timeDrift + " ms, starting over from now."
                        + " delay=" + milliSDelay
                        + " type=" + type.orElse(null));
                expected = System.currentTimeMillis() + milliSDelay;
                scheduleNext((int) milliSDelay);
            } else {
                expected += milliSDelay;
                int nextDelay = Math.max(10, (int) (milliSDelay - timeDrift));
                scheduleNext(nextDelay);
            }
        }
    }

    private long missedRuns(double timeDrift) {
        return (long) (timeDrift / Math.max(1, milliSDelay));
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
