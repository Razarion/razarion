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
            getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
            tickPlanetServiceBaseServiceActive();
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
            System.out.println("i: " + i);
            getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
            tickPlanetServiceBaseServiceActive();
            assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        }
        // Fulfill quest
        getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }


}
