package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
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
public class BoxServiceTest extends WeldMasterBaseTest {

    @Test
    public void testBoxRegions() {
        setupMasterEnvironment();
        WeldSlaveEmulator permanentSalve = new WeldSlaveEmulator();
        UserContext userContext = createLevel1UserContext();
        permanentSalve.connectToMater(userContext, this);
        assertSyncItemCount(permanentSalve, 0);
        // Start box service
        BoxService boxService = getBoxService();
        Collection<BoxRegionConfig> boxRegionConfigs = new ArrayList<>();
        boxRegionConfigs.add(new BoxRegionConfig().setBoxItemTypeId(GameTestContent.BOX_ITEM_TYPE_ID).setMinInterval(100).setMaxInterval(100).setCount(1).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(40, 50, 200, 150))));
        boxService.startBoxRegions(boxRegionConfigs);
        // Span box 1
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 0);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 1);
        SyncBoxItem box1 = findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_ID);
        Assert.assertTrue(box1.isAlive());
        // Span box 2
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 1);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 2);
        SyncBoxItem box2 = findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_ID, box1);
        Assert.assertTrue(box1.isAlive());
        Assert.assertTrue(box2.isAlive());
        // Span box 3
        tickBoxService(900);
        assertSyncItemCount(permanentSalve, 2);
        tickBoxService(100);
        assertSyncItemCount(permanentSalve, 3);
        SyncBoxItem box3 = findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_ID, box1, box2);
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
        SyncBoxItem box4 = findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_ID, box2, box3);
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
        boxRegionConfigs.add(new BoxRegionConfig().setBoxItemTypeId(GameTestContent.BOX_ITEM_TYPE_LONG_ID).setMinInterval(10000).setMaxInterval(10000).setCount(1).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(20, 20, 1, 1))));
        boxService.startBoxRegions(boxRegionConfigs);
        tickBoxService(100000);
        assertSyncItemCount(0, 0, 1);
        // Start base
        UserContext userContext = createLevel1UserContext(1);
        WeldSlaveEmulator permanentSalve = new WeldSlaveEmulator();
        permanentSalve.connectToMater(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(10, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        permanentSalve.assertSyncItemCount(1, 0, 1);
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Pick box
        SyncBoxItem syncBoxItem = findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_LONG_ID);
        getCommandService().pickupBox(builder, syncBoxItem);
        verifySlavePick(permanentSalve, builder.getId(), syncBoxItem.getId());
        tickPlanetServiceBaseServiceActive();
        // Verify
        permanentSalve.assertSyncItemCount(1, 0, 0);
        Assert.assertEquals(1, getTestGameLogicListener().getBoxPicked().size());
        TestGameLogicListener.BoxPickedEntry boxPickedEntry = getTestGameLogicListener().getBoxPicked().get(0);
        Assert.assertEquals(1, (int) boxPickedEntry.getHumanPlayerId().getUserId());
        Assert.assertEquals(10, boxPickedEntry.getBoxContent().getCrystals());
        Assert.assertEquals(2, boxPickedEntry.getBoxContent().getInventoryItems().size());
        TestHelper.assertObjects(boxPickedEntry.getBoxContent().getInventoryItems(),
                getInventoryTypeService().getInventoryItem(GameTestContent.INVENTORY_ITEM_GOLD_ID),
                getInventoryTypeService().getInventoryItem(GameTestContent.INVENTORY_ITEM_ATTACKER_ID));
    }

    @Test
    public void testItemDropBoxes() {
        StaticGameConfig staticGameConfig = GameTestContent.setupStaticGameConfig();
        BaseItemType builderType = GameTestContent.findBaseItemType(GameTestContent.BUILDER_ITEM_TYPE_ID, staticGameConfig.getBaseItemTypes());
        builderType.setDropBoxPossibility(1.0);
        builderType.setDropBoxItemTypeId(GameTestContent.BOX_ITEM_TYPE_LONG_ID);
        BaseItemType factoryType = GameTestContent.findBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID, staticGameConfig.getBaseItemTypes());
        factoryType.setDropBoxPossibility(1.0);
        factoryType.setDropBoxItemTypeId(GameTestContent.BOX_ITEM_TYPE_LONG_ID);
        setupMasterEnvironment(staticGameConfig, null);
        // Setup target to drop boxes
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(10, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(30, 20), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        // Setup attacker bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameTestContent.ATTACKER_ITEM_TYPE_ID).setCount(3).setCreateDirectly(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setAutoAttack(true).setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(10, 10, 50, 50))).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        getBotService().startBots(botConfigs, null);
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

    private void assertSyncItemCount(WeldSlaveEmulator permanentSalve, int boxCount) {
        assertSyncItemCount(0, 0, boxCount);
        permanentSalve.assertSyncItemCount(0, 0, boxCount);
        WeldSlaveEmulator tmpSalve = new WeldSlaveEmulator();
        UserContext tmpUserContext = createLevel1UserContext();
        tmpSalve.connectToMater(tmpUserContext, this);
        tmpSalve.assertSyncItemCount(0, 0, boxCount);
        tmpSalve.disconnectFromMaster();
    }

    private void verifySlavePick(WeldSlaveEmulator permanentSalve, int pickerId, int boxId) {
        SyncBaseItem syncBaseItem = permanentSalve.getSyncItemContainerService().getSyncBaseItem(pickerId);
        Assert.assertFalse(syncBaseItem.isIdle());
        permanentSalve.getBaseItemService().tick(null, null);
        Assert.assertFalse(syncBaseItem.isIdle());
    }

}