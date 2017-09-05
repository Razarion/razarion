package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
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

/**
 * Created by Beat
 * on 31.08.2017.
 */
public class TestBackupRestore extends WeldMasterBaseTest {

    @Test
    public void test() {
        setupMasterEnvironment();
        // Resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resources = getSyncItemContainerService().findResourceItemWithPlace(GameTestContent.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resources.size());
        // Bot
        SyncBaseItem botTarget = setupBot();
        // Create base 1
        UserContext userContext1 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull1 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext1);
        tickPlanetServiceBaseServiceActive();
        QuestConfig questCreate1 = GameTestContent.createItemCountCreatedQuest();
        getQuestService().activateCondition(playerBaseFull1.getHumanPlayerId(), questCreate1);
        SyncBaseItem builder1 = findSyncBaseItem(playerBaseFull1, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder1, new DecimalPosition(40, 20), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory1 = findSyncBaseItem(playerBaseFull1, GameTestContent.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory1, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker1 = findSyncBaseItem(playerBaseFull1, GameTestContent.ATTACKER_ITEM_TYPE_ID);
        getCommandService().fabricate(factory1, getBaseItemType(GameTestContent.HARVESTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem harvester1 = findSyncBaseItem(playerBaseFull1, GameTestContent.HARVESTER_ITEM_TYPE_ID);
        getCommandService().harvest(harvester1, CollectionUtils.getFirst(resources));
        // Create base 2
        UserContext userContext2 = createLevel1UserContext();
        PlayerBaseFull playerBaseFull2 = createHumanBaseWithBaseItem(new DecimalPosition(20, 40), userContext2);
        tickPlanetServiceBaseServiceActive(harvester1);
        QuestConfig questCreate2 = GameTestContent.createItemTypeCountCreatedQuest();
        getQuestService().activateCondition(playerBaseFull2.getHumanPlayerId(), questCreate2);
        SyncBaseItem builder2 = findSyncBaseItem(playerBaseFull2, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder2, new DecimalPosition(70, 40), getBaseItemType(GameTestContent.CONSUMER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive(harvester1);
        SyncBaseItem consumer2 = findSyncBaseItem(playerBaseFull2, GameTestContent.CONSUMER_ITEM_TYPE_ID);

        // Check energy
        assertEnergy(0, 0, playerBaseFull1);
        assertEnergy(60, 0, playerBaseFull2);

        // Command before backup
        getCommandService().attack(attacker1, botTarget, true);
        getCommandService().fabricate(factory1, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
        getCommandService().build(builder2, new DecimalPosition(40, 40), getBaseItemType(GameTestContent.GENERATOR_ITEM_TYPE_ID));
        Assert.assertNotNull(harvester1.getSyncHarvester().getResource());
        // ---------- Backup ---------
        BackupPlanetInfo backupPlanetInfoUnregistered = getPlanetService().backup(true);
        Assert.assertEquals(2, backupPlanetInfoUnregistered.getPlayerBaseInfos().size());
        Assert.assertEquals(6, backupPlanetInfoUnregistered.getSyncBaseItemInfos().size());
        assertQuestUnregistered(backupPlanetInfoUnregistered.getBackupComparisionInfos(), playerBaseFull1, playerBaseFull2);
        BackupPlanetInfo backupPlanetInfoRegistered = getPlanetService().backup(false);
        Assert.assertEquals(1, backupPlanetInfoRegistered.getPlayerBaseInfos().size());
        Assert.assertEquals(4, backupPlanetInfoRegistered.getSyncBaseItemInfos().size());
        Assert.assertEquals(1, backupPlanetInfoRegistered.getBackupComparisionInfos().size());
        assertQuestRegistered(backupPlanetInfoRegistered.getBackupComparisionInfos(), playerBaseFull1);
        restoreVerifyUnregistered(resources, playerBaseFull1, builder1, factory1, attacker1, harvester1, playerBaseFull2, builder2, consumer2, backupPlanetInfoUnregistered);
        restoreVerifyRegistered(resources, playerBaseFull1, builder1, factory1, attacker1, harvester1, backupPlanetInfoRegistered);
    }

    private void restoreVerifyUnregistered(Collection<SyncResourceItem> resources, PlayerBaseFull playerBaseFull1, SyncBaseItem builder1, SyncBaseItem factory1, SyncBaseItem attacker1, SyncBaseItem harvester1, PlayerBaseFull playerBaseFull2, SyncBaseItem builder2, SyncBaseItem consumer2, BackupPlanetInfo backupPlanetInfoUnregistered) {
        getBotService().killAllBots();
        getPlanetService().stop();
        setupMasterEnvironment();
        Assert.assertTrue(getBaseItemService().getPlayerBaseInfos().isEmpty());
        Assert.assertTrue(getSyncItemContainerService().getSyncBaseItemInfos().isEmpty());
        getPlanetService().restoreBases(backupPlanetInfoUnregistered);

        Assert.assertEquals(2, getBaseItemService().getPlayerBaseInfos().size());
        Assert.assertEquals(6, getSyncItemContainerService().getSyncBaseItemInfos().size());
        assertSyncItemCount(6, 0, 0);

        // Verify resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resourcesRestore = getSyncItemContainerService().findResourceItemWithPlace(GameTestContent.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resourcesRestore.size());
        assertDifferentResources(resources, resourcesRestore);
        assertSyncItemCount(6, 5, 0);

        // Verify base 1
        PlayerBaseFull playerBaseFull1Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull1.getBaseId());
        Assert.assertEquals(4, playerBaseFull1Restore.getItemCount());
        assertPlayerBaseFull(playerBaseFull1, playerBaseFull1Restore);
        SyncBaseItem builder1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder1, builder1Restore);
        SyncBaseItem factory1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.FACTORY_ITEM_TYPE_ID);
        assertSyncBaseItem(factory1, factory1Restore);
        SyncBaseItem harvester1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.HARVESTER_ITEM_TYPE_ID);
        assertSyncHarvestBaseItem(harvester1, harvester1Restore);
        SyncBaseItem attacker1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.ATTACKER_ITEM_TYPE_ID);
        assertSyncAttackerBaseItem(attacker1, attacker1Restore);
        // Verify base 2
        PlayerBaseFull playerBaseFull2Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull2.getBaseId());
        assertPlayerBaseFull(playerBaseFull2, playerBaseFull2Restore);
        Assert.assertEquals(2, playerBaseFull2Restore.getItemCount());
        SyncBaseItem builder2Restore = findSyncBaseItem(playerBaseFull2Restore, GameTestContent.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder2, builder2Restore);
        SyncBaseItem consumer2Restore = findSyncBaseItem(playerBaseFull2Restore, GameTestContent.CONSUMER_ITEM_TYPE_ID);
        assertSyncBaseItem(consumer2, consumer2Restore);

        // Check energy restore 1
        assertEnergy(0, 0, playerBaseFull1Restore);
        assertEnergy(60, 0, playerBaseFull2Restore);

        // Check if command will be finished
        tickPlanetServiceBaseServiceActive(attacker1Restore);
        assertSyncItemCount(8, 5, 0);
        // Base 1
        Assert.assertEquals(5, playerBaseFull1Restore.getItemCount());
        findSyncBaseItem(playerBaseFull1Restore, GameTestContent.ATTACKER_ITEM_TYPE_ID, attacker1Restore);
        // Base 2
        Assert.assertEquals(3, playerBaseFull2Restore.getItemCount());
        SyncBaseItem factory2Restore = findSyncBaseItem(playerBaseFull2Restore, GameTestContent.GENERATOR_ITEM_TYPE_ID);
        Assert.assertEquals(new DecimalPosition(40, 40), factory2Restore.getSyncPhysicalArea().getPosition2d());

        // Check energy restore 2
        assertEnergy(0, 0, playerBaseFull1Restore);
        assertEnergy(60, 80, playerBaseFull2Restore);

        // Create base 3
        UserContext userContext3 = createLevel1UserContext(1);
        PlayerBaseFull playerBaseFull3 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext3);
        Assert.assertEquals(5, playerBaseFull3.getBaseId());
        tickPlanetServiceBaseServiceActive(attacker1Restore);
        findSyncBaseItem(playerBaseFull3, GameTestContent.BUILDER_ITEM_TYPE_ID);
        printAllSyncItems();
        assertSyncItemCount(9, 5, 0);

        // Restore Quests
        getQuestService().clean();
        getQuestService().activateCondition(playerBaseFull1Restore.getHumanPlayerId(), GameTestContent.createItemCountCreatedQuest());
        getQuestService().restore(backupPlanetInfoUnregistered);
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(playerBaseFull1Restore.getHumanPlayerId());
        Assert.assertEquals(7, (int) questProgressInfo.getCount());
    }

    private void restoreVerifyRegistered(Collection<SyncResourceItem> resources, PlayerBaseFull playerBaseFull1, SyncBaseItem builder1, SyncBaseItem factory1, SyncBaseItem attacker1, SyncBaseItem harvester1, BackupPlanetInfo backupPlanetInfoRegistered) {
        getBotService().killAllBots();
        getPlanetService().stop();
        setupMasterEnvironment();
        Assert.assertTrue(getBaseItemService().getPlayerBaseInfos().isEmpty());
        Assert.assertTrue(getSyncItemContainerService().getSyncBaseItemInfos().isEmpty());
        getPlanetService().restoreBases(backupPlanetInfoRegistered);

        Assert.assertEquals(1, getBaseItemService().getPlayerBaseInfos().size());
        Assert.assertEquals(4, getSyncItemContainerService().getSyncBaseItemInfos().size());
        assertSyncItemCount(4, 0, 0);

        // Verify resources
        getResourceService().startResourceRegions();
        Collection<SyncResourceItem> resourcesRestore = getSyncItemContainerService().findResourceItemWithPlace(GameTestContent.RESOURCE_ITEM_TYPE_ID, new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(100, 20, 20, 20)));
        Assert.assertEquals(5, resourcesRestore.size());
        assertDifferentResources(resources, resourcesRestore);
        assertSyncItemCount(4, 5, 0);
        // Verify base 1
        PlayerBaseFull playerBaseFull1Restore = (PlayerBaseFull) getBaseItemService().getPlayerBase4BaseId(playerBaseFull1.getBaseId());
        Assert.assertEquals(4, playerBaseFull1Restore.getItemCount());
        assertPlayerBaseFull(playerBaseFull1, playerBaseFull1Restore);
        SyncBaseItem builder1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.BUILDER_ITEM_TYPE_ID);
        assertSyncBaseItem(builder1, builder1Restore);
        SyncBaseItem factory1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.FACTORY_ITEM_TYPE_ID);
        assertSyncBaseItem(factory1, factory1Restore);
        SyncBaseItem harvester1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.HARVESTER_ITEM_TYPE_ID);
        assertSyncHarvestBaseItem(harvester1, harvester1Restore);
        SyncBaseItem attacker1Restore = findSyncBaseItem(playerBaseFull1Restore, GameTestContent.ATTACKER_ITEM_TYPE_ID);
        assertSyncAttackerBaseItem(attacker1, attacker1Restore);

        // Check energy restore 1
        assertEnergy(0, 0, playerBaseFull1Restore);

        // Check if command will be finished
        tickPlanetServiceBaseServiceActive(attacker1Restore);
        // Base 1
        assertSyncItemCount(5, 5, 0);
        Assert.assertEquals(5, playerBaseFull1Restore.getItemCount());
        findSyncBaseItem(playerBaseFull1Restore, GameTestContent.ATTACKER_ITEM_TYPE_ID, attacker1Restore);

        // Check energy restore 2
        assertEnergy(0, 0, playerBaseFull1Restore);

        // Create base 3
        UserContext userContext3 = createLevel1UserContext(1);
        printAllSyncItems();
        PlayerBaseFull playerBaseFull3 = createHumanBaseWithBaseItem(new DecimalPosition(20, 20), userContext3);
        Assert.assertEquals(4, playerBaseFull3.getBaseId());
        tickPlanetServiceBaseServiceActive(attacker1Restore);
        findSyncBaseItem(playerBaseFull3, GameTestContent.BUILDER_ITEM_TYPE_ID);
        assertSyncItemCount(6, 5, 0);

        // Restore Quests
        getQuestService().clean();
        getQuestService().activateCondition(playerBaseFull1Restore.getHumanPlayerId(), GameTestContent.createItemCountCreatedQuest());
        getQuestService().restore(backupPlanetInfoRegistered);
        QuestProgressInfo questProgressInfo = getQuestService().getQuestProgressInfo(playerBaseFull1Restore.getHumanPlayerId());
        Assert.assertEquals(7, (int) questProgressInfo.getCount());
    }

    private SyncBaseItem setupBot() {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameTestContent.FACTORY_ITEM_TYPE_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(100, 70))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        getBotService().startBots(botConfigs);
        TestSimpleScheduledFuture botScheduledFuture = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT);
        for (int i = 0; i < 1000; i++) {
            botScheduledFuture.invokeRun();
            tickPlanetService();
        }
        return findSyncBaseItem(findBotBase(1), GameTestContent.FACTORY_ITEM_TYPE_ID);
    }

    private void assertDifferentResources(Collection<SyncResourceItem> resources, Collection<SyncResourceItem> resourcesRestore) {
        resources.forEach(syncResourceItem -> resourcesRestore.forEach(restore -> Assert.assertNotEquals(System.identityHashCode(syncResourceItem), System.identityHashCode(restore))));
    }

    @Override
    protected MasterPlanetConfig setupMasterPlanetConfig() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().setResourceItemTypeId(GameTestContent.RESOURCE_ITEM_TYPE_ID).setCount(5).setMinDistanceToItems(1).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(100, 20, 20, 20))));
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

    private void assertPlayerBaseFull(PlayerBaseFull expected, PlayerBaseFull actual) {
        Assert.assertNotEquals(System.identityHashCode(expected), System.identityHashCode(actual));
        ReflectionAssert.assertReflectionEquals(expected.getBackupPlayerBaseInfo(), actual.getBackupPlayerBaseInfo());
    }

    private void assertEnergy(int consumingExpected, int generatingExpected, PlayerBase playerBase) {
        EnergyServiceTest.assertEnergy(consumingExpected, generatingExpected, getEnergyService(), playerBase);
    }

    private void assertQuestUnregistered(List<BackupComparisionInfo> backupComparisionInfos, PlayerBase playerBase1, PlayerBase playerBase2) {
        Assert.assertEquals(1, backupComparisionInfos.size());
        // Player 1
        BackupComparisionInfo backupComparisionInfo1 = findBackupComparisionInfo(backupComparisionInfos, playerBase1);
        Assert.assertEquals(GameTestContent.QUEST_CONFIG_1_ID, backupComparisionInfo1.getQuestId());
        Assert.assertFalse(backupComparisionInfo1.hasRemainingMilliSeconds());
        backupComparisionInfo1.checkRemainingCount();
        Assert.assertEquals(7, (int) backupComparisionInfo1.getRemainingCount());
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
        Assert.assertFalse(backupComparisionInfo1.hasRemainingMilliSeconds());
        backupComparisionInfo1.checkRemainingCount();
        Assert.assertEquals(7, (int) backupComparisionInfo1.getRemainingCount());
        Assert.assertNull(backupComparisionInfo1.getRemainingItemTypes());
    }

    private BackupComparisionInfo findBackupComparisionInfo(List<BackupComparisionInfo> backupComparisionInfos, PlayerBase playerBase1) {
        for (BackupComparisionInfo backupComparisionInfo : backupComparisionInfos) {
            if (backupComparisionInfo.getHumanPlayerId().equals(playerBase1.getHumanPlayerId())) {
                return backupComparisionInfo;
            }
        }
        throw new IllegalArgumentException("No BackupComparisionInfo found for HumanPlayerId: " + playerBase1.getHumanPlayerId());
    }

}
