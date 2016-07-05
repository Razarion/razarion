package com.btxtech.shared.system;

/**
 * Created by Beat
 * 24.06.2016.
 */
public interface ExceptionHandler {
    void handleException(Throwable t);

    void handleException(String message, Throwable t);
}
