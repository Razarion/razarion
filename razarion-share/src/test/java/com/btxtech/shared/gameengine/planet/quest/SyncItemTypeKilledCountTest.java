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

import java.util.Collections;

/**
 * Created by Beat
 * on 08.12.2017.
 */
public class SyncItemTypeKilledCountTest extends AbstractQuestServiceTest {

    @Test
    public void killCount() {
        setup();
        AbstractUpdatingComparison.MIN_SEND_DELAY = 0;
        // Setup bases
        PlayerBaseFull botBase1 = setupHarvesterBot(1, Polygon2D.fromRectangle(170, 70, 10, 10));
        PlayerBaseFull botBase2 = setupHarvesterBot(1, Polygon2D.fromRectangle(240, 80, 10, 10));
        PlayerBaseFull botBase3 = setupHarvesterBot(1, Polygon2D.fromRectangle(270, 180, 10, 10));
        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        // Create and activate quest
        QuestConfig questConfig = new QuestConfig().setId(9001).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(3)));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getUserId(), questConfig);
        // showDisplay();

        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), 0, null);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase1, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), 1, null);
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), 1, null);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase2, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), 2, null);
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), 2, null);
        // Kill third
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase3, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), 3, null);
    }

    @Test
    public void killCountBotId() {
        setup();
        AbstractUpdatingComparison.MIN_SEND_DELAY = 0;
        // Setup bases
        PlayerBaseFull botBase1 = setupHarvesterBot(1, Polygon2D.fromRectangle(170, 70, 10, 10));
        PlayerBaseFull botBase2 = setupHarvesterBot(1, Polygon2D.fromRectangle(240, 80, 10, 10));
        PlayerBaseFull botBase3 = setupHarvesterBot(1, Polygon2D.fromRectangle(270, 180, 10, 10));
        HumanBaseContext humanBaseContext = createHumanBaseBFA();
        // Create and activate quest
        QuestConfig questConfig = new QuestConfig().setId(9002).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1).setBotIds(Collections.singletonList(botBase3.getBotId()))));
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getUserId(), questConfig);
        String expectedBotBasesInformation = "TestTargetHarvesterBot id:" + botBase3.getBotId();
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase1, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), 0, expectedBotBasesInformation);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase2, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getUserId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getUserId(), 0, expectedBotBasesInformation);
        // Kill third
        getCommandService().attack(humanBaseContext.getAttacker1(), findSyncBaseItem(botBase3, FallbackConfig.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getUserId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getUserId(), 1, expectedBotBasesInformation);

    }
}
