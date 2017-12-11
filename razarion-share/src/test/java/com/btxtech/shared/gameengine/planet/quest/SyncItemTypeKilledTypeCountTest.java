package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.basic.HumanBaseContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 09.12.2017.
 */
public class SyncItemTypeKilledTypeCountTest extends AbstractQuestServiceTest {

    @Test
    public void killTypeCount() {
        setup();
        AbstractUpdatingComparison.MIN_SEND_DELAY = 0;
        // Setup bases
        PlayerBaseFull botHarvester = setupHarvesterBot(1, Polygon2D.fromRectangle(170, 70, 10, 10));
        PlayerBaseFull botFactory = setupFactoryBot(1, Polygon2D.fromRectangle(240, 80, 10, 10));
        PlayerBaseFull botBuilder = setupBuilderBot(1, Polygon2D.fromRectangle(270, 180, 10, 10));
        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        // Create and activate quest
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(GameTestContent.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(GameTestContent.BUILDER_ITEM_TYPE_ID, 1);
        QuestConfig questConfig = new QuestConfig().setId(9001).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount)));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), questConfig);
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), null, GameTestContent.FACTORY_ITEM_TYPE_ID, 0, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill but not in TypeCount
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botHarvester, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), null, GameTestContent.FACTORY_ITEM_TYPE_ID, 0, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botFactory, GameTestContent.FACTORY_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), null, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), null, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBuilder, GameTestContent.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), null, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 1);
    }

    @Test
    public void killTypeCountBotId() {
        setup();
        AbstractUpdatingComparison.MIN_SEND_DELAY = 0;
        // Setup bases
        PlayerBaseFull botBuilderUnused = setupBuilderBot(1, Polygon2D.fromRectangle(170, 70, 10, 10));
        PlayerBaseFull botFactory = setupFactoryBot(1, Polygon2D.fromRectangle(240, 80, 10, 10));
        PlayerBaseFull botBuilder = setupBuilderBot(1, Polygon2D.fromRectangle(270, 180, 10, 10));
        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        // Create and activate quest
        Map<Integer, Integer> typeCount = new HashMap<>();
        typeCount.put(GameTestContent.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(GameTestContent.BUILDER_ITEM_TYPE_ID, 1);
        QuestConfig questConfig = new QuestConfig().setId(9001).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount).setBotIds(Arrays.asList(botFactory.getBotId(), botBuilder.getBotId()))));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), questConfig);
        String expectedBotBasesInformation = "setupFactoryBot id:" + botFactory.getBotId() + ", setupBuilderBot id:" + botBuilder.getBotId();
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), expectedBotBasesInformation, GameTestContent.FACTORY_ITEM_TYPE_ID, 0, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill but not in BotIds
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBuilderUnused, GameTestContent.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), expectedBotBasesInformation, GameTestContent.FACTORY_ITEM_TYPE_ID, 0, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botFactory, GameTestContent.FACTORY_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), expectedBotBasesInformation, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), expectedBotBasesInformation, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 0);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBuilder, GameTestContent.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), expectedBotBasesInformation, GameTestContent.FACTORY_ITEM_TYPE_ID, 1, GameTestContent.BUILDER_ITEM_TYPE_ID, 1);
    }
}
