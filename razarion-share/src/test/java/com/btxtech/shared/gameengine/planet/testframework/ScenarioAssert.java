package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import org.junit.Assert;

import java.util.List;

/**
 * Created by Beat
 * on 23.04.2018.
 */
public interface ScenarioAssert {
    static void compareSyncBaseItemInfo(List<SyncBaseItemInfo> expectedSyncBaseItemInfos, List<SyncBaseItemInfo> actualSyncBaseItemInfos) {
        Assert.assertEquals(expectedSyncBaseItemInfos.size(), actualSyncBaseItemInfos.size());
        for (int i = 0; i < expectedSyncBaseItemInfos.size(); i++) {
            try {
                compareSyncBaseItemInfo(expectedSyncBaseItemInfos.get(i), actualSyncBaseItemInfos.get(i));
            } catch (Throwable t) {
                System.out.println("Failed SyncBaseItemInfo: " + i);
                throw t;
            }
        }

    }

    static void compareSyncBaseItemInfo(SyncBaseItemInfo expectedSyncBaseItemInfo, SyncBaseItemInfo actualSyncBaseItemInfo) {
        Assert.assertEquals(expectedSyncBaseItemInfo.getId(), actualSyncBaseItemInfo.getId());
        Assert.assertEquals(expectedSyncBaseItemInfo.getItemTypeId(), actualSyncBaseItemInfo.getItemTypeId());
        compareSyncPhysicalAreaInfo(expectedSyncBaseItemInfo.getSyncPhysicalAreaInfo(), actualSyncBaseItemInfo.getSyncPhysicalAreaInfo());
        Assert.assertEquals(expectedSyncBaseItemInfo.getBaseId(), actualSyncBaseItemInfo.getBaseId());
        // TODO more asserts needed
    }

    static void compareSyncPhysicalAreaInfo(SyncPhysicalAreaInfo expectedSyncPhysicalAreaInfo, SyncPhysicalAreaInfo actualSyncPhysicalAreaInfo) {
        if (expectedSyncPhysicalAreaInfo == null && actualSyncPhysicalAreaInfo == null) {
            return;
        } else if (expectedSyncPhysicalAreaInfo != null && actualSyncPhysicalAreaInfo == null) {
            Assert.fail("expectedSyncPhysicalAreaInfo != null && actualSyncPhysicalAreaInfo == null");
        } else if (expectedSyncPhysicalAreaInfo == null) {
            Assert.fail("expectedSyncPhysicalAreaInfo == null");
        }
        TestHelper.assertDecimalPosition("Position", expectedSyncPhysicalAreaInfo.getPosition(), actualSyncPhysicalAreaInfo.getPosition());
        Assert.assertEquals(expectedSyncPhysicalAreaInfo.getAngle(), actualSyncPhysicalAreaInfo.getAngle(), 0.0001);
        TestHelper.assertDecimalPosition("Velocity", expectedSyncPhysicalAreaInfo.getVelocity(), actualSyncPhysicalAreaInfo.getVelocity());
        TestHelper.assertDecimalPositions(expectedSyncPhysicalAreaInfo.getWayPositions(), actualSyncPhysicalAreaInfo.getWayPositions());
        Assert.assertEquals(expectedSyncPhysicalAreaInfo.getCurrentWayPointIndex(), actualSyncPhysicalAreaInfo.getCurrentWayPointIndex());
        Assert.assertEquals(expectedSyncPhysicalAreaInfo.getTotalRange(), actualSyncPhysicalAreaInfo.getTotalRange());
    }
}
