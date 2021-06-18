package com.btxtech.server.systemtests.framework;

import com.btxtech.server.ServerTestHelper;
import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.rest.FrontendController;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.ContextResolver;

public class RestConnection {
    public enum TestUser {
        NONE(null, null),
        USER(ServerTestHelper.NORMAL_USER_EMAIL, ServerTestHelper.NORMAL_USER_PASSWORD),
        ADMIN(ServerTestHelper.ADMIN_USER_EMAIL, ServerTestHelper.ADMIN_USER_PASSWORD);

        private final String email;
        private final String password;

        TestUser(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    public static String URL = "http://localhost:32778";
    public static String REST_URL = URL + "/rest/";
    private TestUser loggedIn = TestUser.NONE;
    private ResteasyWebTarget target;

    public RestConnection(ContextResolver contextResolver) {
        TestSessionContext testSessionContext = new TestSessionContext();
        Client client = ClientBuilder.newClient();
        if (contextResolver != null) {
            client.register(contextResolver);
        }
        target = (ResteasyWebTarget) client.target(REST_URL);
        client.register((ClientRequestFilter) requestContext -> requestContext.getHeaders().add("Accept-Language", testSessionContext.getAcceptLanguage()));
        client.register((ClientResponseFilter) (requestContext, responseContext) -> {
            if (responseContext.getCookies().containsKey("JSESSIONID")) {
                testSessionContext.setSessionCookie(responseContext.getCookies().get("JSESSIONID"));
            }
            if (responseContext.getCookies().containsKey("LoginToken")) {
                testSessionContext.setLoginTokenCookie(responseContext.getCookies().get("LoginToken"));
            }
        });
        client.register((ClientRequestFilter) (requestContext) -> {
            if (testSessionContext.getSessionCookie() != null) {
                requestContext.getCookies().put("JSESSIONID", testSessionContext.getSessionCookie());
            }
            if (testSessionContext.getLoginTokenCookie() != null) {
                requestContext.getCookies().put("LoginToken", testSessionContext.getLoginTokenCookie());
            }
        });
        testSessionContext.setTarget(target);
    }

    public void loginAdmin() {
        login(TestUser.ADMIN);
    }

    public void loginUser() {
        login(TestUser.USER);
    }

    public void login(TestUser testUser) {
        if(testUser == loggedIn) {
            return;
        }
        logout();
        if (testUser != TestUser.NONE) {
            LoginResult loginResult = target.proxy(FrontendController.class).loginUser(testUser.email, testUser.password, false);
            if (loginResult != LoginResult.OK) {
                throw new AssertionError("Can not login with email: " + testUser.email + " and password: " + testUser.password + ". Result: " + loginResult);
            }
        }
        loggedIn = testUser;
    }

    public void logout() {
        if (loggedIn == TestUser.NONE) {
            return;
        }
        target.proxy(FrontendController.class).logout();
        loggedIn = TestUser.NONE;
    }

    public <T> T proxy(Class<T> clazz) {
        return target.proxy(clazz);
    }

    public ResteasyWebTarget getTarget() {
        return target;
    }
}
