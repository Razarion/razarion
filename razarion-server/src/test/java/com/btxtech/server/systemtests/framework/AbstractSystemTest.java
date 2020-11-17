package com.btxtech.server.systemtests.framework;

import com.btxtech.server.ServerTestHelper;
import org.junit.Assert;

import javax.ws.rs.NotAuthorizedException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 05.05.2017.
 */
public abstract class AbstractSystemTest extends ServerTestHelper {
    private RestConnection defaultRestConnection = new RestConnection(null);

    public <T> void runUnauthorizedTest(Class<T> testClass, Consumer<T> underTestConsumer, RestConnection.TestUser... unauthorizedUsers) {
        T underTest = defaultRestConnection.proxy(testClass);
        Arrays.stream(unauthorizedUsers).forEach(unauthorizedUser -> {
            defaultRestConnection.login(unauthorizedUser);
            try {
                underTestConsumer.accept(underTest);
                Assert.fail("NotAuthorizedException expected for " + unauthorizedUser);
            } catch (NotAuthorizedException e) {
                // Ignore
            }
        });
    }

    protected <T> T setupRestAccess(Class<T> clazz) {
        return defaultRestConnection.proxy(clazz);
    }

    public RestConnection getDefaultRestConnection() {
        return defaultRestConnection;
    }

}