package com.btxtech.server;

import com.btxtech.server.persistence.GameUiControlConfigEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

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
    public static int BOX_ITEM_TYPE_ID;
    // Levels
    public static int LEVEL_1_ID;
    public static int LEVEL_2_ID;
    public static int LEVEL_3_ID;
    public static int LEVEL_4_ID;
    public static int LEVEL_5_ID;
    // Planet
    public static int PLANET_1_ID;
    public static int PLANET_2_ID;
    // GameUiControlConfigEntity
    public static int GAME_UI_CONTROL_CONFIG_1_ID;
    public static int GAME_UI_CONTROL_CONFIG_2_ID;
    // Inventory
    public static int INVENTORY_ITEM_1_ID;
    // ServerGameEngineConfigEntity
    public static int SERVER_GAME_ENGINE_CONFIG_ID_1;
    // Quests
    public static int SERVER_QUEST_ID_1;
    public static int SERVER_QUEST_ID_2;
    @PersistenceContext
    private EntityManager em;
    @Inject
    private UserTransaction utx;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Deployment
    public static Archive<?> createDeployment() {
        try {
            File[] libraries = Maven.resolver().loadPomFromFile("./pom.xml").importRuntimeDependencies().resolve("org.unitils:unitils-core:4.0-SNAPSHOT").withTransitivity().asFile();

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

    protected EntityManager getEntityManager() {
        return em;
    }

    protected I18nString i18nHelper(String string) {
        Map<String, String> localizedStrings = new HashMap<>();
        localizedStrings.put(I18nString.DE, string);
        return new I18nString(localizedStrings);
    }

    protected void setupItemTypes() throws Exception {
        BaseItemType builder = new BaseItemType();
        builder.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setInternalName("Builder");
        builder.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        builder.setBuilderType(new BuilderType().setProgress(1).setRange(3)/*.setAbleToBuildIds(Collections.singletonList(FACTORY_ITEM_TYPE.getId()))*/);
        BASE_ITEM_TYPE_BULLDOZER_ID = createBaseItemTypeEntity(builder);

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(10).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Harvester");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        BASE_ITEM_TYPE_HARVESTER_ID = createBaseItemTypeEntity(harvester);

        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setInternalName("Attacker");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        attacker.setWeaponType(new WeaponType().setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1).setTurretType(new TurretType().setTorrentCenter(new Vertex(1, 0, 0)).setMuzzlePosition(new Vertex(1, 0, 1)).setAngleVelocity(Math.toRadians(120))));
        BASE_ITEM_TYPE_ATTACKER_ID = createBaseItemTypeEntity(attacker);

        BaseItemType factory = new BaseItemType();
        factory.setHealth(100).setSpawnDurationMillis(1000).setBuildup(3).setInternalName("Factory");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(5));
        BASE_ITEM_TYPE_FACTORY_ID = createBaseItemTypeEntity(factory);

        BaseItemType tower = new BaseItemType();
        tower.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Tower");
        tower.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(3));
        tower.setWeaponType(new WeaponType().setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1).setTurretType(new TurretType().setTorrentCenter(new Vertex(2, 0, 0)).setMuzzlePosition(new Vertex(2, 0, 1)).setAngleVelocity(Math.toRadians(60))));
        BASE_ITEM_TYPE_TOWER_ID = createBaseItemTypeEntity(tower);

        InventoryItem inventoryItem = new InventoryItem();
        INVENTORY_ITEM_1_ID = createInventoryItemEntity(inventoryItem);

        ResourceItemType resource = new ResourceItemType();
        resource.setRadius(2).setAmount(1000);
        RESOURCE_ITEM_TYPE_ID = createResourceItemTypeEntity(resource);

        BoxItemType boxItemType = new BoxItemType();
        boxItemType.setRadius(2);
        BOX_ITEM_TYPE_ID = createBoxItemTypeEntity(boxItemType);
    }

    protected void cleanItemTypes() throws Exception {
        runInTransaction(em -> {
            em.createNativeQuery("DELETE FROM BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD").executeUpdate();
            em.createNativeQuery("DELETE FROM BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD").executeUpdate();
            em.createNativeQuery("DELETE FROM BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES").executeUpdate();
            em.createQuery("DELETE FROM DemolitionStepEffectParticleEntity").executeUpdate();
            em.createQuery("DELETE FROM DemolitionStepEffectEntity").executeUpdate();
            em.createQuery("DELETE FROM BaseItemTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM WeaponTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM FactoryTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM HarvesterTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM BuilderTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM ConsumerTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM ItemContainerTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM HouseTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM BaseItemTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM ResourceItemTypeEntity").executeUpdate();
            em.createQuery("DELETE FROM BoxItemTypeEntity ").executeUpdate();
            em.createQuery("DELETE FROM InventoryItemEntity").executeUpdate();
            em.createQuery("DELETE FROM TurretTypeEntity").executeUpdate();
        });
    }

    private int createBaseItemTypeEntity(BaseItemType baseItemType) throws Exception {
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        baseItemTypeEntity.fromBaseItemType(baseItemType, itemTypePersistence, shape3DPersistence);
        persistInTransaction(baseItemTypeEntity);
        return baseItemTypeEntity.getId();
    }

    private int createResourceItemTypeEntity(ResourceItemType resourceItemType) throws Exception {
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        persistInTransaction(resourceItemTypeEntity);
        return resourceItemTypeEntity.getId();
    }

    private int createInventoryItemEntity(InventoryItem inventoryItem) throws Exception {
        InventoryItemEntity inventoryItemEntity = new InventoryItemEntity();
        inventoryItemEntity.fromInventoryItem(inventoryItem);
        persistInTransaction(inventoryItemEntity);
        return inventoryItemEntity.getId();
    }

    private int createBoxItemTypeEntity(BoxItemType boxItemType) throws Exception {
        BoxItemTypeEntity boxItemTypeEntity = new BoxItemTypeEntity();
        boxItemTypeEntity.fromBoxItemType(boxItemType, null);
        persistInTransaction(boxItemTypeEntity);
        return boxItemTypeEntity.getId();
    }

    protected <T> T persistInTransaction(T object) throws Exception {
        utx.begin();
        em.joinTransaction();
        em.persist(object);
        utx.commit();
        return object;
    }

    protected void runInTransaction(Consumer<EntityManager> consumer) throws Exception {
        utx.begin();
        em.joinTransaction();
        consumer.accept(em);
        utx.commit();
    }

    protected void runInTransactionSave(Consumer<EntityManager> consumer) throws Exception {
        try {
            utx.begin();
            em.joinTransaction();
            consumer.accept(em);
            utx.commit();
        } catch (Throwable t) {
            t.printStackTrace();
            utx.rollback();
        }
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
        // Level 5
        LevelEntity levelEntity5 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation5 = new HashMap<>();
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 4);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
        levelEntity5.fromLevelConfig(new LevelConfig().setNumber(5).setXp2LevelUp(50), itemTypeLimitation5);
        em.persist(levelEntity5);
        LEVEL_5_ID = levelEntity5.getId();

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

    protected void setupPlanets() throws Exception {
        setupLevels();

        utx.begin();
        em.joinTransaction();

        GroundConfigEntity groundConfigEntity = new GroundConfigEntity();
        groundConfigEntity.fromGroundConfig(setupGroundConfig(), imagePersistence);
        em.persist(groundConfigEntity);

        em.persist(new WaterConfigEntity());

        PlanetEntity planetEntity1 = new PlanetEntity();
        em.persist(planetEntity1);
        PLANET_1_ID = planetEntity1.getId();

        PlanetEntity planetEntity2 = new PlanetEntity();
        em.persist(planetEntity2);
        PLANET_2_ID = planetEntity2.getId();

        GameUiControlConfigEntity gameUiControlConfigEntity1 = new GameUiControlConfigEntity();
        gameUiControlConfigEntity1.setPlanetEntity(planetEntity1);
        gameUiControlConfigEntity1.setMinimalLevel(em.find(LevelEntity.class, LEVEL_1_ID));
        gameUiControlConfigEntity1.setGameEngineMode(GameEngineMode.MASTER);
        em.persist(gameUiControlConfigEntity1);
        GAME_UI_CONTROL_CONFIG_1_ID = gameUiControlConfigEntity1.getId();

        GameUiControlConfigEntity gameUiControlConfigEntity2 = new GameUiControlConfigEntity();
        gameUiControlConfigEntity2.setPlanetEntity(planetEntity2);
        gameUiControlConfigEntity2.setMinimalLevel(em.find(LevelEntity.class, LEVEL_4_ID));
        gameUiControlConfigEntity2.setGameEngineMode(GameEngineMode.SLAVE);
        em.persist(gameUiControlConfigEntity2);
        GAME_UI_CONTROL_CONFIG_2_ID = gameUiControlConfigEntity2.getId();

        ServerGameEngineConfigEntity serverGameEngineConfigEntity1 = new ServerGameEngineConfigEntity();
        serverGameEngineConfigEntity1.setPlanetEntity(planetEntity2);
        ServerLevelQuestEntity serverLevelQuestEntity1 = new ServerLevelQuestEntity();
        serverLevelQuestEntity1.setMinimalLevel(em.find(LevelEntity.class, LEVEL_4_ID));
        QuestConfigEntity questConfigEntity1 = new QuestConfigEntity();
        questConfigEntity1.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest 1").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(1))), Locale.US);
        QuestConfigEntity questConfigEntity2 = new QuestConfigEntity();
        questConfigEntity2.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest 2").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(2))), Locale.US);
        serverLevelQuestEntity1.setQuestConfigs(Arrays.asList(questConfigEntity1, questConfigEntity2));
        serverGameEngineConfigEntity1.setServerQuestEntities(Collections.singletonList(serverLevelQuestEntity1));
        em.persist(serverGameEngineConfigEntity1);
        SERVER_GAME_ENGINE_CONFIG_ID_1 = serverGameEngineConfigEntity1.getId();
        SERVER_QUEST_ID_1 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(0).getId();
        SERVER_QUEST_ID_2 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(1).getId();

        utx.commit();
    }

    private GroundConfig setupGroundConfig() {
        GroundConfig groundConfig = new GroundConfig();
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setLightConfig(new LightConfig().setDiffuse(Color.fromHtmlColor("#000000")).setAmbient(Color.fromHtmlColor("#000000")));
        groundConfig.setGroundSkeletonConfig(groundSkeletonConfig);
        return groundConfig;
    }

    protected void cleanPlanets() throws Exception {
        cleanTable(ServerLevelQuestEntity.class);
        cleanTableNative("SERVER_QUEST");
        cleanTable(QuestConfigEntity.class);
        cleanTable(ConditionConfigEntity.class);
        cleanTable(ComparisonConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BASE_ITEM");

        cleanTable(WaterConfigEntity.class);
        cleanTable(GroundConfigEntity.class);
        cleanTable(GameUiControlConfigEntity.class);
        cleanTable(ServerGameEngineConfigEntity.class);
        cleanTable(PlanetEntity.class);
        cleanLevels();
    }

    protected void assertCount(int countExpected, Class entityClass) {
        Assert.assertEquals(countExpected, ((Number) getEntityManager().createQuery("SELECT COUNT(e) FROM " + entityClass.getName() + " e").getSingleResult()).intValue());
    }

    protected void assertEmptyCount(Class entityClass) {
        assertCount(0, entityClass);
    }

    protected void assertCountNative(int countExpected, String tableName) {
        Assert.assertEquals(countExpected, ((Number) em.createNativeQuery("SELECT COUNT(*) FROM " + tableName).getSingleResult()).intValue());
    }

    protected void assertEmptyCountNative(String tableName) {
        assertCountNative(0, tableName);
    }

    protected void cleanTable(Class entityClass) throws Exception {
        runInTransaction(em -> em.createQuery("DELETE FROM " + entityClass.getName()).executeUpdate());
    }

    protected void cleanTableNative(String tableName) throws Exception {
        runInTransaction(em -> em.createNativeQuery("DELETE FROM " + tableName).executeUpdate());
    }

    protected void printSqlStatement(String sql) {
        Query q = em.createNativeQuery(sql);
        List<Object[]> resultList = q.getResultList();

        System.out.println("SQL-----------------------------------------------------");
        System.out.println(sql);
        for (Object[] row : resultList) {
            for (Object cell : row) {
                System.out.print(cell);
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println("SQL-ENDS-----------------------------------------------------");
    }
}