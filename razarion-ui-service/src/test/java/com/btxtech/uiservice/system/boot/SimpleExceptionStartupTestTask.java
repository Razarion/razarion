package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.02.2010
 * Time: 12:50:50
 */
public class SimpleExceptionStartupTestTask extends AbstractStartupTask {
    public static final String ERROR_STRING = "crash in SimpleExceptionStartupTestTask";
    public static final RuntimeException EXCEPTION = new RuntimeException(ERROR_STRING);

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        throw EXCEPTION;
    }

}
