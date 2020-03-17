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
    public static String URL = "http://localhost:32778";
    public static String REST_URL = URL + "/rest/";
    private boolean loggedIn = false;
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
        login(ServerTestHelper.ADMIN_USER_EMAIL, ServerTestHelper.ADMIN_USER_PASSWORD);
    }

    public void loginUser() {
        login(ServerTestHelper.NORMAL_USER_EMAIL, ServerTestHelper.NORMAL_USER_PASSWORD);
    }

    public void login(String email, String password) {
        logout();
        LoginResult loginResult = target.proxy(FrontendController.class).loginUser(email, password, false);
        if (loginResult != LoginResult.OK) {
            throw new AssertionError("Can not login with email: " + email + " and password: " + password + ". Result: " + loginResult);
        }
        loggedIn = true;
    }

    public void logout() {
        if (!loggedIn) {
            return;
        }
        target.proxy(FrontendController.class).logout();
        loggedIn = false;
    }

    public <T> T proxy(Class<T> clazz) {
        return target.proxy(clazz);
    }
}
