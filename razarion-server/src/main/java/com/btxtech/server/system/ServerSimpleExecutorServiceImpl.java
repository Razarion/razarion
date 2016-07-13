package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable) {
        throw new UnsupportedOperationException();
    }
}
