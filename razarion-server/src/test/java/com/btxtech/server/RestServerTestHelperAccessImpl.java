package com.btxtech.server;

import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 08.02.2018.
 */
public class RestServerTestHelperAccessImpl implements RestServerTestHelperAccess {
    @Inject
    private AccessServerTestHelper serverTestHelper;
    @Inject
    private ExceptionHandler exceptionHandler;

    public static class AccessServerTestHelper extends ServerTestHelper {

    }

    @Override
    public void setupPlanets() {
        try {
            serverTestHelper.setupPlanets();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void cleanUsers() {
        try {
            serverTestHelper.cleanUsers();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void cleanPlanets() {
        try {
            serverTestHelper.cleanPlanets();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }
}
