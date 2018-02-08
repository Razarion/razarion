package com.btxtech.server;

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
import java.io.File;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Ignore
@RunWith(Arquillian.class)
public class ClientArquillianBaseTest {
    private static String REST_URL = "http://192.168.99.100:32778/test/rest/";

    @Deployment
    public static Archive<?> createDeployment() {
        try {
            // Do not ad weld-core dependency for deproxy
            File[] libraries = Maven.resolver().loadPomFromFile("./pom.xml").importRuntimeDependencies().resolve("org.unitils:unitils-core:4.0-SNAPSHOT", "org.easymock:easymock:3.4").withTransitivity().asFile();

            WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                    .addPackages(true, "com.btxtech.server")
                    //.as(ExplodedImporter.class).importDirectory((new File("./target/classes"))).as(WebArchive.class)
                    //.as(ExplodedImporter.class).importDirectory((new File("../razarion-share/target/classes"))).as(WebArchive.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                    .addAsResource("mongodb/PlanetBackup.json", "mongodb/PlanetBackup.json")
                    .addAsResource("mongodb/ServerItemTracking.json", "mongodb/ServerItemTracking.json")
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
        Client client = ClientBuilder.newClient();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(REST_URL);
        return target.proxy(clazz);
    }

    protected void setupPlanets() {
        setupClient(RestServerTestHelperAccess.class).setupPlanets();
    }

    protected void cleanUsers() {
        setupClient(RestServerTestHelperAccess.class).cleanUsers();
    }

    protected void cleanPlanets() {
        setupClient(RestServerTestHelperAccess.class).cleanPlanets();
    }

}

