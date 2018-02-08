package com.btxtech.server.rest;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.user.NewUser;
import com.btxtech.shared.datatypes.FbAuthResponse;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Beat
 * on 07.02.2018.
 */
@RunAsClient
public class FrontendProviderTest extends ClientArquillianBaseTest {
    protected static String REST_URL = "http://192.168.99.100:32778/test/rest/";

    @Before
    public void before() {
        setupPlanets();
    }

    @After
    public void after() {
        cleanUsers();
        cleanPlanets();
    }

    @Test
    public void testFacebookUser() {
        // Test not logged in
        FrontendProvider frontendProvider = setupClient(FrontendProvider.class);
        FrontendLoginState frontendLoginState = frontendProvider.isLoggedIn("");
        frontendProvider.isLoggedIn("");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        // Login facebook
        Assert.assertTrue(frontendProvider.facebookAuthenticated(new FbAuthResponse().setUserID("000000012")));
        frontendLoginState = (FrontendLoginState) frontendProvider.isLoggedIn("");
        Assert.assertTrue(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        // Logout
        frontendProvider.logout();
        // Test not logged in
        frontendLoginState = (FrontendLoginState) frontendProvider.isLoggedIn("");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        // Login same user facebook
        Assert.assertTrue(frontendProvider.facebookAuthenticated(new FbAuthResponse().setUserID("000000012")));
        frontendLoginState = (FrontendLoginState) frontendProvider.isLoggedIn("");
        Assert.assertTrue(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        // Logout
        frontendProvider.logout();
        // Verify
        BackendProvider backendProvider = setupClient(BackendProvider.class);
        List<NewUser> newUsers = backendProvider.newUsers();
        Assert.assertEquals(1, newUsers.size());
        UserBackendInfo userBackendInfo = backendProvider.loadBackendUserInfo(newUsers.get(0).getPlayerId());
        Assert.assertEquals("000000012", userBackendInfo.getFacebookId());
        Assert.assertEquals(4, setupClient(BackendProvider.class).userHistory().size());
    }
}