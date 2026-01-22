package com.btxtech.uiservice.mock;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class TestSimpleExecutorService implements SimpleExecutorService {
    private final Logger logger = Logger.getLogger(TestSimpleExecutorService.class.getName());

    @Inject
    public TestSimpleExecutorService() {
    }

    @Override
    public SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type) {
        logger.fine("schedule()");
        return null;
    }

    @Override
    public SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type) {
        logger.fine("scheduleAtFixedRate()");
        return null;
    }
}
