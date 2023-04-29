package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FallbackConfig {
    public static final int GROUND_CONFIG_ID = 1;
    public static final int BUILDER_ITEM_TYPE_ID = 1;
    public static final int FACTORY_ITEM_TYPE_ID = 2;
    public static final int ATTACKER_ITEM_TYPE_ID = 3;
    public static final int GENERATOR_ITEM_TYPE_ID = 4;
    public static final int CONSUMER_ITEM_TYPE_ID = 5;
    public static final int HARVESTER_ITEM_TYPE_ID = 6;
    public static final int HARBOUR_ITEM_TYPE_ID = 7;
    public static final int SHIP_ATTACKER_ITEM_TYPE_ID = 8;
    public static final int SHIP_HARVESTER_ITEM_TYPE_ID = 9;
    public static final int SHIP_TRANSPORTER_ITEM_TYPE_ID = 10;
    public static final int MOVING_TEST_ITEM_TYPE_ID = 11;
    public static final int RESOURCE_ITEM_TYPE_ID = 101;
    public static final int BOX_ITEM_TYPE_ID = 501;
    public static final int BOX_ITEM_TYPE_LONG_ID = 502;
    public static final int LEVEL_ID_1 = 1;
    public static final int INVENTORY_ITEM_ATTACKER_ID = 1;
    public static final int INVENTORY_ITEM_GOLD_ID = 1;
    public static final int SLOPE_CONFIG_LAND_ID = 1001;
    public static final int SLOPE_CONFIG_WATER_ID = 2001;
    public static final int WATER_CONFIG_ID = 1;
    public static final int DRIVEWAY_ID_ID = 1001;
    public static final int PLANET_ID = 100;

    private FallbackConfig() {

    }

    public static ColdGameUiContext coldGameUiControlConfig(UserContext userContextFromSession) {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        coldGameUiContext.userContext(userContextFromSession);
        coldGameUiContext.shape3Ds(new ArrayList<>());
        coldGameUiContext.staticGameConfig(setupStaticGameConfig());
        coldGameUiContext.warmGameUiContext(warmGameUiControlConfig());
        coldGameUiContext.audioConfig(new AudioConfig());
        return coldGameUiContext;
    }

    public static WarmGameUiContext warmGameUiControlConfig() {
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext();
        warmGameUiContext.setPlanetConfig(setupPlanetConfig());
        warmGameUiContext.setGameEngineMode(GameEngineMode.MASTER);
        warmGameUiContext.setSceneConfigs(Collections.singletonList(new SceneConfig().setRemoveLoadingCover(true).setWait4LevelUpDialog(true)));
        warmGameUiContext.setPlanetVisualConfig(new PlanetVisualConfig().setLightDirection(Vertex.Z_NORM_NEG));
        return warmGameUiContext;
    }

    public static StaticGameConfig setupStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundConfigs(Collections.singletonList(new GroundConfig().id(GROUND_CONFIG_ID)));
        staticGameConfig.setLevelConfigs(setupLevelConfigs());
        staticGameConfig.setBaseItemTypes(setupBaseItemType());
        staticGameConfig.setResourceItemTypes(setupResourceItemType());
        staticGameConfig.setBoxItemTypes(setupBoxItemType());
        staticGameConfig.setInventoryItems(setupInventoryItems());
        staticGameConfig.setSlopeConfigs(setupSlopeSkeletonConfigs());
        staticGameConfig.setDrivewayConfigs(setupDriveways());
        staticGameConfig.setWaterConfigs(setupWaterConfigs());
        return staticGameConfig;
    }

    public static List<BaseItemType> setupBaseItemType() {
        List<BaseItemType> baseItemTypes = new ArrayList<>();
        setupBuilder(baseItemTypes);
        setupFactory(baseItemTypes);
        setupAttacker(baseItemTypes);
        setupShipAttacker(baseItemTypes);
        setupShipTransporter(baseItemTypes);
        setupGenerator(baseItemTypes);
        setupConsumer(baseItemTypes);
        setupHarvester(baseItemTypes);
        setupShipHarvester(baseItemTypes);
        setupHarbour(baseItemTypes);
        setupMoveTestUnits(baseItemTypes);
        return baseItemTypes;
    }

    public static void setupBuilder(List<BaseItemType> baseItemTypes) {
        BaseItemType bulldozer = new BaseItemType();
        bulldozer.setHealth(40).setBoxPickupRange(1).id(BUILDER_ITEM_TYPE_ID).internalName("Builder test");
        bulldozer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.0).angularVelocity(Math.toRadians(30)).radius(3).speed(20.0));
        bulldozer.setBuilderType(new BuilderType().ableToBuildIds(Arrays.asList(FACTORY_ITEM_TYPE_ID, GENERATOR_ITEM_TYPE_ID, CONSUMER_ITEM_TYPE_ID, HARBOUR_ITEM_TYPE_ID)).animationOrigin(new Vertex(3, 5, 17)).progress(5).range(10));
        baseItemTypes.add(bulldozer);
    }

    public static void setupFactory(List<BaseItemType> baseItemTypes) {
        BaseItemType factory = new BaseItemType();
        factory.setHealth(30).id(FACTORY_ITEM_TYPE_ID).setInternalName("Factory test");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(5));
        factory.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(BUILDER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID, HARVESTER_ITEM_TYPE_ID)).setProgress(2));
        baseItemTypes.add(factory);
    }

    public static void setupHarbour(List<BaseItemType> baseItemTypes) {
        BaseItemType harbour = new BaseItemType();
        harbour.setHealth(40).id(HARBOUR_ITEM_TYPE_ID).setInternalName("Harbour test");
        harbour.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER_COAST).radius(4.5));
        harbour.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(SHIP_ATTACKER_ITEM_TYPE_ID, SHIP_HARVESTER_ITEM_TYPE_ID, SHIP_TRANSPORTER_ITEM_TYPE_ID)).setProgress(3));
        baseItemTypes.add(harbour);
    }

    public static void setupAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(20).setBuildup(8).id(ATTACKER_ITEM_TYPE_ID).internalName("Attacker test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.0).angularVelocity(Math.toRadians(30)).radius(2).speed(20.0));
        attacker.setWeaponType(new WeaponType().damage(5).range(10).projectileSpeed(20.0).reloadTime(2).turretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTurretCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public static void setupShipAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(30).setBuildup(12).id(SHIP_ATTACKER_ITEM_TYPE_ID).internalName("Ship attacker test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(0.5).angularVelocity(Math.toRadians(30)).radius(3).speed(10.0));
        attacker.setWeaponType(new WeaponType().damage(5).range(15).projectileSpeed(20.0).reloadTime(2).turretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTurretCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public static void setupShipTransporter(List<BaseItemType> baseItemTypes) {
        BaseItemType transporter = new BaseItemType();
        transporter.setHealth(40).setBuildup(15).id(SHIP_TRANSPORTER_ITEM_TYPE_ID).internalName("Ship transporter test");
        transporter.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(0.7).angularVelocity(Math.toRadians(45)).radius(4).speed(8.0));
        transporter.setItemContainerType(new ItemContainerType().setAbleToContain(Arrays.asList(ATTACKER_ITEM_TYPE_ID, BUILDER_ITEM_TYPE_ID)).setMaxCount(5).setRange(15));
        baseItemTypes.add(transporter);
    }

    public static void setupGenerator(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(10).id(GENERATOR_ITEM_TYPE_ID).internalName("Power planet test");
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(2));
        consumer.setGeneratorType(new GeneratorType().setWattage(80));
        baseItemTypes.add(consumer);
    }

    public static void setupConsumer(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(20).id(CONSUMER_ITEM_TYPE_ID).internalName("Consumer test");
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(1.5));
        consumer.setConsumerType(new ConsumerType().setWattage(60));
        baseItemTypes.add(consumer);
    }

    public static void setupHarvester(List<BaseItemType> baseItemTypes) {
        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(17).setBuildup(18).id(HARVESTER_ITEM_TYPE_ID).internalName("Harvester test");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.5).angularVelocity(Math.toRadians(40)).radius(2).speed(15.0));
        harvester.setHarvesterType(new HarvesterType().setProgress(2.0).setRange(2));
        baseItemTypes.add(harvester);
    }

    public static void setupShipHarvester(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(10).setBuildup(5).id(SHIP_HARVESTER_ITEM_TYPE_ID).internalName("Ship harvester test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(1.5).angularVelocity(Math.toRadians(20)).radius(2.5).speed(15.0));
        attacker.setHarvesterType(new HarvesterType().setRange(10).setProgress(2.0).setAnimationOrigin(new Vertex(1, 0, 0)).setAnimationShape3dId(999111));
        baseItemTypes.add(attacker);
    }

    public static void setupMoveTestUnits(List<BaseItemType> baseItemTypes) {
        BaseItemType moveTest = new BaseItemType();
        moveTest.setHealth(1).setBuildup(1).id(MOVING_TEST_ITEM_TYPE_ID).internalName("Move Test 1");
        moveTest.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(5.0).angularVelocity(Math.toRadians(180)).radius(2).speed(17.0));
        baseItemTypes.add(moveTest);
    }

    public static List<ResourceItemType> setupResourceItemType() {
        List<ResourceItemType> resourceItemTypes = new ArrayList<>();
        setupResource(resourceItemTypes);
        return resourceItemTypes;
    }

    public static void setupResource(List<ResourceItemType> baseItemTypes) {
        ResourceItemType resourceItemType = new ResourceItemType();
        resourceItemType.id(RESOURCE_ITEM_TYPE_ID).internalName("Test resource");
        resourceItemType.setRadius(1).setTerrainType(TerrainType.LAND).setAmount(10000000).setFixVerticalNorm(false);
        baseItemTypes.add(resourceItemType);
    }

    public static List<BoxItemType> setupBoxItemType() {
        List<BoxItemType> boxItemTypes = new ArrayList<>();
        setupBox(boxItemTypes);
        setupLongBox(boxItemTypes);
        return boxItemTypes;
    }

    public static void setupBox(List<BoxItemType> boxItemTypes) {
        BoxItemType resourceItemType = new BoxItemType();
        resourceItemType.id(BOX_ITEM_TYPE_ID).internalName("Test box");
        resourceItemType.setRadius(1).setTerrainType(TerrainType.LAND).setFixVerticalNorm(false);
        // resourceItemType.setBoxItemTypePossibilities()
        resourceItemType.setTtl(250);
        boxItemTypes.add(resourceItemType);
    }

    public static void setupLongBox(List<BoxItemType> boxItemTypes) {
        BoxItemType resourceItemType = new BoxItemType();
        resourceItemType.id(BOX_ITEM_TYPE_LONG_ID).internalName("Test box long");
        resourceItemType.setRadius(1).setTerrainType(TerrainType.LAND).setFixVerticalNorm(false);
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setInventoryItemId(INVENTORY_ITEM_ATTACKER_ID).setPossibility(1));
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setInventoryItemId(INVENTORY_ITEM_GOLD_ID).setPossibility(1));
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setCrystals(10).setPossibility(1));
        resourceItemType.setBoxItemTypePossibilities(boxItemTypePossibilities);
        resourceItemType.setTtl(Integer.MAX_VALUE);
        boxItemTypes.add(resourceItemType);
    }

    public static List<InventoryItem> setupInventoryItems() {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        inventoryItems.add(new InventoryItem().setId(INVENTORY_ITEM_ATTACKER_ID).setBaseItemTypeCount(3).setBaseItemTypeId(ATTACKER_ITEM_TYPE_ID).setBaseItemTypeFreeRange(1));
        inventoryItems.add(new InventoryItem().setId(INVENTORY_ITEM_GOLD_ID).setRazarion(100));
        return inventoryItems;
    }

    public static List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> level1Limitation = new HashMap<>();
        level1Limitation.put(BUILDER_ITEM_TYPE_ID, 1);
        level1Limitation.put(FACTORY_ITEM_TYPE_ID, 2);
        level1Limitation.put(ATTACKER_ITEM_TYPE_ID, 10);
        level1Limitation.put(SHIP_ATTACKER_ITEM_TYPE_ID, 10);
        level1Limitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        level1Limitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        level1Limitation.put(HARVESTER_ITEM_TYPE_ID, 3);
        level1Limitation.put(SHIP_HARVESTER_ITEM_TYPE_ID, 2);
        level1Limitation.put(HARBOUR_ITEM_TYPE_ID, 1);
        level1Limitation.put(SHIP_TRANSPORTER_ITEM_TYPE_ID, 1);
        level1Limitation.put(MOVING_TEST_ITEM_TYPE_ID, 1000);
        levelConfigs.add(new LevelConfig().id(LEVEL_ID_1).number(1).xp2LevelUp(2).itemTypeLimitation(level1Limitation));
        return levelConfigs;
    }

    public static List<SlopeConfig> setupSlopeSkeletonConfigs() {
        // Land
        SlopeConfig skeletonConfigLand = new SlopeConfig();
        skeletonConfigLand.setId(SLOPE_CONFIG_LAND_ID);
        skeletonConfigLand.horizontalSpace(3.0);
//   TODO     skeletonConfigLand.setSlopeNodes(new SlopeNode[][]{
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//                {GameTestHelper.createSlopeNode(1.4, 0.1, 0.1), GameTestHelper.createSlopeNode(4.6, 1.0, 0.9), GameTestHelper.createSlopeNode(5.4, 3.7, 1.0), GameTestHelper.createSlopeNode(7.0, 8.3, 0.8), GameTestHelper.createSlopeNode(9.8, 8.2, 0.0)},
//        });
        skeletonConfigLand.innerLineGameEngine(8).coastDelimiterLineGameEngine(0.0).outerLineGameEngine(2);
        // Water
        SlopeConfig skeletonConfigWater = new SlopeConfig();
        skeletonConfigWater.id(SLOPE_CONFIG_WATER_ID).waterConfigId(WATER_CONFIG_ID);
        skeletonConfigWater.horizontalSpace(3.0);
//   TODO     SlopeNode[][] slopeNodes = new SlopeNode[][]{
//                {GameTestHelper.createSlopeNode(3.5, 0.6, 0.5), GameTestHelper.createSlopeNode(10.5, -1.2, 1.0), GameTestHelper.createSlopeNode(15.6, -1.9, 1.0)},
//                {GameTestHelper.createSlopeNode(3.5, 0.6, 0.5), GameTestHelper.createSlopeNode(10.5, -1.2, 1.0), GameTestHelper.createSlopeNode(15.6, -1.9, 1.0)},
//                {GameTestHelper.createSlopeNode(3.5, 0.6, 0.5), GameTestHelper.createSlopeNode(10.5, -1.2, 1.0), GameTestHelper.createSlopeNode(15.6, -1.9, 1.0)},
//                {GameTestHelper.createSlopeNode(3.5, 0.6, 0.5), GameTestHelper.createSlopeNode(10.5, -1.2, 1.0), GameTestHelper.createSlopeNode(15.6, -1.9, 1.0)},
//                {GameTestHelper.createSlopeNode(3.5, 0.6, 0.5), GameTestHelper.createSlopeNode(10.5, -1.2, 1.0), GameTestHelper.createSlopeNode(15.6, -1.9, 1.0)},
//        };
//        skeletonConfigWater.setSlopeNodes(slopeNodes);
        skeletonConfigWater.innerLineGameEngine(11.0).coastDelimiterLineGameEngine(8.0).outerLineGameEngine(4.0);

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        slopeConfigs.add(skeletonConfigLand);
        slopeConfigs.add(skeletonConfigWater);
        return slopeConfigs;
    }

    public static List<WaterConfig> setupWaterConfigs() {
        List<WaterConfig> waterConfigs = new ArrayList<>();
        waterConfigs.add(new WaterConfig().id(WATER_CONFIG_ID).waterLevel(-0.7).groundLevel(-2));
        return waterConfigs;
    }

    public static List<DrivewayConfig> setupDriveways() {
        List<DrivewayConfig> drivewayConfigs = new ArrayList<>();
        drivewayConfigs.add(new DrivewayConfig().id(DRIVEWAY_ID_ID).angle(Math.toRadians(30)));
        return drivewayConfigs;
    }

    public static PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.id(PLANET_ID);
        planetConfig.setItemTypeLimitation(setupPlanetItemTypeLimitations());
        planetConfig.setSize(new DecimalPosition(960, 960));
        planetConfig.setStartBaseItemTypeId(BUILDER_ITEM_TYPE_ID);
        planetConfig.setGroundConfigId(GROUND_CONFIG_ID);
        return planetConfig;
    }

    public static Map<Integer, Integer> setupPlanetItemTypeLimitations() {
        Map<Integer, Integer> levelLimitation = new HashMap<>();
        levelLimitation.put(BUILDER_ITEM_TYPE_ID, 2);
        levelLimitation.put(FACTORY_ITEM_TYPE_ID, 3);
        levelLimitation.put(ATTACKER_ITEM_TYPE_ID, 10);
        levelLimitation.put(SHIP_ATTACKER_ITEM_TYPE_ID, 10);
        levelLimitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        levelLimitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        levelLimitation.put(HARVESTER_ITEM_TYPE_ID, 3);
        levelLimitation.put(SHIP_HARVESTER_ITEM_TYPE_ID, 2);
        levelLimitation.put(HARBOUR_ITEM_TYPE_ID, 3);
        levelLimitation.put(SHIP_TRANSPORTER_ITEM_TYPE_ID, 1);
        levelLimitation.put(MOVING_TEST_ITEM_TYPE_ID, 1000);
        return levelLimitation;
    }

    public static MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

}
