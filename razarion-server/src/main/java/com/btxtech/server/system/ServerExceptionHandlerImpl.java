package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerExceptionHandlerImpl implements ExceptionHandler {
    @Inject
    private Logger logger;

    @Override
    public void handleException(Throwable t) {
        logger.log(Level.SEVERE, t.getMessage(), t);
    }

    @Override
    public void handleException(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
