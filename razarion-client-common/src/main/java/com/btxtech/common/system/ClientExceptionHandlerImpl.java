package com.btxtech.common.system;

import com.btxtech.shared.system.ExceptionHandler;
import org.jboss.errai.ioc.client.api.UncaughtExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 */
@ApplicationScoped
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

    @UncaughtExceptionHandler
    public void handleUncaughtException(Throwable t) {
        handleException("UncaughtExceptionHandler caught in ClientExceptionHandlerImpl", t);
    }
}
