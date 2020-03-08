package com.btxtech.server;

import com.btxtech.server.clienthelper.TestSessionContext;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiControlController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.File;
import java.util.List;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Ignore
@RunWith(Arquillian.class)  // Ignores tests
public class ClientArquillianBaseTest {
    public static String HOST_PORT = "192.168.99.100:32778";
    public static String URL = "http://" + HOST_PORT + "/test";
    public static String REST_URL = URL + "/rest/";

    @Deployment
    public static Archive<?> createDeployment() {
        try {
            // Do not ad weld-core dependency for deproxy
            File[] libraries = Maven.resolver().loadPomFromFile("./pom.xml").importRuntimeDependencies().resolve("org.unitils:unitils-core:4.0-SNAPSHOT", "org.easymock:easymock:3.4", "org.subethamail:subethasmtp:3.1.7").withTransitivity().asFile();

            WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                    .addPackages(true, "com.btxtech.server")
                    //.as(ExplodedImporter.class).importDirectory((new File("./target/classes"))).as(WebArchive.class)
                    //.as(ExplodedImporter.class).importDirectory((new File("../razarion-share/target/classes"))).as(WebArchive.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                    .addAsResource("mongodb/PlanetBackup.json", "mongodb/PlanetBackup.json")
                    .addAsResource("mongodb/ServerItemTracking.json", "mongodb/ServerItemTracking.json")
                    .addAsResource("templates", "templates")
                    .addAsResource("Razarion.properties", "Razarion.properties")
                    .addAsResource("Razarion_de.properties", "Razarion_de.properties")
                    .addAsLibraries(libraries);
            System.out.println(webArchive.toString(true));
            return webArchive;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
    }

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

    protected ColdGameUiControlConfig getColdGameUiControlConfig(TestSessionContext testSessionContext) {
        return setupClient(GameUiControlController.class, testSessionContext).loadGameUiControlConfig(new GameUiControlInput());
    }

}

