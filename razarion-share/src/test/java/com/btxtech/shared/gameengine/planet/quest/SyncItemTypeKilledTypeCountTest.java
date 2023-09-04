package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
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
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        QuestConfig questConfig = new QuestConfig().id(9001).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().setTypeCount(typeCount)));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getUserId(), questConfig);
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), null, FallbackConfig.FACTORY_ITEM_TYPE_ID, 0, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill but not in TypeCount
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botHarvester, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), null, FallbackConfig.FACTORY_ITEM_TYPE_ID, 0, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botFactory, FallbackConfig.FACTORY_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), null, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), null, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBuilder, FallbackConfig.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), null, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
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
        typeCount.put(FallbackConfig.FACTORY_ITEM_TYPE_ID, 1);
        typeCount.put(FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
        QuestConfig questConfig = new QuestConfig().id(9001).conditionConfig(new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().setTypeCount(typeCount).setBotIds(Arrays.asList(botFactory.getBotId(), botBuilder.getBotId()))));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getUserId(), questConfig);
        String expectedBotBasesInformation = "setupFactoryBot id:" + botFactory.getBotId() + ", setupBuilderBot id:" + botBuilder.getBotId();
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), expectedBotBasesInformation, FallbackConfig.FACTORY_ITEM_TYPE_ID, 0, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill but not in BotIds
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBuilderUnused, FallbackConfig.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), expectedBotBasesInformation, FallbackConfig.FACTORY_ITEM_TYPE_ID, 0, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botFactory, FallbackConfig.FACTORY_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), expectedBotBasesInformation, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        assetQuestProgressTypeCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), expectedBotBasesInformation, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 0);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBuilder, FallbackConfig.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressTypeCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), expectedBotBasesInformation, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1);
    }
}
