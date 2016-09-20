package com.btxtech.client.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ClientSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Inject
    private Instance<ClientSimpleScheduledFutureImpl> instance;

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        ClientSimpleScheduledFutureImpl clientSimpleScheduledFuture = instance.get();
        clientSimpleScheduledFuture.init(delayMilliS, false, runnable);
        clientSimpleScheduledFuture.start();
        return clientSimpleScheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        ClientSimpleScheduledFutureImpl clientSimpleScheduledFuture = instance.get();
        clientSimpleScheduledFuture.init(delayMilliS, true, runnable);
        if (start) {
            clientSimpleScheduledFuture.start();
        }
        return clientSimpleScheduledFuture;
    }
}
