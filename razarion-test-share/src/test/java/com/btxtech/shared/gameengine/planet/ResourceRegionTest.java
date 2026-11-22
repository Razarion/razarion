package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An evenly distributed resource region keeps its pattern: an exhausted crystal comes back at once, on its own
 * slot or beside the harvester standing there, instead of somewhere random in the region. Stand-ins rather than mocks,
 * see AutoHarvestTest.
 */
public class ResourceRegionTest {
    // One noob island cell (PROD SERVER_RESOURCE_REGION_CONFIG 280, region 17599)
    private static final Polygon2D CELL = new Polygon2D(List.of(new DecimalPosition(20, 77), new DecimalPosition(290, 77), new DecimalPosition(290, 100), new DecimalPosition(20, 100)));
    private final Map<SyncResourceItem, DecimalPosition> created = new LinkedHashMap<>();
    private final List<DecimalPosition> blocked = new ArrayList<>();
    private static final DecimalPosition RANDOM = new DecimalPosition(150, 88);
    private boolean allBlocked;
    private ResourceRegion resourceRegion;
    private int nextId = 1;

    @Before
    public void setUp() {
        ItemTypeService itemTypeService = new ItemTypeService(new InitializeService()) {
            @Override
            public ResourceItemType getResourceItemType(Integer resourceItemTypeId) {
                return new ResourceItemType().setRadius(1).setTerrainType(TerrainType.LAND);
            }
        };
        SyncItemContainerServiceImpl syncItemContainerService = new SyncItemContainerServiceImpl(null, null, null, null, null, null, null, null) {
            @Override
            public boolean isFreePosition(TerrainType terrainType, DecimalPosition position, double radius, boolean excludeBotRealm) {
                return !allBlocked && blocked.stream().noneMatch(b -> b.getDistance(position) < radius);
            }

            @Override
            public DecimalPosition getFreeRandomPosition(TerrainType terrainType, double radius, boolean excludeBotRealm, PlaceConfig placeConfig) {
                return RANDOM;
            }
        };
        ResourceService resourceService = new ResourceService(null, null, itemTypeService, syncItemContainerService, new InitializeService()) {
            @Override
            public SyncResourceItem createResource(int resourceItemTypeId, DecimalPosition position2d, double rotationZ) {
                SyncResourceItem syncResourceItem = new SyncResourceItem(null);
                syncResourceItem.init(nextId++, null);
                created.put(syncResourceItem, position2d);
                return syncResourceItem;
            }
        };
        resourceRegion = new ResourceRegion(resourceService, syncItemContainerService, itemTypeService);
        ResourceRegionConfig config = new ResourceRegionConfig();
        config.setCount(8);
        config.setMinDistanceToItems(3);
        config.setResourceItemTypeId(2);
        config.setEvenlyDistributed(true);
        config.setInternalName("Phase 1 cell 1");
        config.setRegion(new PlaceConfig().polygon2D(CELL));
        resourceRegion.init(config);
    }

    @Test
    public void gridReachesAcrossTheWholeCell() {
        Assert.assertEquals(8, created.size());
        double minX = created.values().stream().mapToDouble(DecimalPosition::getX).min().orElseThrow();
        double maxX = created.values().stream().mapToDouble(DecimalPosition::getX).max().orElseThrow();
        // Before: 34 .. 229 - the last 60 m of the cell, over five start places, had no crystal
        Assert.assertTrue("minX " + minX, minX < 40);
        Assert.assertTrue("maxX " + maxX, maxX > 250);
    }

    @Test
    public void exhaustedCrystalComesBackAtOnceBesideTheHarvesterAndNextTimeOnItsSlot() {
        SyncResourceItem first = created.keySet().iterator().next();
        DecimalPosition slot = created.get(first);
        DecimalPosition harvester = slot.add(2.5, 0);
        blocked.add(harvester);
        int before = created.size();

        Assert.assertTrue(resourceRegion.onResourceItemRemoved(first));
        Assert.assertEquals("back at once", before + 1, created.size());
        DecimalPosition beside = lastCreated();
        Assert.assertNotEquals(slot, beside);
        Assert.assertTrue("beside " + slot.getDistance(beside), slot.getDistance(beside) <= ResourceRegion.SLOT_MOVE_MAX_DISTANCE);
        Assert.assertTrue("not on the harvester", harvester.getDistance(beside) >= 4);
        Assert.assertTrue(CELL.isInside(beside));

        // The slot stays the anchor: once the harvester has gone, the next one is back on it - no wandering
        blocked.clear();
        SyncResourceItem second = lastCreatedItem();
        Assert.assertTrue(resourceRegion.onResourceItemRemoved(second));
        Assert.assertEquals(slot, lastCreated());
    }

    @Test
    public void noFreeSpotWaitsThenGoesRandom() {
        SyncResourceItem first = created.keySet().iterator().next();
        allBlocked = true;
        int before = created.size();
        resourceRegion.onResourceItemRemoved(first);
        resourceRegion.tick(System.currentTimeMillis() + 1000);
        Assert.assertEquals("waits", before, created.size());

        resourceRegion.tick(System.currentTimeMillis() + ResourceRegion.SLOT_BLOCKED_GIVE_UP_MS + 1000);
        Assert.assertEquals(before + 1, created.size());
        Assert.assertEquals(RANDOM, lastCreated());
    }

    private SyncResourceItem lastCreatedItem() {
        SyncResourceItem last = null;
        for (SyncResourceItem item : created.keySet()) {
            last = item;
        }
        return last;
    }

    private DecimalPosition lastCreated() {
        DecimalPosition last = null;
        for (DecimalPosition position : created.values()) {
            last = position;
        }
        return last;
    }
}
