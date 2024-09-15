package com.btxtech.common.system;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import com.btxtech.client.ErrorCallback;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 * <p>
 * Idea replace in path with regexp
 * \, \(message\, throwable\) \-\> \{\n            logger\.log\(Level\.SEVERE\, \"(.*)?" \+ message\, throwable\)\;\n            return false\;\n        \}
 * , exceptionHandler.restErrorHandler("$1")
 */
@ApplicationScoped
public class ClientExceptionHandlerImpl extends ExceptionHandler {
    private Logger logger = Logger.getLogger(ExceptionHandler.class.getName());
    private boolean windowClosing;

    @Override
    protected void handleExceptionInternal(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }

    // TODO @UncaughtExceptionHandler
    public void handleUncaughtException(Throwable t) {
        handleException("UncaughtExceptionHandler caught in ClientExceptionHandlerImpl", t);
    }

    public void registerWindowCloseHandler() {
        try {
            DomGlobal.window.addEventListener("beforeunload", event -> windowClosing = true);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    public ErrorCallback<?> restErrorHandler(String restService) {
        return (message, throwable) -> {
            // handleRestException(restService, message, throwable);
            return false;
        };
    }

//    public BusErrorCallback busErrorCallback(String restService) {
//        return (message, throwable) -> {
//            handleRestException(restService, message, throwable);
//            return false;
//        };
//    }

    private void handleRestException(String restService, Object message, Throwable throwable) {
        try {
//            if (throwable instanceof ResponseException) {
//                ResponseException responseException = (ResponseException) throwable;
//                if (responseException.getResponse().getStatusCode() == 0) {
//                    if (windowClosing) {
//                        return;
//                    } else {
//                        logger.log(Level.SEVERE, "StatusCode code == 0. " + restService + ": " + message + ". Throwable: " + throwable);
//                        return;
//                    }
//                }
//            }
            logger.log(Level.SEVERE, restService + ": " + message, throwable);
        } catch (Throwable t) {
            handleException(t);
        }
    }
}
