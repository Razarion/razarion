package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class DevToolsSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Inject
    private Instance<DevToolsSimpleScheduledFutureImpl> devToolsSimpleScheduledFutures;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable) {
        DevToolsSimpleScheduledFutureImpl scheduledFuture = devToolsSimpleScheduledFutures.get();
        scheduledFuture.init(scheduler, delayMilliS, false, runnable);
        scheduledFuture.start();
        return scheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable) {
        DevToolsSimpleScheduledFutureImpl scheduledFuture = devToolsSimpleScheduledFutures.get();
        scheduledFuture.init(scheduler, delayMilliS, true, runnable);
        if (start) {
            scheduledFuture.start();
        }
        return scheduledFuture;
    }
}
