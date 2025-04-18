package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import jakarta.inject.Provider;
import org.springframework.stereotype.Service;

@Service
public class ServerSimpleExecutorServiceImpl implements SimpleExecutorService {
    private final Provider<ServerSimpleScheduledFuture> provider;

    public ServerSimpleExecutorServiceImpl(Provider<ServerSimpleScheduledFuture> provider) {
        this.provider = provider;
    }

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        ServerSimpleScheduledFuture serverSimpleScheduledFuture = provider.get();
        serverSimpleScheduledFuture.init(delayMilliS, false, type.getPerfmonEnum(), runnable);
        serverSimpleScheduledFuture.start();
        return serverSimpleScheduledFuture;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        ServerSimpleScheduledFuture serverSimpleScheduledFuture = provider.get();
        serverSimpleScheduledFuture.init(delayMilliS, true, type.getPerfmonEnum(), runnable);
        if (start) {
            serverSimpleScheduledFuture.start();
        }
        return serverSimpleScheduledFuture;
    }
}
