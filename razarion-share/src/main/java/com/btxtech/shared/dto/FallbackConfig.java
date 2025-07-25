package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.*;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.*;

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
    public static final int DRIVEWAY_ID_ID = 1001;
    public static final int PLANET_ID = 100;

    private FallbackConfig() {

    }

    public static ColdGameUiContext coldGameUiControlConfig(UserContext userContextFromSession) {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        coldGameUiContext.userContext(userContextFromSession);
        coldGameUiContext.staticGameConfig(setupStaticGameConfig());
        coldGameUiContext.warmGameUiContext(warmGameUiControlConfig());
        coldGameUiContext.audioConfig(new AudioConfig());
        coldGameUiContext.inGameQuestVisualConfig(new InGameQuestVisualConfig()
                .nodesMaterialId(1)
                .radius(40)
                .outOfViewNodesMaterialId(2)
                .outOfViewSize(10)
                .outOfViewDistanceFromCamera(3));
        return coldGameUiContext;
    }

    public static WarmGameUiContext warmGameUiControlConfig() {
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext();
        warmGameUiContext.setPlanetConfig(setupPlanetConfig());
        warmGameUiContext.setGameEngineMode(GameEngineMode.MASTER);
        warmGameUiContext.setSceneConfigs(Collections.singletonList(new SceneConfig().wait4LevelUpDialog(true)));
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
        bulldozer.health(40).boxPickupRange(1).id(BUILDER_ITEM_TYPE_ID).internalName("Builder test");
        bulldozer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.0).angularVelocity(Math.toRadians(30)).radius(2).speed(20.0));
        bulldozer.setBuilderType(new BuilderType().ableToBuildIds(Arrays.asList(FACTORY_ITEM_TYPE_ID, GENERATOR_ITEM_TYPE_ID, CONSUMER_ITEM_TYPE_ID, HARBOUR_ITEM_TYPE_ID)).progress(5).range(5).rangeOtherTerrain(15));
        baseItemTypes.add(bulldozer);
    }

    public static void setupFactory(List<BaseItemType> baseItemTypes) {
        BaseItemType factory = new BaseItemType();
        factory.health(30).id(FACTORY_ITEM_TYPE_ID).setInternalName("Factory test");
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(5));
        factory.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(BUILDER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID, HARVESTER_ITEM_TYPE_ID)).setProgress(2));
        baseItemTypes.add(factory);
    }

    public static void setupHarbour(List<BaseItemType> baseItemTypes) {
        BaseItemType harbour = new BaseItemType();
        harbour.health(40).id(HARBOUR_ITEM_TYPE_ID).setInternalName("Harbour test");
        harbour.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).radius(3.0));
        harbour.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(SHIP_ATTACKER_ITEM_TYPE_ID, SHIP_HARVESTER_ITEM_TYPE_ID, SHIP_TRANSPORTER_ITEM_TYPE_ID)).setProgress(3));
        baseItemTypes.add(harbour);
    }

    public static void setupAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.health(20).buildup(8).id(ATTACKER_ITEM_TYPE_ID).internalName("Attacker test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.0).angularVelocity(Math.toRadians(30)).radius(2).speed(20.0));
        attacker.setWeaponType(new WeaponType().damage(5).range(10).projectileSpeed(20.0).reloadTime(2).turretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTurretCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public static void setupShipAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.health(30).buildup(12).id(SHIP_ATTACKER_ITEM_TYPE_ID).internalName("Ship attacker test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(0.5).angularVelocity(Math.toRadians(30)).radius(3).speed(10.0));
        attacker.setWeaponType(new WeaponType().damage(5).range(25).projectileSpeed(20.0).reloadTime(2).turretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTurretCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public static void setupShipTransporter(List<BaseItemType> baseItemTypes) {
        BaseItemType transporter = new BaseItemType();
        transporter.health(40).buildup(15).id(SHIP_TRANSPORTER_ITEM_TYPE_ID).internalName("Ship transporter test");
        transporter.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(0.7).angularVelocity(Math.toRadians(45)).radius(4).speed(8.0));
        transporter.setItemContainerType(new ItemContainerType().setAbleToContain(Arrays.asList(ATTACKER_ITEM_TYPE_ID, BUILDER_ITEM_TYPE_ID)).setMaxCount(5).setRange(15));
        baseItemTypes.add(transporter);
    }

    public static void setupGenerator(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.health(15).buildup(10).id(GENERATOR_ITEM_TYPE_ID).internalName("Power planet test");
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(2));
        consumer.setGeneratorType(new GeneratorType().setWattage(80));
        baseItemTypes.add(consumer);
    }

    public static void setupConsumer(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.health(15).buildup(20).id(CONSUMER_ITEM_TYPE_ID).internalName("Consumer test");
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).radius(1.5));
        consumer.setConsumerType(new ConsumerType().setWattage(60));
        baseItemTypes.add(consumer);
    }

    public static void setupHarvester(List<BaseItemType> baseItemTypes) {
        BaseItemType harvester = new BaseItemType();
        harvester.health(17).buildup(18).id(HARVESTER_ITEM_TYPE_ID).internalName("Harvester test");
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(1.5).angularVelocity(Math.toRadians(40)).radius(2).speed(15.0));
        harvester.setHarvesterType(new HarvesterType().progress(2.0).range(2));
        baseItemTypes.add(harvester);
    }

    public static void setupShipHarvester(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.health(10).buildup(5).id(SHIP_HARVESTER_ITEM_TYPE_ID).internalName("Ship harvester test");
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.WATER).acceleration(1.5).angularVelocity(Math.toRadians(20)).radius(2.5).speed(15.0));
        attacker.setHarvesterType(new HarvesterType().range(10).progress(2.0));
        baseItemTypes.add(attacker);
    }

    public static void setupMoveTestUnits(List<BaseItemType> baseItemTypes) {
        BaseItemType moveTest = new BaseItemType();
        moveTest.health(1).buildup(1).id(MOVING_TEST_ITEM_TYPE_ID).internalName("Move Test 1");
        moveTest.setPhysicalAreaConfig(new PhysicalAreaConfig().terrainType(TerrainType.LAND).acceleration(5.0).angularVelocity(Math.toRadians(180)).radius(2).speed(17.0));
        baseItemTypes.add(moveTest);
    }

    public static List<ResourceItemType> setupResourceItemType() {
        List<ResourceItemType> resourceItemTypes = new ArrayList<>();
        setupResource(resourceItemTypes);
        return resourceItemTypes;
    }

    public static void setupResource(List<ResourceItemType> resourceItemTypes) {
        ResourceItemType resourceItemType = new ResourceItemType();
        resourceItemType.id(RESOURCE_ITEM_TYPE_ID).internalName("Test resource");
        resourceItemType.setRadius(2).setTerrainType(TerrainType.LAND).setAmount(10000000).setFixVerticalNorm(false);
        resourceItemTypes.add(resourceItemType);
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
        resourceItemType.radius(1).terrainType(TerrainType.LAND).fixVerticalNorm(false);
        // resourceItemType.setBoxItemTypePossibilities()
        resourceItemType.ttl(250);
        boxItemTypes.add(resourceItemType);
    }

    public static void setupLongBox(List<BoxItemType> boxItemTypes) {
        BoxItemType resourceItemType = new BoxItemType();
        resourceItemType.id(BOX_ITEM_TYPE_LONG_ID).internalName("Test box long");
        resourceItemType.radius(1).terrainType(TerrainType.LAND).fixVerticalNorm(false);
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setInventoryItemId(INVENTORY_ITEM_ATTACKER_ID).setPossibility(1));
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setInventoryItemId(INVENTORY_ITEM_GOLD_ID).setPossibility(1));
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setCrystals(10).setPossibility(1));
        resourceItemType.boxItemTypePossibilities(boxItemTypePossibilities);
        resourceItemType.ttl(Integer.MAX_VALUE);
        boxItemTypes.add(resourceItemType);
    }

    public static List<InventoryItem> setupInventoryItems() {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        inventoryItems.add(new InventoryItem().id(INVENTORY_ITEM_ATTACKER_ID).baseItemTypeCount(3).baseItemTypeId(ATTACKER_ITEM_TYPE_ID).baseItemTypeFreeRange(1));
        inventoryItems.add(new InventoryItem().id(INVENTORY_ITEM_GOLD_ID).razarion(100));
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

    public static PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.id(PLANET_ID);
        planetConfig.setItemTypeLimitation(setupPlanetItemTypeLimitations());
        planetConfig.setSize(new DecimalPosition(320, 320));
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
