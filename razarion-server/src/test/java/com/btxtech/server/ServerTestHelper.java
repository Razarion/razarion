package com.btxtech.server;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.persistence.GameUiControlConfigEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.history.UserHistoryEntity;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.GroundHeightEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeNodeEntity;
import com.btxtech.server.persistence.surface.SlopeShapeEntity;
import com.btxtech.server.persistence.surface.TerrainSlopeCornerEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.Assert;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Created by Beat
 * on 08.02.2018.
 */
public class ServerTestHelper {
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
    public static int SERVER_QUEST_ID_L4_1;
    public static int SERVER_QUEST_ID_L4_2;
    public static int SERVER_QUEST_ID_L5_1;
    public static int SERVER_QUEST_ID_L5_2;
    public static int SERVER_QUEST_ID_L5_3;
    // Unlock
    public static int LEVEL_UNLOCK_ID_L4_1;
    public static int LEVEL_UNLOCK_ID_L5_1;
    public static int LEVEL_UNLOCK_ID_L5_2;
    // SlopeConfigEntity
    public static int SLOPE_LAND_CONFIG_ENTITY_1;
    public static int SLOPE_WATER_CONFIG_ENTITY_2;
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
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;


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
        builder.setPhysicalAreaConfig(new PhysicalAreaConfig().setTerrainType(TerrainType.LAND).setAcceleration(2.78).setSpeed(17.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        builder.setBuilderType(new BuilderType().setProgress(1).setRange(3)/*.setAbleToBuildIds(Collections.singletonList(FACTORY_ITEM_TYPE.getId()))*/);
        BASE_ITEM_TYPE_BULLDOZER_ID = createBaseItemTypeEntity(builder);

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(10).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Harvester");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setTerrainType(TerrainType.LAND).setAcceleration(40.0).setSpeed(80.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        BASE_ITEM_TYPE_HARVESTER_ID = createBaseItemTypeEntity(harvester);

        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setInternalName("Attacker");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setTerrainType(TerrainType.LAND).setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        attacker.setWeaponType(new WeaponType().setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1).setTurretType(new TurretType().setTorrentCenter(new Vertex(1, 0, 0)).setMuzzlePosition(new Vertex(1, 0, 1)).setAngleVelocity(Math.toRadians(120))));
        BASE_ITEM_TYPE_ATTACKER_ID = createBaseItemTypeEntity(attacker);

        BaseItemType factory = new BaseItemType();
        factory.setHealth(100).setSpawnDurationMillis(1000).setBuildup(3).setInternalName("Factory");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(5));
        BASE_ITEM_TYPE_FACTORY_ID = createBaseItemTypeEntity(factory);

        BaseItemType tower = new BaseItemType();
        tower.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Tower");
        tower.setPhysicalAreaConfig(new PhysicalAreaConfig().setTerrainType(TerrainType.LAND).setRadius(3));
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
        levelEntity1.fromLevelEditConfig((LevelEditConfig) new LevelEditConfig().setNumber(1).setXp2LevelUp(10), itemTypeLimitation1, null);
        em.persist(levelEntity1);
        LEVEL_1_ID = levelEntity1.getId();
        // Level 2
        LevelEntity levelEntity2 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation2 = new HashMap<>();
        itemTypeLimitation2.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation2.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        levelEntity2.fromLevelEditConfig((LevelEditConfig) new LevelEditConfig().setNumber(2).setXp2LevelUp(20), itemTypeLimitation2, null);
        em.persist(levelEntity2);
        LEVEL_2_ID = levelEntity2.getId();
        // Level 3
        LevelEntity levelEntity3 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation3 = new HashMap<>();
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        itemTypeLimitation3.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        levelEntity3.fromLevelEditConfig((LevelEditConfig) new LevelEditConfig().setNumber(3).setXp2LevelUp(30), itemTypeLimitation3, null);
        em.persist(levelEntity3);
        LEVEL_3_ID = levelEntity3.getId();
        // Level 4
        LevelEntity levelEntity4 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation4 = new HashMap<>();
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        itemTypeLimitation4.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
        LevelUnlockEntity levelUnlockEntity4_1 = new LevelUnlockEntity();
        levelUnlockEntity4_1.setBaseItemType(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID));
        levelUnlockEntity4_1.setBaseItemTypeCount(1);
        levelUnlockEntity4_1.setInternalName("levelUnlockEntity4_1");
        levelEntity4.fromLevelEditConfig((LevelEditConfig) new LevelEditConfig().setNumber(4).setXp2LevelUp(300), itemTypeLimitation4, Collections.singletonList(levelUnlockEntity4_1));
        em.persist(levelEntity4);
        LEVEL_4_ID = levelEntity4.getId();
        LEVEL_UNLOCK_ID_L4_1 = levelUnlockEntity4_1.getId();
        // Level 5
        LevelEntity levelEntity5 = new LevelEntity();
        Map<BaseItemTypeEntity, Integer> itemTypeLimitation5 = new HashMap<>();
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 4);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
        itemTypeLimitation5.put(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
        LevelUnlockEntity levelUnlockEntity5_1 = new LevelUnlockEntity();
        levelUnlockEntity5_1.setBaseItemType(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID));
        levelUnlockEntity5_1.setBaseItemTypeCount(2);
        levelUnlockEntity5_1.setCrystalCost(10);
        levelUnlockEntity5_1.setInternalName("levelUnlockEntity5_1");
        LevelUnlockEntity levelUnlockEntity5_2 = new LevelUnlockEntity();
        levelUnlockEntity5_2.setBaseItemType(em.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID));
        levelUnlockEntity5_2.setBaseItemTypeCount(1);
        levelUnlockEntity5_2.setCrystalCost(20);
        levelUnlockEntity5_2.setInternalName("levelUnlockEntity5_2");
        levelEntity5.fromLevelEditConfig((LevelEditConfig) new LevelEditConfig().setNumber(5).setXp2LevelUp(400), itemTypeLimitation5, Arrays.asList(levelUnlockEntity5_1, levelUnlockEntity5_2));
        em.persist(levelEntity5);
        LEVEL_5_ID = levelEntity5.getId();
        LEVEL_UNLOCK_ID_L5_1 = levelUnlockEntity5_1.getId();
        LEVEL_UNLOCK_ID_L5_2 = levelUnlockEntity5_2.getId();

        utx.commit();
    }

    protected void cleanLevels() throws Exception {
        cleanTable(LevelUnlockEntity.class);
        cleanTableNative("LEVEL_LIMITATION");
        cleanTable(LevelEntity.class);
        cleanItemTypes();
    }

    public void setupPlanets() throws Exception {
        setupLevels();

        utx.begin();
        em.joinTransaction();

        GroundConfigEntity groundConfigEntity = new GroundConfigEntity();
        groundConfigEntity.fromGroundConfig(setupGroundConfig(), imagePersistence);
        em.persist(groundConfigEntity);

        em.persist(new WaterConfigEntity());

        PlanetEntity planetEntity1 = new PlanetEntity();
        planetEntity1.setGroundMeshDimension(new Rectangle(0, 0, 2, 2));
        planetEntity1.setPlayGround(new Rectangle2D(50, 50, 200, 200));
        planetEntity1.setStartBaseItemType(itemTypePersistence.readBaseItemTypeEntity(BASE_ITEM_TYPE_BULLDOZER_ID));
        em.persist(planetEntity1);
        PLANET_1_ID = planetEntity1.getId();

        PlanetEntity planetEntity2 = new PlanetEntity();
        planetEntity2.setGroundMeshDimension(new Rectangle(0, 0, 10, 10));
        planetEntity2.setPlayGround(new Rectangle2D(50, 50, 1500, 1500));
        planetEntity2.setStartBaseItemType(itemTypePersistence.readBaseItemTypeEntity(BASE_ITEM_TYPE_BULLDOZER_ID));
        planetEntity2.setItemTypeLimitation(setupPlanet2Limitation());
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

        ServerLevelQuestEntity serverLevelQuestEntityL4 = new ServerLevelQuestEntity();
        serverLevelQuestEntityL4.setMinimalLevel(em.find(LevelEntity.class, LEVEL_4_ID));
        QuestConfigEntity questConfigEntityL41 = new QuestConfigEntity();
        questConfigEntityL41.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L4 1").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1))), Locale.US);
        QuestConfigEntity questConfigEntityL42 = new QuestConfigEntity();
        questConfigEntityL42.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L4 2").setXp(200).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(2))), Locale.US);
        serverLevelQuestEntityL4.setQuestConfigs(Arrays.asList(questConfigEntityL41, questConfigEntityL42));

        ServerLevelQuestEntity serverLevelQuestEntityL5 = new ServerLevelQuestEntity();
        serverLevelQuestEntityL5.setMinimalLevel(em.find(LevelEntity.class, LEVEL_5_ID));
        QuestConfigEntity questConfigEntityL51 = new QuestConfigEntity();
        questConfigEntityL51.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 1").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1))), Locale.US);
        QuestConfigEntity questConfigEntityL52 = new QuestConfigEntity();
        questConfigEntityL52.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 2").setXp(200).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BASE_KILLED).setComparisonConfig(new ComparisonConfig().setCount(2))), Locale.US);
        QuestConfigEntity questConfigEntityL53 = new QuestConfigEntity();
        questConfigEntityL53.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 3").setXp(50).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100))), Locale.US);
        serverLevelQuestEntityL5.setQuestConfigs(Arrays.asList(questConfigEntityL51, questConfigEntityL52, questConfigEntityL53));

        serverGameEngineConfigEntity1.setServerQuestEntities(Arrays.asList(serverLevelQuestEntityL4, serverLevelQuestEntityL5));
        em.persist(serverGameEngineConfigEntity1);
        SERVER_GAME_ENGINE_CONFIG_ID_1 = serverGameEngineConfigEntity1.getId();
        SERVER_QUEST_ID_L4_1 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(0).getId();
        SERVER_QUEST_ID_L4_2 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(1).getId();
        SERVER_QUEST_ID_L5_1 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(0).getId();
        SERVER_QUEST_ID_L5_2 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(1).getId();
        SERVER_QUEST_ID_L5_3 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(2).getId();
        utx.commit();
    }

    protected void setupPlanetWithSlopes() throws Exception {
        setupSlopeConfigEntities();
        setupPlanets();

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();

        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setSlopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new TerrainSlopeCorner().setPosition(new DecimalPosition(50, 40)), new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 40)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 60)), new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 90)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 110)), new TerrainSlopeCorner().setPosition(new DecimalPosition(50, 110))));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.setSlopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePositionWater.setPolygon(Arrays.asList(new TerrainSlopeCorner().setPosition(new DecimalPosition(64, 200)), new TerrainSlopeCorner().setPosition(new DecimalPosition(231, 200)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(231, 256)), new TerrainSlopeCorner().setPosition(new DecimalPosition(151, 257)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(239, 359)), new TerrainSlopeCorner().setPosition(new DecimalPosition(49, 360))));
        terrainSlopePositions.add(terrainSlopePositionWater);

        planetPersistence.createTerrainSlopePositions(PLANET_2_ID, terrainSlopePositions);

        // Start from ServletContextMonitor.contextInitialized() not working
        serverGameEngineControl.start(null, true);
    }

    protected void setupSlopeConfigEntities() throws Exception {
        runInTransaction(em -> {
            SlopeConfigEntity slopeConfigEntity1 = new SlopeConfigEntity();
            SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
            slopeSkeletonConfigLand.setLightConfig(new LightConfig());
            slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
            slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
            slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                    {new SlopeNode().setPosition(new Vertex(2, 0, 5)).setSlopeFactor(1)},
                    {new SlopeNode().setPosition(new Vertex(4, 0, 10)).setSlopeFactor(0.7)},
                    {new SlopeNode().setPosition(new Vertex(7, 0, 20)).setSlopeFactor(0.7)},
            }));
            slopeSkeletonConfigLand.setOuterLineGameEngine(1).setInnerLineGameEngine(6);
            slopeConfigEntity1.setDefault();
            List<SlopeShape> shapeLand = Arrays.asList(new SlopeShape(new DecimalPosition(2, 5), 1), new SlopeShape(new DecimalPosition(4, 10), 1), new SlopeShape(new DecimalPosition(7, 20), 1));
            slopeConfigEntity1.fromSlopeConfig(new SlopeConfig().setSlopeSkeletonConfig(slopeSkeletonConfigLand).setInternalName("Land").setShape(shapeLand), imagePersistence);
            em.persist(slopeConfigEntity1);


            SLOPE_LAND_CONFIG_ENTITY_1 = slopeConfigEntity1.getId();
            SlopeConfigEntity slopeConfigEntity2 = new SlopeConfigEntity();
            SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
            slopeSkeletonConfigWater.setLightConfig(new LightConfig());
            slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
            slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(20).setVerticalSpace(6).setHeight(-2);
            slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                    {new SlopeNode().setPosition(new Vertex(5, 0, 0.5)).setSlopeFactor(0.5)},
                    {new SlopeNode().setPosition(new Vertex(10, 0, -0.1)).setSlopeFactor(1)},
                    {new SlopeNode().setPosition(new Vertex(15, 0, -0.8)).setSlopeFactor(1)},
                    {new SlopeNode().setPosition(new Vertex(20, 0, -2)).setSlopeFactor(1)},
            }));
            slopeSkeletonConfigWater.setOuterLineGameEngine(8).setCoastDelimiterLineGameEngine(10).setInnerLineGameEngine(16);
            slopeConfigEntity2.setDefault();
            List<SlopeShape> shapeWater = Arrays.asList(new SlopeShape(new DecimalPosition(5, 0.5), 0.5f), new SlopeShape(new DecimalPosition(10, -0.1), 1), new SlopeShape(new DecimalPosition(15, -0.8), 1), new SlopeShape(new DecimalPosition(20, -2), 1));
            slopeConfigEntity2.fromSlopeConfig(new SlopeConfig().setSlopeSkeletonConfig(slopeSkeletonConfigWater).setInternalName("Water").setShape(shapeWater), imagePersistence);
            em.persist(slopeConfigEntity2);
            SLOPE_WATER_CONFIG_ENTITY_2 = slopeConfigEntity2.getId();
        });
    }

    protected SlopeNode[][] toColumnRow(SlopeNode[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        SlopeNode[][] columnRow = new SlopeNode[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

    private Map<BaseItemTypeEntity, Integer> setupPlanet2Limitation() {
        Map<BaseItemTypeEntity, Integer> limitation = new HashMap<>();
        limitation.put(itemTypePersistence.readBaseItemTypeEntity(BASE_ITEM_TYPE_BULLDOZER_ID), 1);
        return limitation;
    }

    private GroundConfig setupGroundConfig() {
        GroundConfig groundConfig = new GroundConfig();
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setLightConfig(new LightConfig().setDiffuse(Color.fromHtmlColor("#000000")).setAmbient(Color.fromHtmlColor("#000000")));
        groundSkeletonConfig.setHeightXCount(1);
        groundSkeletonConfig.setHeightYCount(1);
        groundSkeletonConfig.setHeights(new double[][]{{0}});
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
        cleanTable(GroundHeightEntity.class);
        cleanTable(GroundConfigEntity.class);
        cleanTable(GameUiControlConfigEntity.class);
        cleanTable(ServerGameEngineConfigEntity.class);
        cleanTableNative("PLANET_LIMITATION");
        cleanTable(PlanetEntity.class);
        cleanLevels();
    }

    protected void cleanPlanetWithSlopes() throws Exception {
        cleanSlopeEntities();

        cleanPlanets();
    }

    protected void cleanSlopeEntities() throws Exception {
        cleanTable(TerrainSlopeCornerEntity.class);
        cleanTable(TerrainSlopePositionEntity.class);

        cleanTable(SlopeNodeEntity.class);
        cleanTable(SlopeShapeEntity.class);
        cleanTable(SlopeConfigEntity.class);
    }

    protected void cleanUsers() throws Exception {
        cleanTable(UserHistoryEntity.class);
        cleanTableNative("USER_COMPLETED_QUEST");
        cleanTable(UserEntity.class);
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

    protected void printMessage(String message) {
        System.out.println("MESSAGE-----------------------------------------------------");
        System.out.println(message);
        System.out.println("MESSAGE-ENDS-----------------------------------------------------");
    }

    protected void clearMongoDb() {
        new MongoClient().getDatabase("razarion").drop();
    }

    protected <T> MongoCollection<T> getMongoCollection(String collectionName, Class<T> theClass) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClient mongoClient = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        return mongoClient.getDatabase("razarion").getCollection(collectionName, theClass);
    }

    protected <T> void fillBackupInfoMongoDb(String collectionName, String fileName, Class<T> theClass) {
        try {
            InputStream input = getClass().getResourceAsStream(fileName);
            if (input == null) {
                throw new IllegalArgumentException("Can not find: " + fileName);
            }
            String jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                jsonString = br.lines().collect(Collectors.joining(System.lineSeparator()));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(new SimpleDateFormat(DateUtil.JSON_FORMAT_STRING));
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<T> entries = objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, theClass));
            MongoCollection<T> mongoCollection = getMongoCollection(collectionName, theClass);
            for (T entry : entries) {
                mongoCollection.insertOne(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> List<T> readMongoDb(String collectionName, Class<T> theClass) {
        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, theClass);
        FindIterable<T> findIterable = mongoCollection.find();
        List<T> result = new ArrayList<>();
        findIterable.forEach((Consumer<T>) result::add);
        return result;
    }

    protected void tickPlanetServiceBaseServiceActive(SyncBaseItem... ignores) {
        while (isBaseServiceActive(ignores)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected boolean isBaseServiceActive(SyncBaseItem[] ignores) {
        Collection<SyncBaseItem> activeItems = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService));
        activeItems.removeAll(Arrays.asList(ignores));
        Collection<SyncBaseItem> activeItemQueue = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService));
        activeItemQueue.removeAll(Arrays.asList(ignores));
        return !activeItems.isEmpty() || !activeItemQueue.isEmpty();
    }

    protected UserContext handleFacebookUserLogin(String facebookUserId) {
        return userService.handleFacebookUserLogin(new FbAuthResponse().setUserID(facebookUserId));
    }

    protected UserContext handleUnregisteredLogin() {
        return userService.getUserContextFromSession();
    }

}