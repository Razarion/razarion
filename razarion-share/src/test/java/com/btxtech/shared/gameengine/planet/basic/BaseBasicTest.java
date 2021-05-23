package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class BaseBasicTest extends WeldMasterBaseTest {
    private int lastBotId = 0;

    protected void setup() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.setHorizontalSpace(5);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7)));
        slopeConfigLand.setOuterLineGameEngine(1).setInnerLineGameEngine(6);
        slopeConfigs.add(slopeConfigLand);

        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).waterConfigId(FallbackConfig.WATER_CONFIG_ID);
        slopeConfigWater.setHorizontalSpace(6);
        slopeConfigWater.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(5, 0.5)).slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(10, -0.1)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(15, -0.8)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(20, -2)).slopeFactor(1)));
        slopeConfigWater.setOuterLineGameEngine(8).setCoastDelimiterLineGameEngine(10).setInnerLineGameEngine(16);
        slopeConfigs.add(slopeConfigWater);

        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.id(1);
        terrainSlopePositionLand.slopeConfigId(1);
        terrainSlopePositionLand.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, FallbackConfig.DRIVEWAY_ID_ID), GameTestHelper.createTerrainSlopeCorner(100, 90, FallbackConfig.DRIVEWAY_ID_ID), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.id(2);
        terrainSlopePositionWater.slopeConfigId(2);
        terrainSlopePositionWater.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(64, 200, null), GameTestHelper.createTerrainSlopeCorner(231, 200, null),
                GameTestHelper.createTerrainSlopeCorner(231, 256, null), GameTestHelper.createTerrainSlopeCorner(151, 257, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(239, 359, null), GameTestHelper.createTerrainSlopeCorner(49, 360, null)));
        terrainSlopePositions.add(terrainSlopePositionWater);


        StaticGameConfig staticGameConfig = FallbackConfig.setupStaticGameConfig();
        staticGameConfig.setSlopeConfigs(slopeConfigs);

        setupMasterEnvironment(staticGameConfig, terrainSlopePositions);
    }

    protected SyncBaseItem setupBot(String botName, int itemTypeId, DecimalPosition position, int auxiliaryId) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(itemTypeId).count(1).createDirectly(true).place(new PlaceConfig().position(position)).noRebuild(true));
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name(botName).npc(false).auxiliaryId(auxiliaryId));
        getBotService().startBots(botConfigs, null);
        tickPlanetServiceBaseServiceActive();
        PlayerBase botBase = getBotBase(botName);
        return findSyncBaseItem((PlayerBaseFull) botBase, itemTypeId);
    }

    protected HumanBaseContext createHumanBaseBFA() {
        return createHumanBaseBFA(new DecimalPosition(167, 136), new DecimalPosition(104, 144));
    }

    protected HumanBaseContext createHumanBaseBFA(DecimalPosition builderPosition, DecimalPosition factoryPosition) {
        HumanBaseContext humanBaseContext = new HumanBaseContext();
        // Human base
        UserContext userContext = createLevel1UserContext();
        humanBaseContext.setUserContext(userContext);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(builderPosition, userContext);
        humanBaseContext.setPlayerBaseFull(playerBaseFull);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        humanBaseContext.setBuilder(builder);
        getCommandService().build(builder, factoryPosition, getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        humanBaseContext.setFactory(factory);
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker1 = findSyncBaseItem(playerBaseFull, FallbackConfig.ATTACKER_ITEM_TYPE_ID);
        humanBaseContext.setAttacker1(attacker1);

        return humanBaseContext;
    }

    protected HumanBaseContext createHumanBaseBFA4(DecimalPosition builderPosition, DecimalPosition factoryPosition) {
        HumanBaseContext humanBaseContext = createHumanBaseBFA(builderPosition, factoryPosition);
        getCommandService().fabricate(humanBaseContext.getFactory(), getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker2 = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.ATTACKER_ITEM_TYPE_ID, humanBaseContext.getAttacker1());
        humanBaseContext.setAttacker2(attacker2);
        getCommandService().fabricate(humanBaseContext.getFactory(), getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker3 = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.ATTACKER_ITEM_TYPE_ID, humanBaseContext.getAttacker1(), attacker2);
        humanBaseContext.setAttacker3(attacker3);
        getCommandService().fabricate(humanBaseContext.getFactory(), getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker4 = findSyncBaseItem(humanBaseContext.getPlayerBaseFull(), FallbackConfig.ATTACKER_ITEM_TYPE_ID, humanBaseContext.getAttacker1(), attacker2, attacker3);
        humanBaseContext.setAttacker4(attacker4);

        return humanBaseContext;
    }

    protected PlayerBaseFull setupBuilderBot(int builderCount, Polygon2D botRegion) {
        int botId = ++lastBotId;
        String botName = "setupBuilderBot id:" + botId;
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).count(builderCount).createDirectly(true).place(new PlaceConfig().polygon2D(botRegion)).noRebuild(true));
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(botId).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name(botName).npc(false));
        getBotService().startBots(botConfigs, null);
        tickPlanetServiceBaseServiceActive();
        return (PlayerBaseFull) getBotBase(botId);
    }

    protected PlayerBaseFull setupHarvesterBot(int harvesterCount, Polygon2D botRegion) {
        int botId = ++lastBotId;
        String botName = "TestTargetHarvesterBot id:" + botId;
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.HARVESTER_ITEM_TYPE_ID).count(harvesterCount).createDirectly(true).place(new PlaceConfig().polygon2D(botRegion)).noRebuild(true));
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(botId).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name(botName).npc(false));
        getBotService().startBots(botConfigs, null);
        tickPlanetServiceBaseServiceActive();
        return (PlayerBaseFull) getBotBase(botId);
    }

    protected PlayerBaseFull setupFactoryBot(int factoryCount, Polygon2D botRegion) {
        int botId = ++lastBotId;
        String botName = "setupFactoryBot id:" + botId;
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(factoryCount).createDirectly(true).place(new PlaceConfig().polygon2D(botRegion)).noRebuild(true));
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(botId).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name(botName).npc(false));
        getBotService().startBots(botConfigs, null);
        tickPlanetServiceBaseServiceActive();
        return (PlayerBaseFull) getBotBase(botId);
    }
}
