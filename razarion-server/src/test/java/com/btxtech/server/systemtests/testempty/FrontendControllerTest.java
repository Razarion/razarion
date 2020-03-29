package com.btxtech.server.systemtests.testempty;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.rest.FrontendController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FrontendControllerTest extends AbstractSystemTest {
    private FrontendController frontendController;

    @Before
    public void setup() {
        frontendController = setupRestAccess(FrontendController.class);
    }

    @Test
    public void testLoginUser() {
        frontendController.logout();
        LoginResult loginResult = frontendController.loginUser(ServerTestHelper.NORMAL_USER_EMAIL, ServerTestHelper.NORMAL_USER_PASSWORD, false);
        assertEquals(LoginResult.OK, loginResult);
    }

    @Test
    public void testLoginAdmin() {
        frontendController.logout();
        LoginResult loginResult = frontendController.loginUser(ServerTestHelper.ADMIN_USER_EMAIL, ServerTestHelper.ADMIN_USER_PASSWORD, false);
        assertEquals(LoginResult.OK, loginResult);
    }

}
