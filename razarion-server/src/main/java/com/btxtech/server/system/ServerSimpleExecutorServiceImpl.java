package com.btxtech.server.system;

import com.btxtech.system.SimpleExecutorService;
import com.btxtech.system.SimpleScheduledFuture;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerSimpleExecutorServiceImpl implements SimpleExecutorService {
    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable) {
        throw new UnsupportedOperationException();
    }
}
