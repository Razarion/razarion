package com.btxtech.shared.system;

/**
 * Created by Beat
 * 27.06.2016.
 */
public interface SimpleExecutorService {
    SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable);

    SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable);
}
