package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
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
    public void createdCount() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getUserId(), GameTestContent.createItemCountCreatedQuest3());
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create factory not passed
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create two attacker not passed
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getUserId());
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 80), playerBaseFull);
        assertQuestPassed(playerBaseFull.getUserId());
    }

    @Test
    public void createdCountIncludeExisting() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getUserId(), GameTestContent.createItemCountCreatedQuest3IncludeExisting());
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create two attacker not passed
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestPassed(playerBaseFull.getUserId());
    }

    @Test
    public void createdItemTypeCount() {
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
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 80), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create harvester passed
        fabricateAndMove(factory, FallbackConfig.HARVESTER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestPassed(playerBaseFull.getUserId());
    }

    @Test
    public void createdItemTypeCountIncludeExisting() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Create two attacker
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID ,new DecimalPosition(20, 80), playerBaseFull);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getUserId(), GameTestContent.createItemTypeCountCreatedQuest2IncludeExisting());
        assertQuestNotPassed(playerBaseFull.getUserId());
        // Create harvester passed
        fabricateAndMove(factory, FallbackConfig.HARVESTER_ITEM_TYPE_ID ,new DecimalPosition(20, 60), playerBaseFull);
        assertQuestPassed(playerBaseFull.getUserId());
    }
}
