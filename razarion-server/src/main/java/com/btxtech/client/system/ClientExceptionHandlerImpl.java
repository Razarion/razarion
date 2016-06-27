package com.btxtech.client.system;

import com.btxtech.system.ExceptionHandler;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ClientExceptionHandlerImpl implements ExceptionHandler {
    private Logger logger = Logger.getLogger(ExceptionHandler.class.getName());

    @Override
    public void handleException(Throwable t) {
        logger.log(Level.SEVERE, t.getMessage(), t);
    }

    @Override
    public void handleException(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
