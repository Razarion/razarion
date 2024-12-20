package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Inject
    private Provider<ServerSimpleScheduledFuture> instance;

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        ServerSimpleScheduledFuture serverSimpleScheduledFuture = instance.get();
        serverSimpleScheduledFuture.init(delayMilliS, false, type.getPerfmonEnum(), runnable);
        serverSimpleScheduledFuture.start();
        return serverSimpleScheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        ServerSimpleScheduledFuture serverSimpleScheduledFuture = instance.get();
        serverSimpleScheduledFuture.init(delayMilliS, true, type.getPerfmonEnum(), runnable);
        if (start) {
            serverSimpleScheduledFuture.start();
        }
        return serverSimpleScheduledFuture;
    }
}
