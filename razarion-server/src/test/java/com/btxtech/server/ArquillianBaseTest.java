package com.btxtech.server;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Ignore
@RunWith(Arquillian.class)
public class ArquillianBaseTest {
    // Item types
    public static int BASE_ITEM_TYPE_BULLDOZER_ID;
    public static int BASE_ITEM_TYPE_HARVESTER_ID;
    public static int BASE_ITEM_TYPE_ATTACKER_ID;
    public static int BASE_ITEM_TYPE_FACTORY_ID;
    public static int BASE_ITEM_TYPE_TOWER_ID;
    public static int RESOURCE_ITEM_TYPE_ID;
    // Levels
    public static int LEVEL_1_ID;
    public static int LEVEL_2_ID;
    public static int LEVEL_3_ID;
    public static int LEVEL_4_ID;

    @Inject
    private UserTransaction utx;
    @PersistenceContext
    private EntityManager em;

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

    protected void setupItemTypes() throws Exception {
        BaseItemType builder = new BaseItemType();
        builder.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setName("Builder");
        builder.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        builder.setBuilderType(new BuilderType().setProgress(1).setRange(3)/*.setAbleToBuildIds(Collections.singletonList(FACTORY_ITEM_TYPE.getId()))*/);
        BASE_ITEM_TYPE_BULLDOZER_ID = createBaseItemTypeEntity(builder);

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(10).setSpawnDurationMillis(1000).setBuildup(10).setName("Harvester");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        BASE_ITEM_TYPE_HARVESTER_ID = createBaseItemTypeEntity(harvester);

        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setName("Attacker");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        attacker.setWeaponType(new WeaponType().setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1).setTurretType(new TurretType().setTorrentCenter(new Vertex(1, 0, 0)).setMuzzlePosition(new Vertex(1, 0, 1)).setAngleVelocity(Math.toRadians(120))));
        BASE_ITEM_TYPE_ATTACKER_ID = createBaseItemTypeEntity(attacker);

        BaseItemType factory = new BaseItemType();
        factory.setHealth(100).setSpawnDurationMillis(1000).setBuildup(3).setName("Factory");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(5));
        BASE_ITEM_TYPE_FACTORY_ID = createBaseItemTypeEntity(factory);

        BaseItemType tower = new BaseItemType();
        tower.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setName("Tower");
        tower.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(3));
        tower.setWeaponType(new WeaponType().setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1).setTurretType(new TurretType().setTorrentCenter(new Vertex(2, 0, 0)).setMuzzlePosition(new Vertex(2, 0, 1)).setAngleVelocity(Math.toRadians(60))));
        BASE_ITEM_TYPE_TOWER_ID = createBaseItemTypeEntity(tower);

        ResourceItemType resource = new ResourceItemType();
        resource.setRadius(2).setAmount(1000);
        RESOURCE_ITEM_TYPE_ID = createResourceItemTypeEntity(resource);

    }

    protected void cleanItemTypes() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createQuery("DELETE FROM BaseItemTypeEntity").executeUpdate();
        em.createQuery("DELETE FROM ResourceItemTypeEntity").executeUpdate();
        utx.commit();
    }

    private int createBaseItemTypeEntity(BaseItemType baseItemType) throws Exception {
        utx.begin();
        em.joinTransaction();
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        baseItemTypeEntity.fromBaseItemType(baseItemType);
        em.persist(baseItemTypeEntity);
        utx.commit();
        return baseItemTypeEntity.getId();
    }

    private int createResourceItemTypeEntity(ResourceItemType resourceItemType) throws Exception {
        utx.begin();
        em.joinTransaction();
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        em.persist(resourceItemTypeEntity);
        utx.commit();
        return resourceItemTypeEntity.getId();
    }

    protected void setupLevels() throws Exception {
        setupItemTypes();
        utx.begin();
        em.joinTransaction();

        // Level 1
        LevelEntity levelEntity1 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation1 = new HashMap<>();
        itemTypeLimitation1.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        levelEntity1.fromLevelConfig(new LevelConfig().setNumber(1).setXp2LevelUp(10), itemTypeLimitation1);
        em.persist(levelEntity1);
        LEVEL_1_ID = levelEntity1.getId();
        // Level 2
        LevelEntity levelEntity2 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation2 = new HashMap<>();
        itemTypeLimitation2.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation2.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        levelEntity2.fromLevelConfig(new LevelConfig().setNumber(2).setXp2LevelUp(20), itemTypeLimitation2);
        em.persist(levelEntity2);
        LEVEL_2_ID = levelEntity2.getId();
        // Level 3
        LevelEntity levelEntity3 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation3 = new HashMap<>();
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        levelEntity3.fromLevelConfig(new LevelConfig().setNumber(3).setXp2LevelUp(30), itemTypeLimitation3);
        em.persist(levelEntity3);
        LEVEL_3_ID = levelEntity3.getId();
        // Level 4
        LevelEntity levelEntity4 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation4 = new HashMap<>();
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
        levelEntity4.fromLevelConfig(new LevelConfig().setNumber(4).setXp2LevelUp(40), itemTypeLimitation4);
        em.persist(levelEntity4);
        LEVEL_4_ID = levelEntity4.getId();

        utx.commit();
    }

    protected void cleanLevels() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createNativeQuery("DELETE FROM LEVEL_LIMITATION").executeUpdate();
        em.createQuery("DELETE FROM LevelEntity").executeUpdate();
        utx.commit();
        cleanItemTypes();
    }
}