package com.btxtech.server;

import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.shared.dto.LoginResult;
import com.btxtech.shared.rest.FrontendProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

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
    // private SessionFactory sessionFactory;

    protected <T> T setupRestAccess(Class<T> clazz) {
        TestSessionContext testSessionContext = new TestSessionContext();
        Client client = ClientBuilder.newClient();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(REST_URL);
        if (testSessionContext != null) {
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
        LoginResult loginResult = target.proxy(FrontendProvider.class).loginUser("admin@admin.com", "test", false);
        System.out.println("loginResult: " + loginResult);
        return target.proxy(clazz);
    }
}