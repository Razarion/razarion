package com.btxtech.shared.system;

/**
 * Created by Beat
 * 28.06.2016.
 */
public interface SimpleScheduledFuture {
    /**
     * Stops the scheduler. Ignored if the scheduler is already stopped
     */
    void cancel();

    /**
     * Starts the scheduler. Ignored if scheduler is already running
     */
    void start();
}
