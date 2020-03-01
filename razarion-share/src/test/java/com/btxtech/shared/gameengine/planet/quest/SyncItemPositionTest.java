package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.TestBaseRestoreProvider;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Beat
 * on 12.10.2017.
 */
public class SyncItemPositionTest extends AbstractQuestServiceTest {

    @Test
    public void noPosition() {
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
        // Create 3 attacker
        for (int i = 0; i < 3; i++) {
            fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40 + 10 * i, 152), playerBaseFull);
        }
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createNoPositionQuest());
        getQuestService().tick();
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 3);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 3);
        // Verify tick does not trigger unfulfilled quest
        for (int i = 0; i < 100; i++) {
            getQuestService().tick();
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Create 3 attacker
        for (int i = 0; i < 3; i++) {
            fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40 + 10 * i, 168), playerBaseFull);
            assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 4 + i);
            assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 4 + i);
            assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        }
        // Remove one builder
        getBaseItemService().sellItems(Collections.singletonList(findSyncBaseItemHighestId(playerBaseFull, FallbackConfig.ATTACKER_ITEM_TYPE_ID).getId()), playerBaseFull);
        getQuestService().tick();
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 5);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.BUILDER_ITEM_TYPE_ID, 1, FallbackConfig.FACTORY_ITEM_TYPE_ID, 1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 5);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Build last and pass test
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 184), playerBaseFull);
        fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 204), playerBaseFull);
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
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createPositionQuest());
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Create 3 attacker
        SyncBaseItem attacker1 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 160), playerBaseFull);
        SyncBaseItem attacker2 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(60, 160), playerBaseFull);
        SyncBaseItem attacker3 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(80, 160), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move first to position not passed
        getCommandService().move(attacker1, new DecimalPosition(150, 150));
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move second to position not passed
        getCommandService().move(attacker2, new DecimalPosition(170, 150));
        tickPlanetServiceBaseServiceActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move third to position passed
        getCommandService().move(attacker3, new DecimalPosition(190, 150));
        tickPlanetServiceBaseServiceActive();
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }

    @Test
    public void positionTime() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext();
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createPositionTimeQuest());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 0);
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Create 3 attacker
        SyncBaseItem attacker1 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 160), playerBaseFull);
        SyncBaseItem attacker2 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(60, 160), playerBaseFull);
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        // Move first to position not passed
        getCommandService().move(attacker1, new DecimalPosition(180, 150));
        tickPlanetServicePathingActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 1);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 1);
        // Move second to position not passed due to time
        getCommandService().move(attacker2, new DecimalPosition(140, 150));
        tickPlanetServicePathingActive();
        // Verify time
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 57, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 60, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        for (int i = 0; i < 300; i++) {
            getQuestService().tick();
            if (i != 0 && i % 50 == 0) {
                assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 55 - (i / PlanetService.TICKS_PER_SECONDS), 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
            }
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        // Move out
        getCommandService().move(attacker2, new DecimalPosition(120, 120));
        tickPlanetServicePathingActive();
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 1);
        // assertQuestProgressPositionGameLogicListenerFirst(playerBaseFull.getHumanPlayerId(), 25, 10, GameTestContent.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListenerFirst(playerBaseFull.getHumanPlayerId(), 20, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListenerFirst(playerBaseFull.getHumanPlayerId(), null, null, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 1);
        // Move second to position not passed due to time
        getCommandService().move(attacker2, new DecimalPosition(140, 150));
        tickPlanetServicePathingActive();
        // Verify time
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 50, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 60, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        for (int i = 0; i < 300; i++) {
            getQuestService().tick();
            if (i != 0 && i % 50 == 0) {
                assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 55 - (i / PlanetService.TICKS_PER_SECONDS), 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
            }
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        for (int i = 0; i < 300; i++) {
            getQuestService().tick();
            if (i != 0 && i % 50 == 0) {
                assertQuestProgressPositionGameLogicListener(playerBaseFull.getHumanPlayerId(), 30 - (i / PlanetService.TICKS_PER_SECONDS), 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
            }
        }
        assertQuestProgressPositionGameLogicListenerNone(playerBaseFull.getHumanPlayerId());
        assertQuestPassed(playerBaseFull.getHumanPlayerId());
    }

    @Test
    public void positionTimeBackupRestore() {
        setup();
        // Create user
        UserContext userContext = createLevel1UserContext(1);
        // Create base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Start quest
        getQuestService().addQuestListener(createQuestListener());
        getQuestService().activateCondition(playerBaseFull.getHumanPlayerId(), GameTestContent.createPositionTimeQuest());
        // Create factory
        getCommandService().build(builder.getId(), new DecimalPosition(20, 40), FallbackConfig.FACTORY_ITEM_TYPE_ID);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Create 3 attacker
        SyncBaseItem attacker1 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(40, 160), playerBaseFull);
        SyncBaseItem attacker2 = fabricateAndMove(factory, FallbackConfig.ATTACKER_ITEM_TYPE_ID, new DecimalPosition(60, 160), playerBaseFull);
        // Move first to position not passed
        getCommandService().move(attacker1, new DecimalPosition(180, 150));
        tickPlanetServiceBaseServiceActive();
        // Move second to position not passed due to time
        getCommandService().move(attacker2, new DecimalPosition(140, 150));
        tickPlanetServiceBaseServiceActive();
        for (int i = 0; i < 300; i++) {
            getQuestService().tick();
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        // Backup
        BackupPlanetInfo backupPlanetInfo = getPlanetService().backup(false);
        for (int i = 0; i < 150; i++) {
            getQuestService().tick();
        }
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 15, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
        // Restore
        TestBaseRestoreProvider testBaseRestoreProvider = new TestBaseRestoreProvider();
        testBaseRestoreProvider.addUserContext(userContext);
        getPlanetService().restoreBases(backupPlanetInfo, testBaseRestoreProvider);
        getQuestService().clean();
        getQuestService().activateCondition(userContext.getHumanPlayerId(), GameTestContent.createPositionTimeQuest());
        getQuestService().restore(backupPlanetInfo);
        getQuestService().tick();
        // Verify
        assertQuestNotPassed(playerBaseFull.getHumanPlayerId());
        assertQuestProgressPositionDownload(playerBaseFull.getHumanPlayerId(), 30, 10, FallbackConfig.ATTACKER_ITEM_TYPE_ID, 2);
    }

}
