package com.btxtech.test.cdimock;

import com.btxtech.shared.system.ExceptionHandler;
import org.junit.Assert;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestExceptionHandler implements ExceptionHandler {
    public static class TestExceptionHandlerEntry {
        private Throwable throwable;
        private String message;

        public TestExceptionHandlerEntry(Throwable throwable) {
            this.throwable = throwable;
        }

        public TestExceptionHandlerEntry(Throwable throwable, String message) {
            this.throwable = throwable;
            this.message = message;
        }
    }

    private List<TestExceptionHandlerEntry> entries = new ArrayList<>();

    @Override
    public void handleException(Throwable t) {
        t.printStackTrace();
        entries.add(new TestExceptionHandlerEntry(t));
    }

    @Override
    public void handleException(String message, Throwable t) {
        System.out.println("message: " + message);
        handleException(t);
        entries.add(new TestExceptionHandlerEntry(t, message));
    }

    public void failIfException() {
        if (!entries.isEmpty()) {
            Assert.fail("There are exception. See the log");
        }
    }
}
