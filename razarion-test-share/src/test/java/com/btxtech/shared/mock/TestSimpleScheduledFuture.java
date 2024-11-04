package com.btxtech.shared.mock;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class TestSimpleScheduledFuture implements SimpleScheduledFuture {
    private final boolean start;
    private final long delayMilliS;
    private final Runnable runnable;
    private final SimpleExecutorService.Type type;

    public TestSimpleScheduledFuture(boolean start, long delayMilliS, Runnable runnable, SimpleExecutorService.Type type) {
        this.start = start;
        this.delayMilliS = delayMilliS;
        this.runnable = runnable;
        this.type = type;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void start() {

    }

    public boolean isStart() {
        return start;
    }

    public long getDelayMilliS() {
        return delayMilliS;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public SimpleExecutorService.Type getType() {
        return type;
    }

    public void invokeRun() {
        runnable.run();
    }
}
