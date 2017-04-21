package com.btxtech.server;

import com.btxtech.shared.system.ExceptionHandler;
import org.junit.Assert;

/**
 * Created by Beat
 * 20.04.2017.
 */
public class TestServerExceptionHandler implements ExceptionHandler {
    private Throwable throwable;
    private String message;


    @Override
    public void handleException(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void handleException(String message, Throwable throwable) {
        this.throwable = throwable;
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage() {
        return message;
    }

    public void assertNoException() {
        if (throwable != null || message != null) {
            System.out.println(message);
            throwable.printStackTrace();
            Assert.fail("There was an unexpected exception. See the log");
        }
    }
}
