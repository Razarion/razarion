package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
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
    private Map<Type, DevToolFutureControl> futureControls = new HashMap<>();
    private Map<Type, DevToolsSimpleScheduledFutureImpl> futures = new HashMap<>();

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        DevToolsSimpleScheduledFutureImpl scheduledFuture = devToolsSimpleScheduledFutures.get();
        futures.put(type, scheduledFuture);
        scheduledFuture.init(scheduler, delayMilliS, false, runnable, futureControls.get(type));
        scheduledFuture.start();
        return scheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        DevToolsSimpleScheduledFutureImpl scheduledFuture = devToolsSimpleScheduledFutures.get();
        futures.put(type, scheduledFuture);
        scheduledFuture.init(scheduler, delayMilliS, true, runnable, futureControls.get(type));
        if (start) {
            scheduledFuture.start();
        }
        return scheduledFuture;
    }

    public DevToolFutureControl createDevToolFutureControl(Type type) {
        DevToolFutureControl devToolFutureControl = new DevToolFutureControl();
        futureControls.put(type, devToolFutureControl);
        DevToolsSimpleScheduledFutureImpl scheduledFuture = futures.get(type);
        if (scheduledFuture != null) {
            scheduledFuture.setDevToolFutureControl(devToolFutureControl);
        }
        return devToolFutureControl;
    }

}
