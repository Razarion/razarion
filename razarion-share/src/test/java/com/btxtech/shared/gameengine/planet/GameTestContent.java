package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public interface GameTestContent {
    int GROUND_SKELETON_ID = 1;
    int BUILDER_ITEM_TYPE_ID = 1;
    int FACTORY_ITEM_TYPE_ID = 2;
    int ATTACKER_ITEM_TYPE_ID = 3;
    int GENERATOR_ITEM_TYPE_ID = 4;
    int CONSUMER_ITEM_TYPE_ID = 5;
    int HARVESTER_ITEM_TYPE_ID = 6;
    int RESOURCE_ITEM_TYPE_ID = 101;
    int LEVEL_ID_1 = 1;

    static StaticGameConfig setupStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundSkeletonConfig(new GroundSkeletonConfig().setId(GROUND_SKELETON_ID).setHeights(new double[][]{{0.0}}).setHeightXCount(1).setHeightYCount(1));
        staticGameConfig.setLevelConfigs(setupLevelConfigs());
        staticGameConfig.setBaseItemTypes(setupBaseItemType());
        staticGameConfig.setResourceItemTypes(setupResourceItemType());
        return staticGameConfig;
    }

    static PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setItemTypeLimitation(setupPlanetItemTypeLimitations());
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1000, 1000));
        planetConfig.setPlayGround(new Rectangle2D(0, 0, 6, 6));
        planetConfig.setStartBaseItemTypeId(BUILDER_ITEM_TYPE_ID);
        return planetConfig;
    }

    static List<BaseItemType> setupBaseItemType() {
        List<BaseItemType> baseItemTypes = new ArrayList<>();
        setupBuilder(baseItemTypes);
        setupFactory(baseItemTypes);
        setupAttacker(baseItemTypes);
        setupGenerator(baseItemTypes);
        setupConsumer(baseItemTypes);
        setupHarvester(baseItemTypes);
        return baseItemTypes;
    }

    static void setupBuilder(List<BaseItemType> baseItemTypes) {
        BaseItemType bulldozer = new BaseItemType();
        bulldozer.setHealth(40).setId(BUILDER_ITEM_TYPE_ID);
        bulldozer.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(2).setSpeed(20.0));
        bulldozer.setBuilderType(new BuilderType().setAbleToBuildIds(Arrays.asList(FACTORY_ITEM_TYPE_ID, GENERATOR_ITEM_TYPE_ID, CONSUMER_ITEM_TYPE_ID)).setAnimationOrigin(new Vertex(3, 5, 17)).setProgress(5).setRange(2.7));
        baseItemTypes.add(bulldozer);
    }

    static void setupFactory(List<BaseItemType> baseItemTypes) {
        BaseItemType factory = new BaseItemType();
        factory.setHealth(30).setId(FACTORY_ITEM_TYPE_ID);
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(5).setSpeed(20.0));
        factory.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(BUILDER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID, HARVESTER_ITEM_TYPE_ID)).setProgress(2.9));
        baseItemTypes.add(factory);
    }

    static void setupAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(20).setId(ATTACKER_ITEM_TYPE_ID);
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(3).setSpeed(20.0));
        attacker.setWeaponType(new WeaponType().setDamage(5).setRange(10).setProjectileSpeed(20.0).setReloadTime(2).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTorrentCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    static void setupGenerator(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(10).setId(GENERATOR_ITEM_TYPE_ID);
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(2));
        consumer.setGeneratorType(new GeneratorType().setWattage(80));
        baseItemTypes.add(consumer);
    }

    static void setupConsumer(List<BaseItemType> baseItemTypes) {
        BaseItemType consumer = new BaseItemType();
        consumer.setHealth(15).setBuildup(20).setId(CONSUMER_ITEM_TYPE_ID);
        consumer.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(1.5));
        consumer.setConsumerType(new ConsumerType().setWattage(60));
        baseItemTypes.add(consumer);
    }

    static void setupHarvester(List<BaseItemType> baseItemTypes) {
        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(17).setBuildup(18).setId(HARVESTER_ITEM_TYPE_ID);
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.5).setAngularVelocity(Math.toRadians(40)).setRadius(2).setSpeed(15.0));
        harvester.setHarvesterType(new HarvesterType().setProgress(2.0).setRange(2));
        baseItemTypes.add(harvester);
    }

    static List<ResourceItemType> setupResourceItemType() {
        List<ResourceItemType> resourceItemTypes = new ArrayList<>();
        setupResource(resourceItemTypes);
        return resourceItemTypes;
    }

    static void setupResource(List<ResourceItemType> baseItemTypes) {
        ResourceItemType resourceItemType = new ResourceItemType();
        resourceItemType.setId(RESOURCE_ITEM_TYPE_ID).setInternalName("Test resource");
        resourceItemType.setRadius(1).setAmount(10000000).setFixVerticalNorm(false);
        baseItemTypes.add(resourceItemType);
    }

    static List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> level1Limitation = new HashMap<>();
        level1Limitation.put(BUILDER_ITEM_TYPE_ID, 1);
        level1Limitation.put(FACTORY_ITEM_TYPE_ID, 2);
        level1Limitation.put(ATTACKER_ITEM_TYPE_ID, 5);
        level1Limitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        level1Limitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        level1Limitation.put(HARVESTER_ITEM_TYPE_ID, 3);
        levelConfigs.add(new LevelConfig().setLevelId(LEVEL_ID_1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(level1Limitation));
        return levelConfigs;
    }

    static Map<Integer, Integer> setupPlanetItemTypeLimitations() {
        Map<Integer, Integer> levelLimitation = new HashMap<>();
        levelLimitation.put(BUILDER_ITEM_TYPE_ID, 1);
        levelLimitation.put(FACTORY_ITEM_TYPE_ID, 2);
        levelLimitation.put(ATTACKER_ITEM_TYPE_ID, 5);
        levelLimitation.put(GENERATOR_ITEM_TYPE_ID, 6);
        levelLimitation.put(CONSUMER_ITEM_TYPE_ID, 6);
        levelLimitation.put(HARVESTER_ITEM_TYPE_ID, 3);
        return levelLimitation;
    }
}
