package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 10.09.2018.
 */
public class SynchronizationSendingContextTest {

    @Test
    public void test1() {
        SyncBaseItem syncBaseItem1 = GameTestHelper.createMockSyncBaseItem(1, 2, TerrainType.LAND, new DecimalPosition(10, 10), null);
        SyncBaseItem syncBaseItem2 = GameTestHelper.createMockSyncBaseItem(2, 2, TerrainType.LAND, new DecimalPosition(10, 20), null);
        SyncBaseItem syncBaseItem3 = GameTestHelper.createMockSyncBaseItem(3, 2, TerrainType.LAND, new DecimalPosition(10, 30), null);
        // Case 1
        SynchronizationSendingContext synchronizationSendingContext = new SynchronizationSendingContext();
        Assert.assertEquals(0, synchronizationSendingContext.getCollisions().size());
        // Case 2
        synchronizationSendingContext = new SynchronizationSendingContext();
        synchronizationSendingContext.addCollision(syncBaseItem1.getSyncPhysicalMovable(), syncBaseItem2.getSyncPhysicalMovable());
        Assert.assertEquals(1, synchronizationSendingContext.getCollisions().size());
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().findFirst().orElseThrow(AssertionError::new), 1, 2);
        // Case 3
        synchronizationSendingContext = new SynchronizationSendingContext();
        synchronizationSendingContext.addCollision(syncBaseItem1.getSyncPhysicalMovable(), syncBaseItem2.getSyncPhysicalMovable());
        synchronizationSendingContext.addCollision(syncBaseItem2.getSyncPhysicalMovable(), syncBaseItem3.getSyncPhysicalMovable());
        Assert.assertEquals(1, synchronizationSendingContext.getCollisions().size());
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().findFirst().orElseThrow(AssertionError::new), 1, 2, 3);
        // Case 4
        synchronizationSendingContext = new SynchronizationSendingContext();
        synchronizationSendingContext.addCollision(syncBaseItem2.getSyncPhysicalMovable(), syncBaseItem3.getSyncPhysicalMovable());
        synchronizationSendingContext.addCollision(syncBaseItem1.getSyncPhysicalMovable(), syncBaseItem2.getSyncPhysicalMovable());
        Assert.assertEquals(1, synchronizationSendingContext.getCollisions().size());
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().findFirst().orElseThrow(AssertionError::new), 1, 2, 3);
        // Case 5
        synchronizationSendingContext = new SynchronizationSendingContext();
        synchronizationSendingContext.addCollision(syncBaseItem1.getSyncPhysicalMovable(), syncBaseItem2.getSyncPhysicalMovable());
        Assert.assertEquals(1, synchronizationSendingContext.getCollisions().size());
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().findFirst().orElseThrow(AssertionError::new), 1, 2);
    }

    @Test
    public void test2() {
        SyncBaseItem syncBaseItem1 = GameTestHelper.createMockSyncBaseItem(1, 2, TerrainType.LAND, new DecimalPosition(10, 10), null);
        SyncBaseItem syncBaseItem2 = GameTestHelper.createMockSyncBaseItem(2, 2, TerrainType.LAND, new DecimalPosition(10, 20), null);
        SyncBaseItem syncBaseItem3 = GameTestHelper.createMockSyncBaseItem(3, 2, TerrainType.LAND, new DecimalPosition(10, 30), null);
        SyncBaseItem syncBaseItem4 = GameTestHelper.createMockSyncBaseItem(4, 2, TerrainType.LAND, new DecimalPosition(10, 40), null);
        SyncBaseItem syncBaseItem5 = GameTestHelper.createMockSyncBaseItem(5, 2, TerrainType.LAND, new DecimalPosition(10, 50), null);

        SynchronizationSendingContext synchronizationSendingContext = new SynchronizationSendingContext();
        synchronizationSendingContext.addCollision(syncBaseItem1.getSyncPhysicalMovable(), syncBaseItem2.getSyncPhysicalMovable());
        synchronizationSendingContext.addCollision(syncBaseItem2.getSyncPhysicalMovable(), syncBaseItem3.getSyncPhysicalMovable());
        synchronizationSendingContext.addCollision(syncBaseItem4.getSyncPhysicalMovable(), syncBaseItem5.getSyncPhysicalMovable());
        Assert.assertEquals(2, synchronizationSendingContext.getCollisions().size());
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().filter(syncBaseItems -> syncBaseItems.size() == 3).findFirst().orElseThrow(AssertionError::new), 1, 2, 3);
        AbstractDaggerIntegrationTest.assertContainingSyncItemIds(synchronizationSendingContext.getCollisions().stream().filter(syncBaseItems -> syncBaseItems.size() == 2).findFirst().orElseThrow(AssertionError::new), 4, 5);
    }

}