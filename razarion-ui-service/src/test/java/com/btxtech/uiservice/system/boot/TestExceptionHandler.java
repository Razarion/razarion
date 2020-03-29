package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.ExceptionHandler;
import org.junit.Assert;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestExceptionHandler extends ExceptionHandler {
    public static class TestExceptionHandlerEntry {
        private Throwable throwable;
        private String message;

        public TestExceptionHandlerEntry(Throwable throwable, String message) {
            this.throwable = throwable;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestExceptionHandlerEntry that = (TestExceptionHandlerEntry) o;
            return Objects.equals(throwable, that.throwable) &&
                    Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(throwable, message);
        }
    }

    private List<TestExceptionHandlerEntry> entries = new ArrayList<>();

    @Override
    protected void handleExceptionInternal(String message, Throwable t) {
        System.out.println(message);
        if (t != null) {
            t.printStackTrace();
        }
        entries.add(new TestExceptionHandlerEntry(t, message));
    }

    public void failIfException() {
        if (!entries.isEmpty()) {
            Assert.fail("There are exception. See the log");
        }
    }
}
