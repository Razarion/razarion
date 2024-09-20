package com.btxtech.server;

import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Ignore;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.util.List;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Ignore
public class ClientArquillianBaseTest {
    public static String HOST_PORT = "192.168.99.100:32778";
    public static String URL = "http://" + HOST_PORT + "/test";
    public static String REST_URL = URL + "/rest/";

    protected String getRestUrl() {
        return REST_URL;
    }

    protected <T> T setupClient(Class<T> clazz) {
        return setupClient(clazz, null);
    }

    protected <T> T setupClient(Class<T> clazz, TestSessionContext testSessionContext) {
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
        return target.proxy(clazz);
    }

    protected RestServerTestHelperAccess setupRestServerTestHelperAccess() {
        return setupClient(RestServerTestHelperAccess.class);
    }

    protected void setupPlanets() {
        setupRestServerTestHelperAccess().setupPlanets();
    }

    protected void cleanUsers() {
        setupRestServerTestHelperAccess().cleanUsers();
    }

    protected void cleanPlanets() {
        setupRestServerTestHelperAccess().cleanPlanets();
    }

    protected void startFakeMailServer() {
        setupRestServerTestHelperAccess().startFakeMailServer();
    }

    protected void stopFakeMailServer() {
        setupRestServerTestHelperAccess().stopFakeMailServer();
    }

    protected List<FakeEmailDto> getMessagesAndClear() {
        return setupRestServerTestHelperAccess().getMessagesAndClear();
    }

    protected String getEmailVerificationUuid(String email) {
        return setupRestServerTestHelperAccess().getEmailVerificationUuid(email);
    }

    protected String getForgotPasswordUuid(String email) {
        return setupRestServerTestHelperAccess().getForgotPasswordUuid(email);
    }

    protected ColdGameUiContext getColdGameUiControlConfig(TestSessionContext testSessionContext) {
        return setupClient(GameUiContextController.class, testSessionContext).loadColdGameUiContext(new GameUiControlInput());
    }

}

