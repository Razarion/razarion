package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

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
        TestHelper.assertIds(userContext.getInventoryItemIds());
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(10, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        // Pick box
        getCommandService().pickupBox(builder, findSyncBoxItem(GameTestContent.BOX_ITEM_TYPE_LONG_ID));
        tickPlanetServiceBaseServiceActive();
        // Verify
        TestHelper.assertIds(userContext.getInventoryItemIds(), GameTestContent.INVENTORY_ITEM_ATTACKER_ID, GameTestContent.INVENTORY_ITEM_GOLD_ID);
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

}