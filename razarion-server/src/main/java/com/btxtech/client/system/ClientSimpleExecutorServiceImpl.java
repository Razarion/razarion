package com.btxtech.client.system;

import com.btxtech.system.SimpleExecutorService;
import com.btxtech.system.SimpleScheduledFuture;

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
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable) {
        if (start) {
            throw new UnsupportedOperationException();
        }
        ClientSimpleScheduledFutureImpl clientSimpleScheduledFuture = instance.get();
        clientSimpleScheduledFuture.init(delayMilliS, runnable);
        return clientSimpleScheduledFuture;
    }
}
