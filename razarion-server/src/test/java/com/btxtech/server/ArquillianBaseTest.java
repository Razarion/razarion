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
public class ArquillianBaseTest {
    public static final int BASE_ITEM_TYPE_BULLDOZER_ID = 180807;
    public static final int BASE_ITEM_TYPE_HARVESTER_ID = 180830;
    public static final int BASE_ITEM_TYPE_ATTACKER_ID = 180832;
    public static final int BASE_ITEM_TYPE_FACTORY_ID = 272490;
    public static final int BASE_ITEM_TYPE_TOWER_ID = 272495;
    public static final int RESOURCE_ITEM_TYPE_ID = 180829;

    @Deployment
    public static Archive<?> createDeployment() {
        try {
            File[] libraries = Maven.resolver().loadPomFromFile("./pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();

            WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                    .addPackages(true, "com.btxtech.server")
                    //.as(ExplodedImporter.class).importDirectory((new File("./target/classes"))).as(WebArchive.class)
                    //.as(ExplodedImporter.class).importDirectory((new File("../razarion-share/target/classes"))).as(WebArchive.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                    .addAsLibraries(libraries);
            System.out.println(webArchive.toString(true));
            return webArchive;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }

    }
}
