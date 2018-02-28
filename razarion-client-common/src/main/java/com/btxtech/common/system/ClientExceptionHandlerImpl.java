package com.btxtech.common.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
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
    private boolean windowClosing;

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

    public void registerWindowCloseHandler() {
        try {
            Window.addCloseHandler(windowCloseEvent -> windowClosing = true);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    public void handleRestException(String restService, Object message, Throwable throwable) {
        try {
            if (throwable instanceof ResponseException) {
                ResponseException responseException = (ResponseException) throwable;
                if (responseException.getResponse().getStatusCode() == 0 && windowClosing) {
                    return;
                }
            }
            logger.log(Level.SEVERE, restService + ": " + message, throwable);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    public ErrorCallback<?> restErrorHandler(String restService) {
        return (message, throwable) -> {
            handleRestException(restService, message, throwable);
            return false;
        };
    }
}
