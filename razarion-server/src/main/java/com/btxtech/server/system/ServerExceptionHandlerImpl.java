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
public class ServerExceptionHandlerImpl extends ExceptionHandler {
    @Inject
    private Logger logger;

    @Override
    protected void handleExceptionInternal(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
