package com.btxtech.server;

import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.rest.FrontendProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * Created by Beat
 * 05.05.2017.
 */
public abstract class RestServerTestBase extends ServerTestHelper {
    public static String URL = "http://localhost:32778";
    public static String REST_URL = URL + "/rest/";
    private ResteasyWebTarget target;
    private boolean loggedIn = false;

    @After
    public void cleanup() {
        if (loggedIn) {
            logout();
        }
    }

    public RestServerTestBase() {
        TestSessionContext testSessionContext = new TestSessionContext();
        Client client = ClientBuilder.newClient();
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

    protected void login(String email, String password) {
        LoginResult loginResult = target.proxy(FrontendProvider.class).loginUser(email, password, false);
        if (loginResult != LoginResult.OK) {
            throw new AssertionError("Can not login with email: " + email + " and password: " + password + ". Result: " + loginResult);
        }
        loggedIn = true;
    }

    protected void logout() {
        target.proxy(FrontendProvider.class).logout();
        loggedIn = false;
    }

    protected <T> T setupRestAccess(Class<T> clazz) {
        return target.proxy(clazz);
    }
}