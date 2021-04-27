package com.btxtech.server;

import com.btxtech.server.persistence.AudioLibraryEntity;
import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.GameUiContextEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.server.persistence.TerrainObjectCrudPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BuilderTypeEntity;
import com.btxtech.server.persistence.itemtype.ConsumerTypeEntity;
import com.btxtech.server.persistence.itemtype.DemolitionStepEffectEntity;
import com.btxtech.server.persistence.itemtype.DemolitionStepEffectParticleEntity;
import com.btxtech.server.persistence.itemtype.FactoryTypeEntity;
import com.btxtech.server.persistence.itemtype.HarvesterTypeEntity;
import com.btxtech.server.persistence.itemtype.HouseTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemContainerTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.server.persistence.itemtype.TurretTypeEntity;
import com.btxtech.server.persistence.itemtype.WeaponTypeEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceEntity;
import com.btxtech.server.persistence.particle.ParticleShapeEntity;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeShapeEntity;
import com.btxtech.server.persistence.surface.TerrainSlopeCornerEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.server.systemtests.framework.CleanupAfterTest;
import com.btxtech.server.user.ForgotPasswordEntity;
import com.btxtech.server.user.LoginCookieEntity;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.dto.RegisterResult;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
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
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
    // Images
    public static final String IMG_1_DATA_BASE64 = "R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
    public static final String IMG_1_DATA_URL = "data:image/jpeg;base64," + IMG_1_DATA_BASE64;
    public static final byte[] IMG_1_BYTES = Base64.getDecoder().decode(IMG_1_DATA_BASE64.getBytes());
    public static final String IMG_2_DATA_BASE64 = "R0lGODlhAQABAIABABWLAP///yH+EUNyZWF0ZWQgd2l0aCBHSU1QACwAAAAAAQABAAACAkQBADs=";
    public static final String IMG_2_DATA_URL = "data:image/gif;base64," + IMG_2_DATA_BASE64;
    public static final byte[] IMG_2_BYTES = Base64.getDecoder().decode(IMG_2_DATA_BASE64.getBytes());
    // Default users
    public static final String ADMIN_USER_EMAIL = "admin@admin.com";
    public static final String ADMIN_USER_PASSWORD = "1234";
    public static final String ADMIN_USER_PASSWORD_HASH = "qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==";
    public static int ADMIN_USER_ID;
    public static final String NORMAL_USER_EMAIL = "user@user.com";
    public static final String NORMAL_USER_PASSWORD = "1234";
    public static final String NORMAL_USER_PASSWORD_HASH = "qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==";
    public static int NORMAL_USER_ID;
    // Images
    public static int IMAGE_1_ID;
    public static int IMAGE_2_ID;
    public static int IMAGE_3_ID;
    // Audio
    public static int AUDIO_1_ID;
    public static int AUDIO_2_ID;
    public static int AUDIO_3_ID;
    // Shape3D ColladaEntity
    public static int SHAPE_3D_1_ID;
    public static int SHAPE_3D_2_ID;
    public static int SHAPE_3D_3_ID;
    // Particle Shapes
    public static int PARTICLE_SHAPE_1_ID;
    public static int PARTICLE_SHAPE_2_ID;
    public static int PARTICLE_SHAPE_3_ID;
    // Particle Emitter sequences
    public static int PARTICLE_EMITTER_SEQUENCE_1_ID;
    public static int PARTICLE_EMITTER_SEQUENCE_2_ID;
    public static int PARTICLE_EMITTER_SEQUENCE_3_ID;
    // Ground
    public static int GROUND_1_ID;
    public static int GROUND_2_ID;
    public static int GROUND_3_ID;
    public static int GROUND_4_ID;
    // Water
    public static int WATER_1_ID;
    public static int WATER_2_ID;
    // Terrain Object
    public static int TERRAIN_OBJECT_1_ID;
    public static int TERRAIN_OBJECT_2_ID;
    public static int TERRAIN_OBJECT_3_ID;
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
    // GameUiContextEntity
    public static int GAME_UI_CONTEXT_CONFIG_1_ID;
    public static int GAME_UI_CONTEXT_CONFIG_2_ID;
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
    // SlopeConfigEntity
    public static int DRIVEWAY_CONFIG_ENTITY_1;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private EntityTransaction entityTransaction;
    private List<List<CleanupAfterTest>> cleanupAfterTests = new ArrayList<>();
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;
    private MongoClient mongoClient;

    @Before
    public void setupJpa() {
        entityManagerFactory = Persistence.createEntityManagerFactory("test-jpa");
        entityManager = entityManagerFactory.createEntityManager();
        entityTransaction = entityManager.getTransaction();

        setupDefaultUser();
    }

    @After
    public void closeJpa() {
        cleanUsers();
        Collections.reverse(cleanupAfterTests);
        cleanupAfterTests.forEach(block -> block.forEach(cleanupAfterTest -> {
            if (cleanupAfterTest.getEntityClass() != null) {
                cleanTable(cleanupAfterTest.getEntityClass());
            }
            if (cleanupAfterTest.getTableName() != null) {
                cleanTableNative(cleanupAfterTest.getTableName());
            }
        }));
        entityManager.close();
        entityManagerFactory.close();
    }

    @PreDestroy
    public void preDestroy() {
        closeMongoDb();
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    private void setupDefaultUser() {
        UserEntity adminUser = new UserEntity()
                .admin(true);
        adminUser.setVerifiedDone();
        adminUser.fromEmailPasswordHash(ADMIN_USER_EMAIL, ADMIN_USER_PASSWORD_HASH, Locale.ENGLISH);
        ADMIN_USER_ID = persistInTransaction(adminUser).getId();
        UserEntity normalUser = new UserEntity();
        normalUser.setVerifiedDone();
        normalUser.fromEmailPasswordHash(NORMAL_USER_EMAIL, NORMAL_USER_PASSWORD_HASH, Locale.ENGLISH);
        NORMAL_USER_ID = persistInTransaction(normalUser).getId();
    }

    protected void cleanUsers() {
        cleanTable(ForgotPasswordEntity.class);
        cleanTable(LoginCookieEntity.class);
        cleanTableNative("USER_COMPLETED_QUEST");
        cleanTable(UserEntity.class);
    }

    protected void setupImages() {
        IMAGE_1_ID = persistInTransaction(new ImageLibraryEntity()).getId();
        IMAGE_2_ID = persistInTransaction(new ImageLibraryEntity()).getId();
        IMAGE_3_ID = persistInTransaction(new ImageLibraryEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(ImageLibraryEntity.class)));
    }

    protected void setupAudios() {
        AUDIO_1_ID = persistInTransaction(new AudioLibraryEntity()).getId();
        AUDIO_2_ID = persistInTransaction(new AudioLibraryEntity()).getId();
        AUDIO_3_ID = persistInTransaction(new AudioLibraryEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(AudioLibraryEntity.class)));
    }

    protected void setupGroundConfig() {
        GROUND_1_ID = persistInTransaction(new GroundConfigEntity()).getId();
        GROUND_2_ID = persistInTransaction(new GroundConfigEntity()).getId();
        GROUND_3_ID = persistInTransaction(new GroundConfigEntity()).getId();
        GROUND_4_ID = persistInTransaction(new GroundConfigEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(GroundConfigEntity.class)));
    }

    protected void setupSlopeConfig() {
        SLOPE_LAND_CONFIG_ENTITY_1 = persistInTransaction(createLandSlopeConfig()).getId();
        cleanupAfterTests.add(Arrays.asList(
                new CleanupAfterTest().entity(SlopeShapeEntity.class),
                new CleanupAfterTest().entity(SlopeConfigEntity.class)));
    }

    protected void setupDrivewayConfig() {
        DrivewayConfigEntity drivewayConfigEntity = new DrivewayConfigEntity();
        drivewayConfigEntity.fromDrivewayConfig(new DrivewayConfig().angle(Math.toRadians(20)));
        DRIVEWAY_CONFIG_ENTITY_1 = persistInTransaction(drivewayConfigEntity).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(DrivewayConfigEntity.class)));
    }

    protected SlopeConfigEntity createLandSlopeConfig() {
        SlopeConfigEntity slopeConfigEntity = new SlopeConfigEntity();
        slopeConfigEntity.fromSlopeConfig(new SlopeConfig()
                        .horizontalSpace(5)
                        .outerLineGameEngine(1).innerLineGameEngine(6)
                        .slopeShapes(Arrays.asList(new SlopeShape().slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7)))
                , EasyMock.createNiceMock(ImagePersistence.class), null, null);
        return slopeConfigEntity;
    }

    protected void setupWaterConfig() {
        WATER_1_ID = persistInTransaction(new WaterConfigEntity()).getId();
        WATER_2_ID = persistInTransaction(new WaterConfigEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(WaterConfigEntity.class)));
    }

    protected void setupTerrainObjectConfig() {
        TERRAIN_OBJECT_1_ID = persistInTransaction(createTerrainObjectEntity(new TerrainObjectConfig().radius(1.0).shape3DId(SHAPE_3D_1_ID))).getId();
        TERRAIN_OBJECT_2_ID = persistInTransaction(createTerrainObjectEntity(new TerrainObjectConfig().radius(2.0).shape3DId(SHAPE_3D_2_ID))).getId();
        TERRAIN_OBJECT_3_ID = persistInTransaction(createTerrainObjectEntity(new TerrainObjectConfig().radius(2.5).shape3DId(SHAPE_3D_3_ID))).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(TerrainObjectEntity.class)));
    }

    private TerrainObjectEntity createTerrainObjectEntity(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = new TerrainObjectEntity();
        terrainObjectEntity.fromTerrainObjectConfig(terrainObjectConfig, entityManager.find(ColladaEntity.class, terrainObjectConfig.getShape3DId()));
        return terrainObjectEntity;
    }

    protected void setupShape3dConfig() {
        SHAPE_3D_1_ID = persistInTransaction(new ColladaEntity()).getId();
        SHAPE_3D_2_ID = persistInTransaction(new ColladaEntity()).getId();
        SHAPE_3D_3_ID = persistInTransaction(new ColladaEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(ColladaEntity.class)));
    }

    protected void setupItemTypes() {
        BaseItemType factory = new BaseItemType();
        factory.setPrice(1).setHealth(100).setSpawnDurationMillis(1000).setBuildup(3).setInternalName("Factory");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().radius(5).terrainType(TerrainType.LAND));
        BASE_ITEM_TYPE_FACTORY_ID = createBaseItemTypeEntity(factory);

        BaseItemType builder = new BaseItemType();
        builder.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setInternalName("Builder");
        builder.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(2.78).speed(17.0).angularVelocity(Math.toRadians(30)).radius(2));
        builder.setBuilderType(new BuilderType().progress(1).range(3).ableToBuildIds(Collections.singletonList(BASE_ITEM_TYPE_FACTORY_ID)));
        BASE_ITEM_TYPE_BULLDOZER_ID = createBaseItemTypeEntity(builder);

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(10).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Harvester");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(40.0).speed(80.0).angularVelocity(Math.toRadians(30)).radius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        BASE_ITEM_TYPE_HARVESTER_ID = createBaseItemTypeEntity(harvester);

        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(2).setBuildup(10).setInternalName("Attacker");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(40.0).speed(10.0).angularVelocity(Math.toRadians(30)).radius(2));
        attacker.setWeaponType(new WeaponType().projectileSpeed(17.0).range(20).reloadTime(0.3).damage(1).turretType(new TurretType().setTurretCenter(new Vertex(1, 0, 0)).setMuzzlePosition(new Vertex(1, 0, 1)).setAngleVelocity(Math.toRadians(120))));
        BASE_ITEM_TYPE_ATTACKER_ID = createBaseItemTypeEntity(attacker);

        BaseItemType tower = new BaseItemType();
        tower.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setInternalName("Tower");
        tower.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(3));
        tower.setWeaponType(new WeaponType().projectileSpeed(17.0).range(20).reloadTime(0.3).damage(1).turretType(new TurretType().setTurretCenter(new Vertex(2, 0, 0)).setMuzzlePosition(new Vertex(2, 0, 1)).setAngleVelocity(Math.toRadians(60))));
        BASE_ITEM_TYPE_TOWER_ID = createBaseItemTypeEntity(tower);

        InventoryItem inventoryItem = new InventoryItem();
        INVENTORY_ITEM_1_ID = createInventoryItemEntity(inventoryItem);

        ResourceItemType resource = new ResourceItemType();
        resource.setRadius(2).setAmount(1000);
        RESOURCE_ITEM_TYPE_ID = createResourceItemTypeEntity(resource);

        BoxItemType boxItemType = new BoxItemType();
        boxItemType.setRadius(2);
        BOX_ITEM_TYPE_ID = createBoxItemTypeEntity(boxItemType);

        cleanupAfterTests.add(Arrays.asList(
                new CleanupAfterTest().tableName("BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD"),
                new CleanupAfterTest().tableName("BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD"),
                new CleanupAfterTest().tableName("BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES"),
                new CleanupAfterTest().tableName("BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES"),
                new CleanupAfterTest().entity(DemolitionStepEffectParticleEntity.class),
                new CleanupAfterTest().entity(DemolitionStepEffectEntity.class),
                new CleanupAfterTest().entity(BaseItemTypeEntity.class),
                new CleanupAfterTest().entity(WeaponTypeEntity.class),
                new CleanupAfterTest().entity(FactoryTypeEntity.class),
                new CleanupAfterTest().entity(HarvesterTypeEntity.class),
                new CleanupAfterTest().entity(BuilderTypeEntity.class),
                new CleanupAfterTest().entity(ConsumerTypeEntity.class),
                new CleanupAfterTest().entity(ItemContainerTypeEntity.class),
                new CleanupAfterTest().entity(HouseTypeEntity.class),
                new CleanupAfterTest().entity(BaseItemTypeEntity.class),
                new CleanupAfterTest().entity(ResourceItemTypeEntity.class),
                new CleanupAfterTest().entity(BoxItemTypeEntity.class),
                new CleanupAfterTest().entity(InventoryItemEntity.class),
                new CleanupAfterTest().entity(TurretTypeEntity.class)));
    }

    private int createBaseItemTypeEntity(BaseItemType baseItemType) {
        BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
        ItemTypePersistence itemTypePersistence = EasyMock.createNiceMock(ItemTypePersistence.class);
        BaseItemTypeCrudPersistence baseItemTypeCrudPersistence = EasyMock.createNiceMock(BaseItemTypeCrudPersistence.class);
        Shape3DCrudPersistence shape3DPersistence = EasyMock.createNiceMock(Shape3DCrudPersistence.class);
        ParticleEmitterSequenceCrudPersistence particleEmitterSequenceCrudPersistence = EasyMock.createNiceMock(ParticleEmitterSequenceCrudPersistence.class);
        EasyMock.replay(itemTypePersistence, baseItemTypeCrudPersistence, shape3DPersistence, particleEmitterSequenceCrudPersistence);
        baseItemTypeEntity.fromBaseItemType(baseItemType, itemTypePersistence, baseItemTypeCrudPersistence, shape3DPersistence, particleEmitterSequenceCrudPersistence);
        persistInTransaction(baseItemTypeEntity);
        return baseItemTypeEntity.getId();
    }

    private int createResourceItemTypeEntity(ResourceItemType resourceItemType) {
        ResourceItemTypeEntity resourceItemTypeEntity = new ResourceItemTypeEntity();
        resourceItemTypeEntity.fromResourceItemType(resourceItemType);
        persistInTransaction(resourceItemTypeEntity);
        return resourceItemTypeEntity.getId();
    }

    private int createInventoryItemEntity(InventoryItem inventoryItem) {
        InventoryItemEntity inventoryItemEntity = new InventoryItemEntity();
        inventoryItemEntity.fromInventoryItem(inventoryItem);
        persistInTransaction(inventoryItemEntity);
        return inventoryItemEntity.getId();
    }

    private int createBoxItemTypeEntity(BoxItemType boxItemType) {
        BoxItemTypeEntity boxItemTypeEntity = new BoxItemTypeEntity();
        boxItemTypeEntity.fromBoxItemType(boxItemType, null);
        persistInTransaction(boxItemTypeEntity);
        return boxItemTypeEntity.getId();
    }

    protected void setupLevelDb() {
        setupItemTypes();
        runInTransaction(entityManager -> {
            // Level 1
            LevelEntity levelEntity1 = new LevelEntity();
            Map<BaseItemTypeEntity, Integer> itemTypeLimitation1 = new HashMap<>();
            itemTypeLimitation1.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
            levelEntity1.fromLevelConfig(new LevelConfig().number(1).xp2LevelUp(10), itemTypeLimitation1, null);
            entityManager.persist(levelEntity1);
            LEVEL_1_ID = levelEntity1.getId();
            // Level 2
            LevelEntity levelEntity2 = new LevelEntity();
            Map<BaseItemTypeEntity, Integer> itemTypeLimitation2 = new HashMap<>();
            itemTypeLimitation2.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
            itemTypeLimitation2.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
            levelEntity2.fromLevelConfig(new LevelConfig().number(2).xp2LevelUp(20), itemTypeLimitation2, null);
            entityManager.persist(levelEntity2);
            LEVEL_2_ID = levelEntity2.getId();
            // Level 3
            LevelEntity levelEntity3 = new LevelEntity();
            Map<BaseItemTypeEntity, Integer> itemTypeLimitation3 = new HashMap<>();
            itemTypeLimitation3.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
            itemTypeLimitation3.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
            itemTypeLimitation3.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
            levelEntity3.fromLevelConfig(new LevelConfig().number(3).xp2LevelUp(30), itemTypeLimitation3, null);
            entityManager.persist(levelEntity3);
            LEVEL_3_ID = levelEntity3.getId();
            // Level 4
            LevelEntity levelEntity4 = new LevelEntity();
            Map<BaseItemTypeEntity, Integer> itemTypeLimitation4 = new HashMap<>();
            itemTypeLimitation4.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
            itemTypeLimitation4.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 2);
            itemTypeLimitation4.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
            itemTypeLimitation4.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
            LevelUnlockEntity levelUnlockEntity4_1 = new LevelUnlockEntity();
            levelUnlockEntity4_1.setBaseItemType(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID));
            levelUnlockEntity4_1.setBaseItemTypeCount(1);
            levelUnlockEntity4_1.setInternalName("levelUnlockEntity4_1");
            levelEntity4.fromLevelConfig(new LevelConfig().number(4).xp2LevelUp(300), itemTypeLimitation4, Collections.singletonList(levelUnlockEntity4_1));
            entityManager.persist(levelEntity4);
            LEVEL_4_ID = levelEntity4.getId();
            LEVEL_UNLOCK_ID_L4_1 = levelUnlockEntity4_1.getId();
            // Level 5
            LevelEntity levelEntity5 = new LevelEntity();
            Map<BaseItemTypeEntity, Integer> itemTypeLimitation5 = new HashMap<>();
            itemTypeLimitation5.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_BULLDOZER_ID), 1);
            itemTypeLimitation5.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID), 4);
            itemTypeLimitation5.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_FACTORY_ID), 1);
            itemTypeLimitation5.put(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID), 1);
            LevelUnlockEntity levelUnlockEntity5_1 = new LevelUnlockEntity();
            levelUnlockEntity5_1.setBaseItemType(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_ATTACKER_ID));
            levelUnlockEntity5_1.setBaseItemTypeCount(2);
            levelUnlockEntity5_1.setCrystalCost(10);
            levelUnlockEntity5_1.setInternalName("levelUnlockEntity5_1");
            LevelUnlockEntity levelUnlockEntity5_2 = new LevelUnlockEntity();
            levelUnlockEntity5_2.setBaseItemType(entityManager.find(BaseItemTypeEntity.class, BASE_ITEM_TYPE_HARVESTER_ID));
            levelUnlockEntity5_2.setBaseItemTypeCount(1);
            levelUnlockEntity5_2.setCrystalCost(20);
            levelUnlockEntity5_2.setInternalName("levelUnlockEntity5_2");
            levelEntity5.fromLevelConfig(new LevelConfig().number(5).xp2LevelUp(400), itemTypeLimitation5, Arrays.asList(levelUnlockEntity5_1, levelUnlockEntity5_2));
            entityManager.persist(levelEntity5);
            LEVEL_5_ID = levelEntity5.getId();
            LEVEL_UNLOCK_ID_L5_1 = levelUnlockEntity5_1.getId();
            LEVEL_UNLOCK_ID_L5_2 = levelUnlockEntity5_2.getId();
        });

        cleanupAfterTests.add(Arrays.asList(
                new CleanupAfterTest().entity(LevelUnlockEntity.class),
                new CleanupAfterTest().tableName("LEVEL_LIMITATION"),
                new CleanupAfterTest().entity(LevelEntity.class)
        ));
    }

    protected void setupParticleEmitterSequences() {
        PARTICLE_EMITTER_SEQUENCE_1_ID = persistInTransaction(new ParticleEmitterSequenceEntity()).getId();
        PARTICLE_EMITTER_SEQUENCE_2_ID = persistInTransaction(new ParticleEmitterSequenceEntity()).getId();
        PARTICLE_EMITTER_SEQUENCE_3_ID = persistInTransaction(new ParticleEmitterSequenceEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(ParticleEmitterSequenceEntity.class)));
    }

    protected void setupParticleShapes() {
        PARTICLE_SHAPE_1_ID = persistInTransaction(new ParticleShapeEntity()).getId();
        PARTICLE_SHAPE_2_ID = persistInTransaction(new ParticleShapeEntity()).getId();
        PARTICLE_SHAPE_3_ID = persistInTransaction(new ParticleShapeEntity()).getId();
        cleanupAfterTests.add(Collections.singletonList(new CleanupAfterTest().entity(ParticleShapeEntity.class)));
    }

    public void setupPlanetDb() {
        setupGroundConfig();
        setupSlopeConfig();
        setupDrivewayConfig();
        setupTerrainObjectConfig();

        runInTransaction(entityManager -> {
            PlanetEntity planetEntity = new PlanetEntity();
            planetEntity.fromPlanetConfig(new PlanetConfig().size(new DecimalPosition(960, 960))
                    , entityManager.find(GroundConfigEntity.class, GROUND_1_ID), null, Collections.emptyMap());
            planetEntity.getTerrainSlopePositionEntities().add(createLandSlope());
            planetEntity.getTerrainObjectPositionEntities().addAll(createTerrainObjectPositions());
            entityManager.persist(planetEntity);
            PLANET_1_ID = planetEntity.getId();

            planetEntity = new PlanetEntity();
            planetEntity.fromPlanetConfig(new PlanetConfig(), entityManager.find(GroundConfigEntity.class, GROUND_1_ID), null, Collections.emptyMap());
            entityManager.persist(planetEntity);
            PLANET_2_ID = planetEntity.getId();
        });

        cleanupAfterTests.add(Arrays.asList(
                new CleanupAfterTest().entity(TerrainSlopeCornerEntity.class),
                new CleanupAfterTest().entity(TerrainSlopePositionEntity.class),
                new CleanupAfterTest().entity(TerrainObjectPositionEntity.class),
                new CleanupAfterTest().tableName("PLANET_LIMITATION"),
                new CleanupAfterTest().entity(PlanetEntity.class)));

//        ServerGameEngineConfigEntity serverGameEngineConfigEntity1 = new ServerGameEngineConfigEntity();
//        serverGameEngineConfigEntity1.setPlanetEntity(planetEntity2);
//
//        ServerLevelQuestEntity serverLevelQuestEntityL4 = new ServerLevelQuestEntity();
//        serverLevelQuestEntityL4.setMinimalLevel(entityManager.find(LevelEntity.class, LEVEL_4_ID));
//        QuestConfigEntity questConfigEntityL41 = new QuestConfigEntity();
//        questConfigEntityL41.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L4 1").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1))), Locale.US);
//        QuestConfigEntity questConfigEntityL42 = new QuestConfigEntity();
//        questConfigEntityL42.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L4 2").setXp(200).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(2))), Locale.US);
//        serverLevelQuestEntityL4.setQuestConfigs(Arrays.asList(questConfigEntityL41, questConfigEntityL42));
//
//        ServerLevelQuestEntity serverLevelQuestEntityL5 = new ServerLevelQuestEntity();
//        serverLevelQuestEntityL5.setMinimalLevel(entityManager.find(LevelEntity.class, LEVEL_5_ID));
//        QuestConfigEntity questConfigEntityL51 = new QuestConfigEntity();
//        questConfigEntityL51.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 1").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1))), Locale.US);
//        QuestConfigEntity questConfigEntityL52 = new QuestConfigEntity();
//        questConfigEntityL52.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 2").setXp(200).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BASE_KILLED).setComparisonConfig(new ComparisonConfig().setCount(2))), Locale.US);
//        QuestConfigEntity questConfigEntityL53 = new QuestConfigEntity();
//        questConfigEntityL53.fromQuestConfig(null, new QuestConfig().setInternalName("Test Server Quest L5 3").setXp(50).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100))), Locale.US);
//        serverLevelQuestEntityL5.setQuestConfigs(Arrays.asList(questConfigEntityL51, questConfigEntityL52, questConfigEntityL53));
//
//        serverGameEngineConfigEntity1.setServerQuestEntities(Arrays.asList(serverLevelQuestEntityL4, serverLevelQuestEntityL5));
//        entityManager.persist(serverGameEngineConfigEntity1);
//        SERVER_GAME_ENGINE_CONFIG_ID_1 = serverGameEngineConfigEntity1.getId();
//        SERVER_QUEST_ID_L4_1 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(0).getId();
//        SERVER_QUEST_ID_L4_2 = serverGameEngineConfigEntity1.getServerQuestEntities().get(0).getQuestConfigs().get(1).getId();
//        SERVER_QUEST_ID_L5_1 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(0).getId();
//        SERVER_QUEST_ID_L5_2 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(1).getId();
//        SERVER_QUEST_ID_L5_3 = serverGameEngineConfigEntity1.getServerQuestEntities().get(1).getQuestConfigs().get(2).getId();
    }

    private TerrainSlopePositionEntity createLandSlope() {
        TerrainSlopePositionEntity terrainSlopePositionLand = new TerrainSlopePositionEntity();
        terrainSlopePositionLand.setSlopeConfigEntity(entityManager.find(SlopeConfigEntity.class, SLOPE_LAND_CONFIG_ENTITY_1));
        terrainSlopePositionLand.setPolygon(Arrays.asList(
                new TerrainSlopeCornerEntity().position(new DecimalPosition(50, 50)),
                new TerrainSlopeCornerEntity().position(new DecimalPosition(400, 50)).drivewayConfigEntity(entityManager.find(DrivewayConfigEntity.class, DRIVEWAY_CONFIG_ENTITY_1)),
                new TerrainSlopeCornerEntity().position(new DecimalPosition(400, 200)).drivewayConfigEntity(entityManager.find(DrivewayConfigEntity.class, DRIVEWAY_CONFIG_ENTITY_1)),
                new TerrainSlopeCornerEntity().position(new DecimalPosition(50, 200))));
        return terrainSlopePositionLand;
    }

    private List<TerrainObjectPositionEntity> createTerrainObjectPositions() {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        TerrainObjectPositionEntity top1 = new TerrainObjectPositionEntity();
        top1.fromTerrainObjectPosition(new TerrainObjectPosition()
                        .terrainObjectId(TERRAIN_OBJECT_1_ID)
                        .position(new DecimalPosition(25, 25))
                        .scale(new Vertex(0.5, 1, 1.25))
                        .rotation(new Vertex(1.1, 1.2, 1.3))
                , terrainObjectCrudPersistence());
        terrainObjectPositionEntities.add(top1);
        TerrainObjectPositionEntity top2 = new TerrainObjectPositionEntity();
        top2.fromTerrainObjectPosition(new TerrainObjectPosition()
                        .terrainObjectId(TERRAIN_OBJECT_2_ID)
                        .position(new DecimalPosition(450, 25))
                        .scale(new Vertex(0.6, 1.1, 1.3))
                        .rotation(new Vertex(1.2, 1.3, 1.4))
                , terrainObjectCrudPersistence());
        terrainObjectPositionEntities.add(top2);
        TerrainObjectPositionEntity top3 = new TerrainObjectPositionEntity();
        top3.fromTerrainObjectPosition(new TerrainObjectPosition()
                        .terrainObjectId(TERRAIN_OBJECT_3_ID)
                        .position(new DecimalPosition(300, 250))
                        .scale(new Vertex(0.8, 0.6, 0.7))
                        .rotation(new Vertex(0.9, 0.8, 0.7))
                , terrainObjectCrudPersistence());
        terrainObjectPositionEntities.add(top3);
        return terrainObjectPositionEntities;
    }

    private TerrainObjectCrudPersistence terrainObjectCrudPersistence() {
        return new TerrainObjectCrudPersistence() {
            @Override
            public TerrainObjectEntity getEntity(Integer id) {
                return entityManager.find(TerrainObjectEntity.class, id);
            }
        };
    }

    protected void setupDb() {
        setupParticleEmitterSequences();
        setupParticleShapes();
        setupLevelDb();
        setupPlanetDb();
        runInTransaction(entityManager -> {
            GameUiContextEntity gameUiControlConfigEntity1 = new GameUiContextEntity();
            gameUiControlConfigEntity1.fromConfig(new GameUiContextConfig().gameEngineMode(GameEngineMode.MASTER),
                    entityManager.find(LevelEntity.class, LEVEL_1_ID),
                    entityManager.find(PlanetEntity.class, PLANET_1_ID));
            entityManager.persist(gameUiControlConfigEntity1);
            GAME_UI_CONTEXT_CONFIG_1_ID = gameUiControlConfigEntity1.getId();

            GameUiContextEntity gameUiControlConfigEntity2 = new GameUiContextEntity();
            gameUiControlConfigEntity2.fromConfig(new GameUiContextConfig().gameEngineMode(GameEngineMode.SLAVE),
                    entityManager.find(LevelEntity.class, LEVEL_3_ID),
                    entityManager.find(PlanetEntity.class, PLANET_2_ID));
            entityManager.persist(gameUiControlConfigEntity2);
            GAME_UI_CONTEXT_CONFIG_2_ID = gameUiControlConfigEntity2.getId();
        });

        cleanupAfterTests.add(Arrays.asList(
                new CleanupAfterTest().entity(SceneEntity.class),
                new CleanupAfterTest().entity(GameUiContextEntity.class)));
    }

    protected I18nString i18nHelper(String string) {
        Map<String, String> localizedStrings = new HashMap<>();
        localizedStrings.put(I18nString.DE, string);
        return new I18nString(localizedStrings);
    }

    protected <T> T persistInTransaction(T object) {
        entityTransaction.begin();
        entityManager.joinTransaction();
        entityManager.persist(object);
        entityTransaction.commit();
        return object;
    }

    protected void runInTransaction(Consumer<EntityManager> consumer) {
        entityTransaction.begin();
        try {
            entityManager.joinTransaction();
            consumer.accept(entityManager);
            entityTransaction.commit();
        } catch (Throwable throwable) {
            entityTransaction.rollback();
            throw throwable;
        }
    }


    protected <T> T runInTransactionAndReturn(Function<EntityManager, T> function) {
        entityTransaction.begin();
        entityManager.joinTransaction();
        T result = function.apply(entityManager);
        entityTransaction.commit();
        return result;
    }

    protected void runInTransactionSave(Consumer<EntityManager> consumer) {
        try {
            entityTransaction.begin();
            entityManager.joinTransaction();
            consumer.accept(entityManager);
            entityTransaction.commit();
        } catch (Throwable t) {
            t.printStackTrace();
            entityTransaction.rollback();
        }
    }

    protected void setupPlanetWithSlopes() throws Exception {
        setupSlopeConfig();
        setupPlanetDb();

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();

        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.slopeConfigId(SLOPE_LAND_CONFIG_ENTITY_1);
        terrainSlopePositionLand.polygon(Arrays.asList(new TerrainSlopeCorner().setPosition(new DecimalPosition(50, 40)), new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 40)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 60)), new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 90)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(100, 110)), new TerrainSlopeCorner().setPosition(new DecimalPosition(50, 110))));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.slopeConfigId(SLOPE_WATER_CONFIG_ENTITY_2);
        terrainSlopePositionWater.polygon(Arrays.asList(new TerrainSlopeCorner().setPosition(new DecimalPosition(64, 200)), new TerrainSlopeCorner().setPosition(new DecimalPosition(231, 200)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(231, 256)), new TerrainSlopeCorner().setPosition(new DecimalPosition(151, 257)),
                new TerrainSlopeCorner().setPosition(new DecimalPosition(239, 359)), new TerrainSlopeCorner().setPosition(new DecimalPosition(49, 360))));
        terrainSlopePositions.add(terrainSlopePositionWater);

        // TODO planetCrudPersistence.createTerrainSlopePositions(PLANET_2_ID, terrainSlopePositions);

        // Start from ServletContextMonitor.contextInitialized() not working
        // TODO serverGameEngineControl.start(null, true);
    }

    protected void setupPlanetFastTickGameEngine() throws Exception {
        PlanetService.TICK_TIME_MILLI_SECONDS = 1;
        setupPlanetWithSlopes();
    }

    protected void cleanPlanets() {
        cleanTable(ServerLevelQuestEntity.class);
        cleanTableNative("SERVER_QUEST");
        cleanTable(QuestConfigEntity.class);
        cleanTable(ConditionConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BOT");
        cleanTable(ComparisonConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BASE_ITEM");

        cleanTable(WaterConfigEntity.class);
        cleanTable(GroundConfigEntity.class);
        cleanTable(ServerGameEngineConfigEntity.class);
    }

    protected void cleanPlanetWithSlopes() throws Exception {
        cleanSlopeEntities();

        cleanPlanets();
    }

    protected void cleanPlanetFastTickGameEngine() throws Exception {
        PlanetService.TICK_TIME_MILLI_SECONDS = PlanetService.DEFAULT_TICK_TIME_MILLI_SECONDS;
        cleanPlanetWithSlopes();
    }

    protected void cleanSlopeEntities() {
        cleanTable(TerrainSlopeCornerEntity.class);
        cleanTable(TerrainSlopePositionEntity.class);

        // TODO cleanTable(SlopeNodeEntity.class);
        cleanTable(SlopeShapeEntity.class);
        cleanTable(SlopeConfigEntity.class);
    }

    protected void assertCount(int countExpected, Class entityClass) {
        Assert.assertEquals(countExpected, ((Number) getEntityManager().createQuery("SELECT COUNT(e) FROM " + entityClass.getName() + " e").getSingleResult()).intValue());
    }

    protected void assertEmptyCount(Class entityClass) {
        assertCount(0, entityClass);
    }

    protected void assertCountNative(int countExpected, String tableName) {
        Assert.assertEquals(countExpected, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tableName).getSingleResult()).intValue());
    }

    protected void assertEmptyCountNative(String tableName) {
        assertCountNative(0, tableName);
    }

    protected void cleanTable(Class entityClass) {
        try {
            runInTransaction(em -> em.createQuery("DELETE FROM " + entityClass.getName()).executeUpdate());
        } catch (Throwable t) {
            throw new RuntimeException("Can not clean table. entityClass: " + entityClass, t);
        }
    }

    protected void cleanTableNative(String tableName) {
        try {
            runInTransaction(em -> em.createNativeQuery("DELETE FROM " + tableName).executeUpdate());
        } catch (Throwable t) {
            throw new RuntimeException("Can not clean table. tableName: " + tableName, t);
        }
    }

    protected void printSqlStatement(String sql) {
        Query q = entityManager.createNativeQuery(sql);
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
        MongoClient mongoClient = new MongoClient();
        mongoClient.getDatabase("razarion").drop();
        mongoClient.close();
    }

    protected <T> MongoCollection<T> getMongoCollection(String collectionName, Class<T> theClass) {
        closeMongoDb();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        mongoClient = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        return mongoClient.getDatabase("razarion").getCollection(collectionName, theClass);
    }

    protected void closeMongoDb() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
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
            closeMongoDb();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> List<T> readMongoDb(String collectionName, Class<T> theClass) {
        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, theClass);
        FindIterable<T> findIterable = mongoCollection.find();
        List<T> result = new ArrayList<>();
        findIterable.forEach((Consumer<T>) result::add);
        closeMongoDb();
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

    protected UserContext handleNewUnverifiedUser(String email, String passwor) {
        RegisterResult registerResult = userService.createUnverifiedUserAndLogin(email, passwor);
        if (registerResult != RegisterResult.OK) {
            throw new IllegalStateException("createUnverifiedUserAndLogin failed: " + registerResult);
        }
        return userService.getUserContextFromSession();
    }

    public String getEmailVerificationUuid(String email) {
        return runInTransactionAndReturn(em -> (String) em.createQuery("SELECT u.verificationId FROM UserEntity u where u.email=:email").setParameter("email", email).getSingleResult());
    }

    public String getForgotPasswordUuid(String email) {
        return runInTransactionAndReturn(em -> (String) em.createQuery("SELECT p.uuid FROM ForgotPasswordEntity p where p.user.email=:email").setParameter("email", email).getSingleResult());
    }

    protected QuestConfig readQuestConfig(int questId) {
        SingleHolder<QuestConfig> holder = new SingleHolder<>();
        runInTransaction(entityManager -> {
            holder.setO(entityManager.find(QuestConfigEntity.class, questId).toQuestConfig(Locale.US));
        });
        return holder.getO();
    }

}
