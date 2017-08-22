package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncFactory;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncTurret;
import com.btxtech.shared.gameengine.planet.model.SyncWeapon;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class BaseItemServiceBase {
    public static final int BUILDER_ITEM_TYPE_ID = 1;
    public static final int FACTORY_ITEM_TYPE_ID = 2;
    public static final int ATTACKER_ITEM_TYPE_ID = 3;
    public static final int GENERATOR_ITEM_TYPE_ID = 4;
    public static final int CONSUMER_ITEM_TYPE_ID = 5;
    protected static final int LEVEL_ID_1 = 1;
    private BaseItemService baseItemService;
    private SyncItemContainerService syncItemContainerService;
    private ItemTypeService itemTypeService;

    protected void setup(PlanetConfig planetConfig, GameEngineMode gameEngineMode, MasterPlanetConfig masterPlanetConfig, SlaveSyncItemInfo slaveSyncItemInfo) {
        baseItemService = new BaseItemService();
        itemTypeService = new ItemTypeService();
        // SyncItemContainerService
        syncItemContainerService = new SyncItemContainerService();
        Map<Class, Supplier> selectorSupplier = new HashMap<>();
        selectorSupplier.put(SyncBaseItem.class, () -> {
            SyncBaseItem syncBaseItem = new SyncBaseItem();
            Map<Class, Supplier> instanceSupplier = new HashMap<>();
            instanceSupplier.put(SyncBuilder.class, () -> {
                SyncBuilder syncBuilder = new SyncBuilder();
                SimpleTestEnvironment.injectService("itemTypeService", syncBuilder, itemTypeService);
                return syncBuilder;
            });
            instanceSupplier.put(SyncFactory.class, () -> {
                SyncFactory syncFactory = new SyncFactory();
                SimpleTestEnvironment.injectService("itemTypeService", syncFactory, itemTypeService);
                return syncFactory;
            });
            instanceSupplier.put(SyncWeapon.class, () -> {
                SyncWeapon syncWeapon = new SyncWeapon();
                SimpleTestEnvironment.injectInstance("syncTurretInstance", syncWeapon, SyncTurret::new);
                SimpleTestEnvironment.injectService("syncItemContainerService", syncWeapon, getSyncItemContainerService());
                return syncWeapon;
            });
            SimpleTestEnvironment.injectInstance("instance", syncBaseItem, instanceSupplier);
            GameLogicService gameLogicServiceMock = EasyMock.createNiceMock(GameLogicService.class);
            EasyMock.replay(gameLogicServiceMock);
            SimpleTestEnvironment.injectService("gameLogicService", syncBaseItem, gameLogicServiceMock);
            return syncBaseItem;
        });
        SimpleTestEnvironment.injectInstance("syncItemInstance", syncItemContainerService, selectorSupplier);
        SimpleTestEnvironment.injectInstance("syncPhysicalMovableInstance", syncItemContainerService, () -> {
            SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
            SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, syncItemContainerService);
            TerrainService terrainServiceMock = EasyMock.createNiceMock(TerrainService.class);
            EasyMock.expect(terrainServiceMock.getSurfaceAccess().getInterpolatedZ(EasyMock.anyObject(DecimalPosition.class))).andReturn(-1.7);
            EasyMock.replay(terrainServiceMock);
            SimpleTestEnvironment.injectService("terrainService", syncPhysicalMovable, SyncPhysicalArea.class, terrainServiceMock);
            return syncPhysicalMovable;
        });
        SimpleTestEnvironment.injectService("syncItemContainerService", baseItemService, syncItemContainerService);
        // Level
        LevelService levelService = new LevelService();
        levelService.init(new StaticGameConfig().setLevelConfigs(setupLevelConfigs()));
        SimpleTestEnvironment.injectService("levelService", baseItemService, levelService);

        GameLogicService gameLogicServiceMock = EasyMock.createNiceMock(GameLogicService.class);
        EasyMock.replay(gameLogicServiceMock);
        SimpleTestEnvironment.injectService("gameLogicService", baseItemService, gameLogicServiceMock);

        // Setup ItemTypeService
        setupItemTypeService(itemTypeService);
        SimpleTestEnvironment.injectService("itemTypeService", baseItemService, itemTypeService);

        planetConfig.setItemTypeLimitation(setupItemTypeLimitations());

        baseItemService.onPlanetActivation(new PlanetActivationEvent(planetConfig, gameEngineMode, masterPlanetConfig, slaveSyncItemInfo, PlanetActivationEvent.Type.INITIALIZE));
    }

    public static List<BaseItemType> setupBaseItemType() {
        List<BaseItemType> baseItemTypes = new ArrayList<>();
        setupBuilder(baseItemTypes);
        setupFactory(baseItemTypes);
        setupAttacker(baseItemTypes);
        setupGenerator(baseItemTypes);
        setupConsumer(baseItemTypes);
        return baseItemTypes;
    }

    public static void setupItemTypeService(ItemTypeService itemTypeService) {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setBaseItemTypes(setupBaseItemType());
        itemTypeService.onGameEngineInit(new StaticGameInitEvent(staticGameConfig));
    }

    public static void setupBuilder(List<BaseItemType> baseItemTypes) {
        BaseItemType bulldozer = new BaseItemType();
        bulldozer.setHealth(40).setId(BUILDER_ITEM_TYPE_ID);
        bulldozer.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(2).setSpeed(20.0));
        bulldozer.setBuilderType(new BuilderType().setAbleToBuildIds(Arrays.asList(FACTORY_ITEM_TYPE_ID, GENERATOR_ITEM_TYPE_ID, CONSUMER_ITEM_TYPE_ID)).setAnimationOrigin(new Vertex(3, 5, 17)).setProgress(5).setRange(2.7));
        baseItemTypes.add(bulldozer);
    }

    public static void setupFactory(List<BaseItemType> baseItemTypes) {
        BaseItemType factory = new BaseItemType();
        factory.setHealth(30).setId(FACTORY_ITEM_TYPE_ID);
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(5).setSpeed(20.0));
        factory.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(BUILDER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID)).setProgress(2.9));
        baseItemTypes.add(factory);
    }

    public static void setupAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(20).setId(ATTACKER_ITEM_TYPE_ID);
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(3).setSpeed(20.0));
        attacker.setWeaponType(new WeaponType().setDamage(5).setRange(10).setProjectileSpeed(20.0).setReloadTime(2).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTorrentCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public static void setupGenerator(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(10).setId(GENERATOR_ITEM_TYPE_ID);
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(2));
        consumer.setGeneratorType(new GeneratorType().setWattage(80));
        baseItemTypes.add(consumer);
    }

    public static void setupConsumer(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(20).setId(CONSUMER_ITEM_TYPE_ID);
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(1.5));
        consumer.setConsumerType(new ConsumerType().setWattage(60));
        baseItemTypes.add(consumer);
    }

    public BaseItemService getBaseItemService() {
        return baseItemService;
    }

    public SyncItemContainerService getSyncItemContainerService() {
        return syncItemContainerService;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) {
        return itemTypeService.getBaseItemType(baseItemTypeId);
    }

    public static List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> level1Limitation = new HashMap<>();
        level1Limitation.put(BUILDER_ITEM_TYPE_ID, 1);
        level1Limitation.put(FACTORY_ITEM_TYPE_ID, 2);
        level1Limitation.put(ATTACKER_ITEM_TYPE_ID, 5);
        level1Limitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        level1Limitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        levelConfigs.add(new LevelConfig().setLevelId(LEVEL_ID_1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(level1Limitation));
        return levelConfigs;
    }

    public static Map<Integer, Integer> setupItemTypeLimitations() {
        Map<Integer, Integer> levelLimitation = new HashMap<>();
        levelLimitation.put(BUILDER_ITEM_TYPE_ID, 1);
        levelLimitation.put(FACTORY_ITEM_TYPE_ID, 2);
        levelLimitation.put(ATTACKER_ITEM_TYPE_ID, 5);
        levelLimitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        levelLimitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        return levelLimitation;
    }

    public static SyncBaseItem createMockSyncBaseItem(DecimalPosition position) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
        SimpleTestEnvironment.injectService("position2d", syncPhysicalMovable, SyncPhysicalArea.class, position);
        syncBaseItem.init(-99, null, syncPhysicalMovable);
        return syncBaseItem;
    }
}