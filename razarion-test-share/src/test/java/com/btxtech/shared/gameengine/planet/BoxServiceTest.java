package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.mock.TestSimpleScheduledFuture;
import com.btxtech.shared.system.SimpleExecutorService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 15.09.2017.
 */
public class BoxServiceTest extends DaggerMasterBaseTest {

    @Test
    public void testBoxRegions() {
        setupMasterEnvironment();
        DaggerSlaveEmulator permanentSalve = new DaggerSlaveEmulator();
        UserContext userContext = createLevel1UserContext();
        permanentSalve.connectToMaster(userContext, this);
        assertSyncItemCount(permanentSalve, 0);
        // Start box service
        BoxService boxService = getBoxService();
        Collection<BoxRegionConfig> boxRegionConfigs = new ArrayList<>();
        boxRegionConfigs.add(new BoxRegionConfig().boxItemTypeId(FallbackConfig.BOX_ITEM_TYPE_ID).minInterval(100).maxInterval(100).count(1).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(40, 50, 200, 150))));
        boxService.startBoxRegions(boxRegionConfigs);
        // Span box 1
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 0);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 1);
        SyncBoxItem box1 = findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_ID);
        Assert.assertTrue(box1.isAlive());
        // Span box 2
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 1);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 2);
        SyncBoxItem box2 = findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_ID, box1);
        Assert.assertTrue(box1.isAlive());
        Assert.assertTrue(box2.isAlive());
        // Span box 3
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 2);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 3);
        SyncBoxItem box3 = findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_ID, box1, box2);
        Assert.assertTrue(box1.isAlive());
        Assert.assertTrue(box2.isAlive());
        Assert.assertTrue(box3.isAlive());
        // Expire first box
        tickBoxService(400);
        assertSyncItemCount(permanentSalve, 3);
        tickBoxService(200);
        assertSyncItemCount(permanentSalve, 2);
        Assert.assertFalse(box1.isAlive());
        Assert.assertTrue(box2.isAlive());
        Assert.assertTrue(box3.isAlive());
        // Span box 4
        tickBoxService(400);
        assertSyncItemCount(permanentSalve, 3);
        SyncBoxItem box4 = findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_ID, box2, box3);
        Assert.assertFalse(box1.isAlive());
        Assert.assertTrue(box2.isAlive());
        Assert.assertTrue(box3.isAlive());
        Assert.assertTrue(box4.isAlive());
    }

    @Test
    public void testBoxPicked() {
        setupMasterEnvironment();
        // Start box service
        BoxService boxService = getBoxService();
        Collection<BoxRegionConfig> boxRegionConfigs = new ArrayList<>();
        boxRegionConfigs.add(new BoxRegionConfig().boxItemTypeId(FallbackConfig.BOX_ITEM_TYPE_LONG_ID).minInterval(10000).maxInterval(10000).count(1).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 1, 1))));
        boxService.startBoxRegions(boxRegionConfigs);
        tickBoxService(100000);
        assertSyncItemCount(0, 0, 1);
        // Start base
        UserContext userContext = createLevel1UserContext("00001");
        DaggerSlaveEmulator permanentSalve = new DaggerSlaveEmulator();
        permanentSalve.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(10, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        permanentSalve.tickPlanetServiceBaseServiceActive();
        permanentSalve.assertSyncItemCount(1, 0, 1);
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        // Pick box
        SyncBoxItem syncBoxItem = findSyncBoxItem(FallbackConfig.BOX_ITEM_TYPE_LONG_ID);
        getCommandService().pickupBox(builder, syncBoxItem);
        tickPlanetServiceBaseServiceActive();
        verifySlavePick(permanentSalve, builder.getId(), syncBoxItem.getId());
        // Verify
        permanentSalve.assertSyncItemCount(1, 0, 0);
        Assert.assertEquals(1, getTestGameLogicListener().getBoxPicked().size());
        TestGameLogicListener.BoxPickedEntry boxPickedEntry = getTestGameLogicListener().getBoxPicked().get(0);
        Assert.assertEquals(1, boxPickedEntry.getUserId());
        Assert.assertEquals(10, boxPickedEntry.getBoxContent().getCrystals());
        Assert.assertEquals(2, boxPickedEntry.getBoxContent().getInventoryItems().size());
        TestHelper.assertObjects(boxPickedEntry.getBoxContent().getInventoryItems(),
                getInventoryTypeService().getInventoryItem(FallbackConfig.INVENTORY_ITEM_GOLD_ID),
                getInventoryTypeService().getInventoryItem(FallbackConfig.INVENTORY_ITEM_ATTACKER_ID));
    }

    @Test
    public void testItemDropBoxes() {
        StaticGameConfig staticGameConfig = FallbackConfig.setupStaticGameConfig();
        BaseItemType builderType = GameTestContent.findBaseItemType(FallbackConfig.BUILDER_ITEM_TYPE_ID, staticGameConfig.getBaseItemTypes());
        builderType.setDropBoxPossibility(1.0);
        builderType.setDropBoxItemTypeId(FallbackConfig.BOX_ITEM_TYPE_LONG_ID);
        BaseItemType factoryType = GameTestContent.findBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID, staticGameConfig.getBaseItemTypes());
        factoryType.setDropBoxPossibility(1.0);
        factoryType.setDropBoxItemTypeId(FallbackConfig.BOX_ITEM_TYPE_LONG_ID);
        setupMasterEnvironment(staticGameConfig);
        // Setup target to drop boxes
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(10, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(30, 20), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        // Setup attacker bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(3).createDirectly(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).autoAttack(true).realm(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(10, 10, 50, 50))).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        getBotService().startBots(botConfigs);
        // Attack
        assertSyncItemCount(5, 0, 0);
        TestSimpleScheduledFuture botScheduledFuture = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT_TICKER);
        for (int i = 0; i < 100; i++) {
            botScheduledFuture.invokeRun();
            tickPlanetServiceBaseServiceActive();
        }
        assertSyncItemCount(3, 0, 2);

    }

    private void tickBoxService(int count) {
        BoxService boxService = getBoxService();
        for (int i = 0; i < count; i++) {
            boxService.tick();
        }
    }

    private void assertSyncItemCount(DaggerSlaveEmulator permanentSalve, int boxCount) {
        assertSyncItemCount(0, 0, boxCount);
        permanentSalve.assertSyncItemCount(0, 0, boxCount);
        DaggerSlaveEmulator tmpSalve = new DaggerSlaveEmulator();
        UserContext tmpUserContext = createLevel1UserContext();
        tmpSalve.connectToMaster(tmpUserContext, this);
        tmpSalve.assertSyncItemCount(0, 0, boxCount);
        tmpSalve.disconnectFromMaster();
    }

    private void verifySlavePick(DaggerSlaveEmulator permanentSalve, int pickerId, int boxId) {
        SyncBaseItem syncBaseItem = permanentSalve.getSyncItemContainerService().getSyncBaseItem(pickerId);
        Assert.assertFalse(syncBaseItem.isIdle());
        permanentSalve.tickPlanetServiceBaseServiceActive();
        Assert.assertTrue(syncBaseItem.isIdle());
    }

}