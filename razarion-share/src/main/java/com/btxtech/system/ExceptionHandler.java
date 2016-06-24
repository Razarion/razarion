package com.btxtech.system;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.06.2016.
 */
@Singleton
public class ExceptionHandler {
    private Logger logger = Logger.getLogger(ExceptionHandler.class.getName());

    public void handleException(Throwable t) {
        logger.log(Level.SEVERE, t.getMessage(), t);
    }
}
