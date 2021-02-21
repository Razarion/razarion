package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
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
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getUserId(), GameTestContent.createItemTypeCountCreatedQuest2());
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create factory not passed
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create two attacker not passed
        Assert.fail("... FIX ME: below will block ...");
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 80), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create harvester passed
        fabricateAndMove(factory, FallbackConfig.HARVESTER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestPassed(playerBaseFull.getUserId());
    }
}
