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
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), questConfig);
        // showDisplay();

        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 0);
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase1, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 1);
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 1);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase2, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 2);
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 2);
        // Kill third
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase3, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 3);
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
        getQuestService().activateCondition(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), questConfig);
        // showDisplay();
        // Verify
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        // Kill first
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase1, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 0);
        // Kill second
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase2, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestNotPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        Assert.assertNull(getTestGameLogicListener().getQuestProgresses().get(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()));
        assetQuestProgressCountDownload(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 0);
        // Kill third
        getCommandService().attack(humanBaseContext.getAttacker(), findSyncBaseItem(botBase3, GameTestContent.HARVESTER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        // Verify
        assertQuestPassed(humanBaseContext.getPlayerBaseFull().getHumanPlayerId());
        assetQuestProgressCountGameLogicListener(humanBaseContext.getPlayerBaseFull().getHumanPlayerId(), 1);

    }
}
