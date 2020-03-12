package com.btxtech.server.user;

import com.btxtech.server.ClientArquillianBaseTest;
import com.btxtech.server.FakeEmailDto;
import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.server.clienthelper.WebsocketTestHelper;
import com.btxtech.shared.dto.FrontendLoginState;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.rest.BackendProvider;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.rest.FrontendController;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.List;

/**
 * Created by Beat
 * on 07.02.2018.
 */
@RunAsClient
public class FrontendControllerTest extends ClientArquillianBaseTest {
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
        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage("en-US");
        FrontendController frontendController = setupClient(FrontendController.class, testSessionContext);
        FrontendLoginState frontendLoginState = frontendController.isLoggedIn("", "");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        GameUiContextController gameUiControlProvider = testSessionContext.proxy(GameUiContextController.class);
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login facebook
        Assert.assertTrue(frontendController.facebookAuthenticated(new FbAuthResponse().setUserID("000000012")));
        frontendLoginState = frontendController.isLoggedIn("", "");
        Assert.assertTrue(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        // Test not logged in
        frontendLoginState = frontendController.isLoggedIn("", "");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login same user facebook
        Assert.assertTrue(frontendController.facebookAuthenticated(new FbAuthResponse().setUserID("000000012")));
        frontendLoginState = (FrontendLoginState) frontendController.isLoggedIn("", "");
        Assert.assertTrue(frontendLoginState.isLoggedIn());
        Assert.assertEquals("en_US", frontendLoginState.getLanguage());
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        // Verify
        BackendProvider backendProvider = setupClient(BackendProvider.class);
        List<NewUser> newUsers = backendProvider.newUsers();
        Assert.assertEquals(1, newUsers.size());
        UserBackendInfo userBackendInfo = backendProvider.loadBackendUserInfo(newUsers.get(0).getPlayerId());
        Assert.assertEquals("000000012", userBackendInfo.getFacebookId());
        Assert.assertEquals(4, setupClient(BackendProvider.class).userHistory().size());
    }

    @Test
    public void testEmailUserDe() {
        testEmailUser("de-DE", "de_DE", "Razarion - Please confirm your Email address",
                "<html><body><h3>HalloundherzlichwillkommenbeiRazarion</h3><div>VielenDankfürdieRegistrierungbeiRazarion.BittebestätigedeineE-Mail-Adresse,indemduaufdenfolgendenLinkklickst:<br><ahref=\"https://www.razarion.com/verify-email/",
                "\">https://www.razarion.com/verify-email/",
                "</a><br><br><br>Wirfreuenunsdarauf,dichbeiRazarionbegrüssenzudürfen!<br><br>DeinRazarion-Team</div></body></html>");
    }

    @Test
    public void testEmailUserEn() {
        testEmailUser("en-Us", "en_US", "Razarion - Please confirm your Email address",
                "<html><body><h3>HelloandwelcometoRazarion</h3><div>ThankyouforregisteringatRazarion.Pleasefollowthelinkbelowtoconfirmyouremailaddress:<br><ahref=\"https://www.razarion.com/verify-email/",
                "\">https://www.razarion.com/verify-email/",
                "</a><br><br><br>WearepleasedtobeabletowelcomeyoutoRazarion<br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    @Test
    public void testEmailUserJp() {
        testEmailUser("ja-JP", "ja_JP", "Razarion - Please confirm your Email address",
                "<html><body><h3>HelloandwelcometoRazarion</h3><div>ThankyouforregisteringatRazarion.Pleasefollowthelinkbelowtoconfirmyouremailaddress:<br><ahref=\"https://www.razarion.com/verify-email/",
                "\">https://www.razarion.com/verify-email/",
                "</a><br><br><br>WearepleasedtobeabletowelcomeyoutoRazarion<br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    @Test
    public void testEmailUserUnknown() {
        testEmailUser("xxx", "xxx", "Razarion - Please confirm your Email address",
                "<html><body><h3>HelloandwelcometoRazarion</h3><div>ThankyouforregisteringatRazarion.Pleasefollowthelinkbelowtoconfirmyouremailaddress:<br><ahref=\"https://www.razarion.com/verify-email/",
                "\">https://www.razarion.com/verify-email/",
                "</a><br><br><br>WearepleasedtobeabletowelcomeyoutoRazarion<br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    private void testEmailUser(String languageIn, String languageExpected, String subject, String messageBodyPart1, String messageBodyPart2, String messageBodyPart3) {
        startFakeMailServer();

        // Test not logged in
        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage(languageIn);
        FrontendController frontendController = setupClient(FrontendController.class, testSessionContext);
        FrontendLoginState frontendLoginState = frontendController.isLoggedIn("", "");
        Assert.assertFalse(frontendLoginState.isLoggedIn());
        Assert.assertEquals(languageExpected, frontendLoginState.getLanguage());
        GameUiContextController gameUiControlProvider = testSessionContext.proxy(GameUiContextController.class);
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Register
        RegisterResult registerResult = frontendController.createUnverifiedUser("xxx@yyy.com", "123456789", false);
        Assert.assertEquals(RegisterResult.OK, registerResult);
        Assert.assertNull(testSessionContext.getLoginTokenCookie());
        // Verify UserContext
        UserContext userContext = gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.isRegistered());
        Assert.assertTrue(userContext.isEmailNotVerified());
        // verify email
        List<FakeEmailDto> mails = getMessagesAndClear();
        Assert.assertEquals(1, mails.size());
        Assert.assertEquals("xxx@yyy.com", mails.get(0).getEnvelopeReceiver());
        Assert.assertEquals("no-reply@razarion.com", mails.get(0).getEnvelopeSender());
        // TODO why das this not work? Assert.assertEquals(subject, mails.get(0).getSubject());
        Assert.assertEquals("text/html; charset=UTF-8", mails.get(0).getContentType());
        String uuid = getEmailVerificationUuid("xxx@yyy.com");
        Assert.assertEquals(messageBodyPart1 + uuid + messageBodyPart2 + uuid + messageBodyPart3, mails.get(0).getContent().replaceAll("\\s", ""));
        // Click wrong activation link
        Assert.assertFalse(frontendController.verifyEmailLink(uuid + "xxxxxxx"));
        // Verify UserContext
        userContext = gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.isRegistered());
        Assert.assertTrue(userContext.isEmailNotVerified());
        // Click activation link
        Assert.assertTrue(frontendController.verifyEmailLink(uuid));
        // Verify UserContext
        userContext = gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext.isRegistered());
        Assert.assertFalse(userContext.isEmailNotVerified());
        // Logout
        frontendController.logout();
        // Verify
        Assert.assertFalse(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login wrong password
        Assert.assertEquals(LoginResult.WRONG_PASSWORD, frontendController.loginUser("xxx@yyy.com", "qwerttzz", false));
        Assert.assertFalse(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        Assert.assertNull(testSessionContext.getLoginTokenCookie());
        // Login wrong email
        Assert.assertEquals(LoginResult.WRONG_EMAIL, frontendController.loginUser("qqqq@yyy.com", "qwerttzz", false));
        Assert.assertFalse(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        Assert.assertNull(testSessionContext.getLoginTokenCookie());
        // Login
        Assert.assertEquals(LoginResult.OK, frontendController.loginUser("xxx@yyy.com", "123456789", false));
        Assert.assertTrue(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        Assert.assertNull(testSessionContext.getLoginTokenCookie());
        // Logout
        frontendController.logout();
        // Verify
        BackendProvider backendProvider = setupClient(BackendProvider.class);
        List<NewUser> newUsers = backendProvider.newUsers();
        Assert.assertEquals(1, newUsers.size());
        UserBackendInfo userBackendInfo = backendProvider.loadBackendUserInfo(newUsers.get(0).getPlayerId());
        Assert.assertEquals("xxx@yyy.com", userBackendInfo.getEmail());
        Assert.assertEquals(4, setupClient(BackendProvider.class).userHistory().size());
    }

    @Test
    public void testEmailActivationWrongUser() {
        // TODO Fails due to websocket and httpSessionId problem. Fix sending httpsessionid over WebSocket
        startFakeMailServer();

        // Register user 1
        TestSessionContext testSessionContext1 = new TestSessionContext().setAcceptLanguage("en-Us");
        FrontendController frontendController1 = setupClient(FrontendController.class, testSessionContext1);
        Assert.assertEquals(RegisterResult.OK, frontendController1.createUnverifiedUser("xxx@yyy.com", "123456789", false));
        WebsocketTestHelper websocketTestHelper1 = new WebsocketTestHelper(CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT, testSessionContext1);
        // Register user 2
        TestSessionContext testSessionContext2 = new TestSessionContext().setAcceptLanguage("en-Us");
        FrontendController frontendController2 = setupClient(FrontendController.class, testSessionContext2);
        Assert.assertEquals(RegisterResult.OK, frontendController2.createUnverifiedUser("xx1@yyy.com", "123456789", false));
        WebsocketTestHelper websocketTestHelper2 = new WebsocketTestHelper(CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT, testSessionContext2);

        System.out.println("testSessionContext1: " + testSessionContext1);
        System.out.println("testSessionContext2: " + testSessionContext2);

        // User 1 login and activate user 2
        Assert.assertTrue(frontendController1.isLoggedIn("", "").isLoggedIn());
        Assert.assertTrue(frontendController1.verifyEmailLink(getEmailVerificationUuid("xx1@yyy.com")));
        UserContext userContext1 = testSessionContext1.proxy(GameUiContextController.class).loadColdGameUiContext(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext1.isRegistered());
        Assert.assertTrue(userContext1.isEmailNotVerified());
        // User 2 login and activate user 2
        Assert.assertTrue(frontendController2.isLoggedIn("", "").isLoggedIn());
        UserContext userContext2 = testSessionContext2.proxy(GameUiContextController.class).loadColdGameUiContext(new GameUiControlInput()).getUserContext();
        Assert.assertTrue(userContext2.isRegistered());
        Assert.assertFalse(userContext2.isEmailNotVerified());

        websocketTestHelper2.waitForDelivery();
        websocketTestHelper2.getWebsocketMessageHelper().assertMessageSent(0, "EMAIL_VERIFIED#null");
        websocketTestHelper1.assertNoDelivery(5000);
    }

    @Test
    public void testLoginToken() {
        startFakeMailServer();
        // Register
        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage("de_DE");
        FrontendController frontendController = setupClient(FrontendController.class, testSessionContext);
        RegisterResult registerResult = frontendController.createUnverifiedUser("xxx@yyy.com", "123456789", true);
        Assert.assertEquals(RegisterResult.OK, registerResult);
        // Verify login token cookie
        Assert.assertNotNull(testSessionContext.getLoginTokenCookie());
        Cookie loginCookie1 = testSessionContext.getLoginTokenCookie();
        GameUiContextController gameUiControlProvider = testSessionContext.proxy(GameUiContextController.class);
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with no login token cookie
        testSessionContext.setLoginTokenCookie(null);
        Assert.assertFalse(frontendController.isLoggedIn("", "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with wrong login token cookie
        NewCookie wrongCookie = new NewCookie(loginCookie1.getName(), "xxxxxxxx" + loginCookie1.getValue(), loginCookie1.getPath(), loginCookie1.getDomain(), loginCookie1.getVersion(), null, -1, false);
        testSessionContext.setLoginTokenCookie(wrongCookie);
        Assert.assertFalse(frontendController.isLoggedIn("xxxxxxxx" + loginCookie1.getValue(), "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with login token cookie
        testSessionContext.setLoginTokenCookie(loginCookie1);
        Assert.assertTrue(frontendController.isLoggedIn(loginCookie1.getValue(), "").isLoggedIn());
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with same login token cookie
        testSessionContext.setLoginTokenCookie(loginCookie1);
        Assert.assertFalse(frontendController.isLoggedIn(loginCookie1.getValue(), "").isLoggedIn());
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login email & password
        testSessionContext.setLoginTokenCookie(null);
        Assert.assertEquals(LoginResult.OK, frontendController.loginUser("xxx@yyy.com", "123456789", true));
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        Assert.assertNotNull(testSessionContext.getLoginTokenCookie());
        Cookie loginCookie2 = testSessionContext.getLoginTokenCookie();
        // Logout
        frontendController.logout();
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with login token cookie
        testSessionContext.setLoginTokenCookie(loginCookie2);
        Assert.assertTrue(frontendController.isLoggedIn(loginCookie2.getValue(), "").isLoggedIn());
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
    }

    @Test
    public void testPasswordResetDe() {
        testPasswordReset("de-DE", "Razarion - Konto Passworthilfe",
                "<html><body><div>DuerhältstdiesesEmail,weildueinneuesPasswortfürdeinRazarionKontobeantragthast.FallsDukeinneuesPasswordbeantragthast,kannstdudieseEmailignorieren.<br><br>UmdenVorgangabzuschliessen,klickebitteaufdenLink:<br><ahref=\"https://www.razarion.com/change-password/",
                "\">https://www.razarion.com/change-password/",
                "</a><br><br><br>DeinRazarion-Team</div></body></html>");
    }

    @Test
    public void testPasswordResetEn() {
        testPasswordReset("en-US", "Razarion - Account password help",
                "<html><body><div>You'rereceivingthisemailbecauseyourequestedapasswordresetforyourRazarionaccount.Ifyoudidnotrequestthischange,youcansafelyignorethisemail.<br><br>Tochooseanewpasswordandcompleteyourrequest,pleasefollowthelinkbelow:<br><ahref=\"https://www.razarion.com/change-password/",
                "\">https://www.razarion.com/change-password/",
                "</a><br><br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    @Test
    public void testPasswordResetJp() {
        testPasswordReset("ja-JP", "Razarion - Account password help",
                "<html><body><div>You'rereceivingthisemailbecauseyourequestedapasswordresetforyourRazarionaccount.Ifyoudidnotrequestthischange,youcansafelyignorethisemail.<br><br>Tochooseanewpasswordandcompleteyourrequest,pleasefollowthelinkbelow:<br><ahref=\"https://www.razarion.com/change-password/",
                "\">https://www.razarion.com/change-password/",
                "</a><br><br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    @Test
    public void testPasswordResetUnknwon() {
        testPasswordReset("wwwwwww", "Razarion - Account password help",
                "<html><body><div>You'rereceivingthisemailbecauseyourequestedapasswordresetforyourRazarionaccount.Ifyoudidnotrequestthischange,youcansafelyignorethisemail.<br><br>Tochooseanewpasswordandcompleteyourrequest,pleasefollowthelinkbelow:<br><ahref=\"https://www.razarion.com/change-password/",
                "\">https://www.razarion.com/change-password/",
                "</a><br><br><br>WithkindregardsyourRazarionteam</div></body></html>");
    }

    public void testPasswordReset(String languageIn, String subject, String messageBodyPart1, String messageBodyPart2, String messageBodyPart3) {
        startFakeMailServer();
        // Register
        TestSessionContext testSessionContext = new TestSessionContext().setAcceptLanguage(languageIn);
        FrontendController frontendController = setupClient(FrontendController.class, testSessionContext);
        RegisterResult registerResult = frontendController.createUnverifiedUser("xxx@yyy.com", "123456789", false);
        Assert.assertEquals(RegisterResult.OK, registerResult);
        getMessagesAndClear().size();
        frontendController.logout();
        // Wrong password reset
        Assert.assertFalse(frontendController.sendEmailForgotPassword("xxx@yy11.com"));
        Assert.assertEquals(0, getMessagesAndClear().size());
        // Password reset
        Assert.assertTrue(frontendController.sendEmailForgotPassword("xxx@yyy.com"));
        List<FakeEmailDto> mails = getMessagesAndClear();
        Assert.assertEquals(1, mails.size());
        Assert.assertEquals("xxx@yyy.com", mails.get(0).getEnvelopeReceiver());
        Assert.assertEquals("no-reply@razarion.com", mails.get(0).getEnvelopeSender());
        Assert.assertEquals(subject, mails.get(0).getSubject());
        Assert.assertEquals("text/html; charset=UTF-8", mails.get(0).getContentType());
        String uuid = getForgotPasswordUuid("xxx@yyy.com");
        Assert.assertEquals(messageBodyPart1 + uuid + messageBodyPart2 + uuid + messageBodyPart3, mails.get(0).getContent().replaceAll("\\s", ""));
        // Check wrong uuid
        Assert.assertFalse(frontendController.savePassword(uuid + "qefdewfdswf", "asdasdasdasd"));
        GameUiContextController gameUiControlProvider = testSessionContext.proxy(GameUiContextController.class);
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with new password
        Assert.assertEquals(LoginResult.WRONG_PASSWORD, frontendController.loginUser("xxx@yyy.com", "asdasdasdasd", false));
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Change password
        Assert.assertTrue(frontendController.savePassword(uuid, "987654321"));
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with old password
        Assert.assertEquals(LoginResult.WRONG_PASSWORD, frontendController.loginUser("xxx@yyy.com", "123456789", false));
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Login with new password
        Assert.assertEquals(LoginResult.OK, frontendController.loginUser("xxx@yyy.com", "987654321", false));
        Assert.assertTrue(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Logout
        frontendController.logout();
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
        // Check old uuid
        Assert.assertFalse(frontendController.savePassword(uuid, "qwerwerqwr"));
        Assert.assertFalse(gameUiControlProvider.loadColdGameUiContext(new GameUiControlInput()).getUserContext().isRegistered());
    }
}