package com.btxtech.common.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ClientSimpleExecutorServiceImpl implements SimpleExecutorService {
    private final Provider<ClientSimpleScheduledFutureImpl> instance;

    @Inject
    public ClientSimpleExecutorServiceImpl(Provider<ClientSimpleScheduledFutureImpl> instance) {
        this.instance = instance;
    }

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        ClientSimpleScheduledFutureImpl clientSimpleScheduledFuture = instance.get();
        clientSimpleScheduledFuture.init(delayMilliS, false, type.getPerfmonEnum(), runnable);
        clientSimpleScheduledFuture.start();
        return clientSimpleScheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        ClientSimpleScheduledFutureImpl clientSimpleScheduledFuture = instance.get();
        clientSimpleScheduledFuture.init(delayMilliS, true, type.getPerfmonEnum(), runnable);
        if (start) {
            clientSimpleScheduledFuture.start();
        }
        return clientSimpleScheduledFuture;
    }
}
