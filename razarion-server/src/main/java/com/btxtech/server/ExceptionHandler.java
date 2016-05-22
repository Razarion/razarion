package com.btxtech.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 22.11.2015.
 */
@Singleton
public class ExceptionHandler {
    @Inject
    private Logger logger;

    public void handleException(Throwable throwable) {
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);
    }
}
