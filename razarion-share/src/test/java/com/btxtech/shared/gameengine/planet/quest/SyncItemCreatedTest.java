package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

/**
 * Created by Beat
 * on 13.10.2017.
 */
public class SyncItemCreatedTest extends AbstractQuestServiceTest {
    @Test
    public void created() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createItemTypeCountCreatedQuest2());
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Create factory not passed
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), GameTestContent.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Create two attacker not passed
        fabricateAndMove(factory,GameTestContent.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        fabricateAndMove(factory,GameTestContent.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 80), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Create harvester passed
        fabricateAndMove(factory,GameTestContent.HARVESTER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }
}
