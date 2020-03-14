package com.btxtech.server.systemtests.fallback;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.rest.FrontendController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FallbackFrontendControllerTest extends AbstractSystemTest {
    private FrontendController frontendController;

    @Before
    public void setup() {
        frontendController = setupRestAccess(FrontendController.class);
    }

    @Test
    public void testLoginUser() {
        frontendController.logout();
        LoginResult loginResult = frontendController.loginUser(RestConnection.NORMAL_USER_EMAIL, RestConnection.NORMAL_USER_PASSWORD, false);
        assertEquals(LoginResult.OK, loginResult);
    }

    @Test
    public void testLoginAdmin() {
        frontendController.logout();
        LoginResult loginResult = frontendController.loginUser(RestConnection.ADMIN_USER_EMAIL, RestConnection.ADMIN_USER_PASSWORD, false);
        assertEquals(LoginResult.OK, loginResult);
    }

}
