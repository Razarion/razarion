package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @Inject
    private Instance<ServerSimpleScheduledFuture> instance;

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        ServerSimpleScheduledFuture serverSimpleScheduledFuture = instance.get();
        serverSimpleScheduledFuture.init(scheduleExecutor, runnable, delayMilliS);
        if (start) {
            serverSimpleScheduledFuture.start();
        }
        return serverSimpleScheduledFuture;
    }
}
