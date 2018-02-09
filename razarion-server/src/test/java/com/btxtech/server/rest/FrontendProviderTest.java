package com.btxtech.server.rest;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.FakeEmailDto;
import com.btxtech.server.frontend.FrontendLoginState;
import com.btxtech.server.frontend.LoginResult;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.user.NewUser;
import com.btxtech.server.user.RegisterResult;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiControlProvider;
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
        stopFakeMailServer();
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

    @Test
    public void testEmailUser() {
        startFakeMailServer();

        // Test not logged in
        RestContext restContext = new RestContext().setAcceptLanguage("de-DE");
        FrontendProvider frontendProvider = setupClient(FrontendProvider.class, restContext);
        FrontendLoginState frontendLoginState = frontendProvider.isLoggedIn("");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals("de_DE", frontendLoginState.getLanguage());
        // Register
        RegisterResult registerResult = frontendProvider.createUnverifiedUser("xxx@yyy.com", "123456789", true);
        Assert.assertEquals(RegisterResult.OK, registerResult);
        // Verify UserContext
        GameUiControlProvider gameUiControlProvider = restContext.proxy(GameUiControlProvider.class);
        UserContext userContext = gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.checkRegistered());
        Assert.assertTrue(userContext.isEmailNotVerified());
        // verify email
        List<FakeEmailDto> mails = getMessagesAndClear();
        Assert.assertEquals(1, mails.size());
        Assert.assertEquals("xxx@yyy.com", mails.get(0).getEnvelopeReceiver());
        Assert.assertEquals("no-reply@razarion.com", mails.get(0).getEnvelopeSender());
        // TODO why das this not work? Assert.assertEquals("Razarion - Please confirm your Email address", mails.get(0).getSubject());
        Assert.assertEquals("text/html; charset=UTF-8", mails.get(0).getContentType());
        String uuid = getEmailVerificationUuid("xxx@yyy.com");
        Assert.assertEquals("<html><body><h3>HalloundherzlichwillkommenbeiRazarion</h3><div>VielenDankfürdieRegistrierungbeiRazarion.BittebestätigedeineE-Mail-Adresse,indemduaufdenfolgendenLinkklickst:<br><ahref=\"https://www.razarion.com/verify-email/" + uuid + "\">https://www.razarion.com/verify-email/" + uuid + "</a><br><br><br>Wirfreuenunsdarauf,dichbeiRazarionbegrüssenzudürfen!<br><br>DeinRazarion-Team</div></body></html>", mails.get(0).getContent().replaceAll("\\s", ""));
        // Click wrong activation link
        Assert.assertFalse(frontendProvider.verifyEmailLink(uuid + "xxxxxxx"));
        // Verify UserContext
        userContext = gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.checkRegistered());
        Assert.assertTrue(userContext.isEmailNotVerified());
        // Click activation link
        Assert.assertTrue(frontendProvider.verifyEmailLink(uuid));
        // Verify UserContext
        userContext = gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.checkRegistered());
        Assert.assertFalse(userContext.isEmailNotVerified());
        // Logout
        frontendProvider.logout();
        // Verify
        Assert.assertFalse(frontendProvider.isLoggedIn("").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext().checkRegistered());
        // Login wrong password
        Assert.assertEquals(LoginResult.WRONG_PASSWORD, frontendProvider.loginUser("xxx@yyy.com", "qwerttzz", true));
        Assert.assertFalse(frontendProvider.isLoggedIn("").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext().checkRegistered());
        // Login wrong email
        Assert.assertEquals(LoginResult.WRONG_EMAIL, frontendProvider.loginUser("qqqq@yyy.com", "qwerttzz", true));
        Assert.assertFalse(frontendProvider.isLoggedIn("").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext().checkRegistered());
        // Login
        Assert.assertEquals(LoginResult.OK, frontendProvider.loginUser("xxx@yyy.com", "123456789", true));
        Assert.assertTrue(frontendProvider.isLoggedIn("").isLoggedIn());
        Assert.assertTrue(gameUiControlProvider.loadGameUiControlConfig(new GameUiControlInput()).getUserContext().checkRegistered());
        // Logout
        frontendProvider.logout();
        // Verify
        BackendProvider backendProvider = setupClient(BackendProvider.class);
        List<NewUser> newUsers = backendProvider.newUsers();
        Assert.assertEquals(1, newUsers.size());
        UserBackendInfo userBackendInfo = backendProvider.loadBackendUserInfo(newUsers.get(0).getPlayerId());
        Assert.assertEquals("xxx@yyy.com", userBackendInfo.getEmail());
        Assert.assertEquals(4, setupClient(BackendProvider.class).userHistory().size());
    }
}