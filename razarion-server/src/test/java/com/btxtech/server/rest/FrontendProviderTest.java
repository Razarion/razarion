package com.btxtech.server.rest;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.ServerArquillianBaseTest;
import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.shared.datatypes.FbAuthResponse;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Created by Beat
 * on 07.02.2018.
 */
@RunAsClient
public class FrontendProviderTest extends ClientArquillianBaseTest {
    protected static String REST_URL = "http://192.168.99.100:32778/test/rest/";

    @Before
    public void before() throws Exception {
         setupPlanets();
    }

    @After
    public void after() throws Exception {
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
    }
}