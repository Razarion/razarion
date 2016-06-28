package com.btxtech.webglemulator.razarion;

import com.btxtech.system.ExceptionHandler;

/**
 * Created by Beat
 * 28.06.2016.
 */
public class DevToolsExceptionHandlerImpl implements ExceptionHandler {
    @Override
    public void handleException(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void handleException(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace();
    }
}
