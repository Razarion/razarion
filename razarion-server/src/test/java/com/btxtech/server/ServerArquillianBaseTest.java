package com.btxtech.server;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Ignore
@RunWith(Arquillian.class)
public class ServerArquillianBaseTest extends ServerTestHelper {
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
}