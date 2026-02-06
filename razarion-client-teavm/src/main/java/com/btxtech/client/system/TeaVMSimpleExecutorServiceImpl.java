package com.btxtech.client.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMSimpleExecutorServiceImpl implements SimpleExecutorService {
    private final Provider<TeaVMSimpleScheduledFutureImpl> instance;

    @Inject
    public TeaVMSimpleExecutorServiceImpl(Provider<TeaVMSimpleScheduledFutureImpl> instance) {
        this.instance = instance;
    }

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        TeaVMSimpleScheduledFutureImpl scheduledFuture = instance.get();
        scheduledFuture.init(delayMilliS, false, type.getPerfmonEnum(), runnable);
        scheduledFuture.start();
        return scheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        TeaVMSimpleScheduledFutureImpl scheduledFuture = instance.get();
        scheduledFuture.init(delayMilliS, true, type.getPerfmonEnum(), runnable);
        if (start) {
            scheduledFuture.start();
        }
        return scheduledFuture;
    }
}
