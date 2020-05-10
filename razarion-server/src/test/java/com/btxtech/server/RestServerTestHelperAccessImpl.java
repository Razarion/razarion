package com.btxtech.server;

import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 08.02.2018.
 */
public class RestServerTestHelperAccessImpl implements RestServerTestHelperAccess {
    @Inject
    private AccessServerTestHelper serverTestHelper;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private FakeEmailServer fakeEmailServer;

    public static class AccessServerTestHelper extends ServerTestHelper {

    }

    @Override
    public void setupPlanets() {
        try {
            serverTestHelper.setupPlanetDb();
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

    @Override
    public void startFakeMailServer() {
        try {
            fakeEmailServer.startFakeMailServer();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void stopFakeMailServer() {
        try {
            fakeEmailServer.stopFakeMailServer();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public List<FakeEmailDto> getMessagesAndClear() {
        try {
            return fakeEmailServer.getMessagesAndClear();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public String getEmailVerificationUuid(String email) {
        try {
            return serverTestHelper.getEmailVerificationUuid(email);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public String getForgotPasswordUuid(String email) {
        try {
            return serverTestHelper.getForgotPasswordUuid(email);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new RuntimeException(t);
        }
    }
}
