package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

/**
 * Created by Beat
 * on 12.10.2017.
 */
public class SyncItemPositionTest extends AbstractQuestServiceTest {

    @Test
    public void noPositionAddExisting() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), GameTestContent.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        // Create 3 attacker
        for (int i = 0; i < 3; i++) {
            fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40 + 10 * i, 40), playerBaseFull);
        }
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createNoPositionAddExistingQuest());
        // Verify tick does not trigger unfulfilled quest
        for (int i = 0; i < 100; i++) {
            getQuestService().tick();
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Create 3 attacker
        for (int i = 0; i < 3; i++) {
            fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40 + 10 * i, 60), playerBaseFull);
            assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        }
        fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 80), playerBaseFull);
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }

    @Test
    public void position() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createPositionAddExistingQuest());
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), GameTestContent.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        // Create 3 attacker
        SyncBaseItem attacker1 = fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 40), playerBaseFull);
        SyncBaseItem attacker2 = fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(60, 40), playerBaseFull);
        SyncBaseItem attacker3 = fabricateAndMove(factory, GameTestContent.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(80, 40), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move first to position not passed
        getCommandService().move(attacker1, new DecimalPosition(110, 90));
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move second to position not passed
        getCommandService().move(attacker2, new DecimalPosition(110, 110));
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move third to position passed
        getCommandService().move(attacker3, new DecimalPosition(110, 110));
        tickPlanetServiceBaseServiceActive();
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }

}
