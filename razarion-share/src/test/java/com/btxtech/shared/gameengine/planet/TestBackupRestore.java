package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyServiceTest;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.BackupComparisionInfo;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 31.08.2017.
 */
public class TestBackupRestore extends WeldMasterBaseTest {
    @Test
    public void test() {
        setupMasterEnvironment();
        TestBaseRestoreProvider testBaseRestoreProvider = new TestBaseRestoreProvider();
        // Resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resources = getSyncItemContainerService().findResourceItemWithPlace(FallbackConfig.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().polygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resources.size());
        // Box
        Collection<BoxRegionConfig> boxRegionConfigs = new ArrayList<>();
        boxRegionConfigs.add(new BoxRegionConfig().boxItemTypeId(FallbackConfig.BOX_ITEM_TYPE_LONG_ID).minInterval(1).maxInterval(1).count(1).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(200, 20, 20, 20))));
        getBoxService().startBoxRegions(boxRegionConfigs);
        // Bot
        SyncBaseItem botTarget = setupBot();
        // Create base 1
        UserContext userContext1 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull1 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext1);
        testBaseRestoreProvider.addUserContext(userContext1);
        tickPlanetServiceBaseServiceActive();
        QuestConfig questCreate1 = GameTestContent.createItemCountCreatedQuest10();
        getQuestService().activateCondition(playerBaseFull1.getUserId(), questCreate1);
        SyncBaseItem builder1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder1, new DecimalPosition(40, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory1, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.ATTACKER_ITEM_TYPE_ID);
        getCommandService().fabricate(factory1, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem boxPicker1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker1);
        getCommandService().fabricate(factory1, getBaseItemType(FallbackConfig.HARVESTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harvester1 = findSyncBaseItem(playerBaseFull1, FallbackConfig.HARVESTER_ITEM_TYPE_ID);
        getCommandService().harvest(harvester1, CollectionUtils.getFirst(resources));
        // Create base 2
        UserContext userContext2 = createLevel1UserContext();
        testBaseRestoreProvider.addUserContext(userContext2);
        PlayerBaseFull playerBaseFull2 = createHumanBaseWithBaseItem(new DecimalPosition(20, 40), userContext2);
        tickPlanetServiceBaseServiceActive(harvester1);
        QuestConfig questCreate2 = GameTestContent.createItemTypeCountCreatedQuest();
        getQuestService().activateCondition(playerBaseFull2.getUserId(), questCreate2);
        SyncBaseItem builder2 = findSyncBaseItem(playerBaseFull2, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder2, new DecimalPosition(70, 40), getBaseItemType(FallbackConfig.CONSUMER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive(harvester1);
        SyncBaseItem consumer2 = findSyncBaseItem(playerBaseFull2, FallbackConfig.CONSUMER_ITEM_TYPE_ID);

        // Check energy
        assertEnergy(0, 0, playerBaseFull1);
        assertEnergy(60, 0, playerBaseFull2);

        // Command before backup
        getCommandService().attack(attacker1, botTarget, true);
        getCommandService().pickupBox(boxPicker1, findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_LONG_ID));
        getCommandService().fabricate(factory1, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        getCommandService().build(builder2, new DecimalPosition(40, 40), getBaseItemType(FallbackConfig.GENERATOR_ITEM_TYPE_ID));
        Assert.assertNotNull(harvester1.getSyncHarvester().getResource());
        // ---------- Backup ---------
        BackupPlanetInfo backupPlanetInfoUnregistered = getPlanetService().backup();
        Assert.assertEquals(2, backupPlanetInfoUnregistered.getPlayerBaseInfos().size());
        Assert.assertEquals(7, backupPlanetInfoUnregistered.getSyncBaseItemInfos().size());
        assertQuestUnregistered(backupPlanetInfoUnregistered.getBackupComparisionInfos(), playerBaseFull1, playerBaseFull2);
        BackupPlanetInfo backupPlanetInfoRegistered = getPlanetService().backup();
        Assert.assertEquals(1, backupPlanetInfoRegistered.getPlayerBaseInfos().size());
        Assert.assertEquals(5, backupPlanetInfoRegistered.getSyncBaseItemInfos().size());
        Assert.assertEquals(1, backupPlanetInfoRegistered.getBackupComparisionInfos().size());
        assertQuestRegistered(backupPlanetInfoRegistered.getBackupComparisionInfos(), playerBaseFull1);
        restoreVerifyUnregistered(resources, playerBaseFull1, builder1, factory1, attacker1, boxPicker1, harvester1, playerBaseFull2, builder2, consumer2, backupPlanetInfoUnregistered, testBaseRestoreProvider);
        restoreVerifyRegistered(resources, playerBaseFull1, builder1, factory1, attacker1, boxPicker1, harvester1, backupPlanetInfoRegistered, testBaseRestoreProvider);
    }

    private void restoreVerifyUnregistered(Collection<SyncResourceItem> resources, PlayerBaseFull playerBaseFull1, SyncBaseItem builder1, SyncBaseItem factory1, SyncBaseItem attacker1, SyncBaseItem boxPicker1, SyncBaseItem harvester1, PlayerBaseFull playerBaseFull2, SyncBaseItem builder2, SyncBaseItem consumer2, BackupPlanetInfo backupPlanetInfoUnregistered, BaseRestoreProvider baseRestoreProvider) {
        getBotService().killAllBots();
        getPlanetService().stop();
        setupMasterEnvironment();
        Assert.assertTrue(getBaseItemService().getPlayerBaseInfos().isEmpty());
        Assert.assertTrue(getSyncItemContainerService().getSyncBaseItemInfos().isEmpty());
        getPlanetService().restoreBases(backupPlanetInfoUnregistered, baseRestoreProvider);

        Assert.assertEquals(2, getBaseItemService().getPlayerBaseInfos().size());
        Assert.assertEquals(7, getSyncItemContainerService().getSyncBaseItemInfos().size());
        assertSyncItemCount(7, 0, 0);

        // Verify resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resourcesRestore = getSyncItemContainerService().findResourceItemWithPlace(FallbackConfig.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().polygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resourcesRestore.size());
        assertDifferentResources(resources, resourcesRestore);
        assertSyncItemCount(7, 5, 0);

        // Verify base 1
        PlayerBaseFull playerBaseFull1Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull1.getBaseId());
        Assert.assertEquals(5, playerBaseFull1Restore.getItemCount());
        assertPlayerBaseFull(playerBaseFull1, playerBaseFull1Restore);
        SyncBaseItem builder1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder1, builder1Restore);
        SyncBaseItem factory1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertSyncBaseItem(factory1, factory1Restore);
        SyncBaseItem harvester1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.HARVESTER_ITEM_TYPE_ID);
        assertSyncHarvestBaseItem(harvester1, harvester1Restore);
        SyncBaseItem attacker1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, boxPicker1);
        assertSyncAttackerBaseItem(attacker1, attacker1Restore);
        SyncBaseItem boxPickerRestore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker1);
        assertSyncBoxPickBaseItem(boxPicker1, boxPickerRestore);
        // Verify base 2
        PlayerBaseFull playerBaseFull2Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull2.getBaseId());
        assertPlayerBaseFull(playerBaseFull2, playerBaseFull2Restore);
        Assert.assertEquals(2, playerBaseFull2Restore.getItemCount());
        SyncBaseItem builder2Restore = findSyncBaseItem(playerBaseFull2Restore, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder2, builder2Restore);
        SyncBaseItem consumer2Restore = findSyncBaseItem(playerBaseFull2Restore, FallbackConfig.CONSUMER_ITEM_TYPE_ID);
        assertSyncBaseItem(consumer2, consumer2Restore);

        // Check energy restore 1
        assertEnergy(0, 0, playerBaseFull1Restore);
        assertEnergy(60, 0, playerBaseFull2Restore);

        // Check if command will be finished
        tickPlanetServiceBaseServiceActive(attacker1Restore, boxPickerRestore);
        assertSyncItemCount(9, 5, 0);
        // Base 1
        Assert.assertEquals(6, playerBaseFull1Restore.getItemCount());
        findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker1Restore, boxPickerRestore);
        // Base 2
        Assert.assertEquals(3, playerBaseFull2Restore.getItemCount());
        SyncBaseItem factory2Restore = findSyncBaseItem(playerBaseFull2Restore, FallbackConfig.GENERATOR_ITEM_TYPE_ID);
        Assert.assertEquals(new DecimalPosition(40, 40), factory2Restore.getSyncPhysicalArea().getPosition2d());

        // Check energy restore 2
        assertEnergy(0, 0, playerBaseFull1Restore);
        assertEnergy(60, 80, playerBaseFull2Restore);

        // Create base 3
        UserContext userContext3 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull3 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext3);
        Assert.assertEquals(5, playerBaseFull3.getBaseId());
        tickPlanetServiceBaseServiceActive(attacker1Restore, boxPickerRestore);
        findSyncBaseItem(playerBaseFull3, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        printAllSyncItems();
        assertSyncItemCount(10, 5, 0);

        // Restore Quests
        getQuestService().clean();
        getQuestService().activateCondition(playerBaseFull1Restore.getUserId(), GameTestContent.createItemCountCreatedQuest10());
        getQuestService().restore(backupPlanetInfoUnregistered);
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(playerBaseFull1Restore.getUserId());
        Assert.assertEquals(4, (int) questProgressInfo.getCount());
    }

    private void restoreVerifyRegistered(Collection<SyncResourceItem> resources, PlayerBaseFull playerBaseFull1, SyncBaseItem builder1, SyncBaseItem factory1, SyncBaseItem attacker1, SyncBaseItem boxPicker1, SyncBaseItem harvester1, BackupPlanetInfo backupPlanetInfoRegistered, BaseRestoreProvider baseRestoreProvider) {
        getBotService().killAllBots();
        getPlanetService().stop();
        setupMasterEnvironment();
        Assert.assertTrue(getBaseItemService().getPlayerBaseInfos().isEmpty());
        Assert.assertTrue(getSyncItemContainerService().getSyncBaseItemInfos().isEmpty());
        getPlanetService().restoreBases(backupPlanetInfoRegistered, baseRestoreProvider);

        Assert.assertEquals(1, getBaseItemService().getPlayerBaseInfos().size());
        Assert.assertEquals(5, getSyncItemContainerService().getSyncBaseItemInfos().size());
        assertSyncItemCount(5, 0, 0);

        // Verify resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resourcesRestore = getSyncItemContainerService().findResourceItemWithPlace(FallbackConfig.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().polygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resourcesRestore.size());
        assertDifferentResources(resources, resourcesRestore);
        assertSyncItemCount(5, 5, 0);
        // Verify base 1
        PlayerBaseFull playerBaseFull1Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull1.getBaseId());
        Assert.assertEquals(5, playerBaseFull1Restore.getItemCount());
        assertPlayerBaseFull(playerBaseFull1, playerBaseFull1Restore);
        SyncBaseItem builder1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder1, builder1Restore);
        SyncBaseItem factory1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        assertSyncBaseItem(factory1, factory1Restore);
        SyncBaseItem harvester1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.HARVESTER_ITEM_TYPE_ID);
        assertSyncHarvestBaseItem(harvester1, harvester1Restore);
        SyncBaseItem attacker1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, boxPicker1);
        assertSyncAttackerBaseItem(attacker1, attacker1Restore);
        SyncBaseItem boxPicker1Restore = findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker1);
        assertSyncBoxPickBaseItem(boxPicker1, boxPicker1Restore);

        // Check energy restore 1
        assertEnergy(0, 0, playerBaseFull1Restore);

        // Check if command will be finished
        tickPlanetServiceBaseServiceActive(attacker1Restore, boxPicker1Restore);
        // Base 1
        assertSyncItemCount(6, 5, 0);
        Assert.assertEquals(6, playerBaseFull1Restore.getItemCount());
        findSyncBaseItem(playerBaseFull1Restore, FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker1Restore, boxPicker1Restore);

        // Check energy restore 2
        assertEnergy(0, 0, playerBaseFull1Restore);

        // Create base 3
        UserContext userContext3 = createLevel1UserContext(1);
        printAllSyncItems();
        PlayerBaseFull playerBaseFull3 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext3);
        Assert.assertEquals(4, playerBaseFull3.getBaseId());
        tickPlanetServiceBaseServiceActive(attacker1Restore, boxPicker1Restore);
        findSyncBaseItem(playerBaseFull3, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        assertSyncItemCount(7, 5, 0);

        // Restore Quests
        getQuestService().clean();
        getQuestService().activateCondition(playerBaseFull1Restore.getUserId(), GameTestContent.createItemCountCreatedQuest10());
        getQuestService().restore(backupPlanetInfoRegistered);
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(playerBaseFull1Restore.getUserId());
        Assert.assertEquals(4, (int) questProgressInfo.getCount());
    }

    private SyncBaseItem setupBot() {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(100, 70))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        getBotService().startBots(botConfigs, null);
        TestSimpleScheduledFuture botScheduledFuture = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT_TICKER);
        for (int i = 0; i < 1000; i++) {
            botScheduledFuture.invokeRun();
            tickPlanetService();
        }
        return findSyncBaseItem(findBotBase(1), FallbackConfig.FACTORY_ITEM_TYPE_ID);
    }

    private void assertDifferentResources(Collection<SyncResourceItem> resources, Collection<SyncResourceItem> resourcesRestore) {
        resources.forEach(syncResourceItem -> resourcesRestore.forEach(restore -> Assert.assertNotEquals(System.identityHashCode(syncResourceItem), System.identityHashCode(restore))));
    }

    @Override
    protected MasterPlanetConfig setupMasterPlanetConfig() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().resourceItemTypeId(FallbackConfig.RESOURCE_ITEM_TYPE_ID).count(5).minDistanceToItems(1).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(100, 20, 20, 20))));
        return super.setupMasterPlanetConfig().setResourceRegionConfigs(resourceRegionConfigs);
    }

    private void assertSyncBaseItem(SyncBaseItem expected, SyncBaseItem actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        ReflectionAssert.assertReflectionEquals(expected.getSyncInfo(), actual.getSyncInfo());
    }

    private void assertSyncHarvestBaseItem(SyncBaseItem expected, SyncBaseItem actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        SyncBaseItemInfo syncBaseItemInfo = expected.getSyncInfo();
        syncBaseItemInfo.setTarget(null);
        ReflectionAssert.assertReflectionEquals(syncBaseItemInfo, actual.getSyncInfo());
    }

    private void assertSyncAttackerBaseItem(SyncBaseItem expected, SyncBaseItem actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        SyncBaseItemInfo syncBaseItemInfo = expected.getSyncInfo();
        syncBaseItemInfo.setTarget(null);
        ReflectionAssert.assertReflectionEquals(syncBaseItemInfo, actual.getSyncInfo());
    }

    private void assertSyncBoxPickBaseItem(SyncBaseItem expected, SyncBaseItem actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        SyncBaseItemInfo syncBaseItemInfo = expected.getSyncInfo();
        syncBaseItemInfo.setSyncBoxItemId(null);
        ReflectionAssert.assertReflectionEquals(syncBaseItemInfo, actual.getSyncInfo());
    }

    private void assertPlayerBaseFull(PlayerBaseFull expected, PlayerBaseFull actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        ReflectionAssert.assertReflectionEquals(expected.getPlayerBaseInfo(), actual.getPlayerBaseInfo());
        Assert.assertEquals(expected.getLevelId(), actual.getLevelId());
        Assert.assertEquals(expected.getHouseSpace(), actual.getHouseSpace());
        Assert.assertEquals(expected.getItemCount(), actual.getItemCount());
        Collection<Integer> expectedIds = expected.getItems().stream().map(SyncItem::getId).collect(Collectors.toList());
        for (SyncBaseItem actualId : actual.getItems()) {
            Assert.assertTrue("Item not expected: " + actual, expectedIds.remove(actualId.getId()));
        }
        if (!expectedIds.isEmpty()) {
            Assert.fail("Some items ar missing: " + expectedIds);
        }
        ReflectionAssert.assertReflectionEquals(expected.getUnlockedItemLimit(), actual.getUnlockedItemLimit());
    }

    private void assertEnergy(int consumingExpected, int generatingExpected, PlayerBase playerBase) {
        EnergyServiceTest.assertEnergy(consumingExpected, generatingExpected, getEnergyService(), playerBase);
    }

    private void assertQuestUnregistered(List<BackupComparisionInfo> backupComparisionInfos, PlayerBase playerBase1, PlayerBase playerBase2) {
        Assert.assertEquals(1, backupComparisionInfos.size());
        // Player 1
        BackupComparisionInfo backupComparisionInfo1 = findBackupComparisionInfo(backupComparisionInfos, playerBase1);
        Assert.assertEquals(GameTestContent.QUEST_CONFIG_1_ID, backupComparisionInfo1.getQuestId());
        Assert.assertFalse(backupComparisionInfo1.hasPassedSeconds());
        backupComparisionInfo1.checkRemainingCount();
        Assert.assertEquals(6, (int) backupComparisionInfo1.getRemainingCount());
        Assert.assertNull(backupComparisionInfo1.getRemainingItemTypes());
        // Player 2
        try {
            findBackupComparisionInfo(backupComparisionInfos, playerBase2);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Ignore
        }
    }

    private void assertQuestRegistered(List<BackupComparisionInfo> backupComparisionInfos, PlayerBase playerBase1) {
        Assert.assertEquals(1, backupComparisionInfos.size());
        // Player 1
        BackupComparisionInfo backupComparisionInfo1 = findBackupComparisionInfo(backupComparisionInfos, playerBase1);
        Assert.assertEquals(GameTestContent.QUEST_CONFIG_1_ID, backupComparisionInfo1.getQuestId());
        Assert.assertFalse(backupComparisionInfo1.hasPassedSeconds());
        backupComparisionInfo1.checkRemainingCount();
        Assert.assertEquals(6, (int) backupComparisionInfo1.getRemainingCount());
        Assert.assertNull(backupComparisionInfo1.getRemainingItemTypes());
    }

    private BackupComparisionInfo findBackupComparisionInfo(List<BackupComparisionInfo> backupComparisionInfos, PlayerBase playerBase1) {
        for (BackupComparisionInfo backupComparisionInfo : backupComparisionInfos) {
            if (backupComparisionInfo.getUserId() == playerBase1.getUserId()) {
                return backupComparisionInfo;
            }
        }
        throw new IllegalArgumentException("No BackupComparisionInfo found for HumanPlayerId: " + playerBase1.getUserId());
    }

}
