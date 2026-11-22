package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.utils.MathHelper;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Razarion contributors
 * Date: 09.05.2010
 * Time: 11:36:12
 */

public class ResourceRegion {
    private final Logger logger = Logger.getLogger(ResourceRegion.class.getName());
    private final ItemTypeService itemTypeService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final ResourceService resourceService;
    // How long an exhausted resource waits when neither its slot nor any spot beside it is free, before it is
    // placed randomly in the region.
    static final long SLOT_BLOCKED_GIVE_UP_MS = 120_000;
    // How far from its slot a resource may come back when the slot itself is taken, and the step of that search.
    static final double SLOT_MOVE_MAX_DISTANCE = 20;
    private static final double SLOT_MOVE_STEP = 2;
    private static final int SPACING_STRETCH_STEPS = 100;
    private final Set<Integer> syncResourceItems = new HashSet<>();
    // For evenly-distributed regions: one fixed slot per resource. evenSlotById maps each live resource's
    // sync-item id to its slot - the anchor, not necessarily where the resource stands. An exhausted resource
    // comes back at once, on its slot or, while the harvester that emptied it still stands there, on the
    // nearest free spot beside it; the anchor stays, so the next one is back on the slot and the pattern
    // never wanders. Respawning at a random spot instead let the resources near the start places get
    // harvested away and pile up where nobody harvests (noob island 2026-11-22: a 131 m hole over five
    // start places, 363 took 20-50 s for one in ten).
    private final Map<Integer, DecimalPosition> evenSlotById = new HashMap<>();
    // Slots with no free spot at or beside them (buildings all around), with the time their resource was
    // exhausted; tick tries them again.
    private final Map<DecimalPosition, Long> pendingSlots = new HashMap<>();
    private ResourceRegionConfig resourceRegionConfig;
    private ResourceItemType resourceItemType;

    @Inject
    public ResourceRegion(ResourceService resourceService,
                          SyncItemContainerServiceImpl syncItemContainerService,
                          ItemTypeService itemTypeService) {
        this.resourceService = resourceService;
        this.syncItemContainerService = syncItemContainerService;
        this.itemTypeService = itemTypeService;
    }

    public void init(ResourceRegionConfig resourceRegionConfig) {
        this.resourceRegionConfig = resourceRegionConfig;
        resourceItemType = itemTypeService.getResourceItemType(resourceRegionConfig.getResourceItemTypeId());
        if (resourceRegionConfig.isEvenlyDistributed()) {
            generateEvenResources(resourceRegionConfig.getCount());
        } else {
            generateResources(resourceRegionConfig.getCount());
        }
    }

    private void generateResources(int count) {
        for (int i = 0; i < count; i++) {
            generateResource();
        }
    }

    private void generateResource() {
        try {
            DecimalPosition position = syncItemContainerService.getFreeRandomPosition(resourceItemType.getTerrainType(), resourceItemType.getRadius() + resourceRegionConfig.getMinDistanceToItems(), true, resourceRegionConfig.getRegion());
            SyncResourceItem syncResourceItem = resourceService.createResource(resourceItemType.getId(), position, MathHelper.getRandomAngle());
            synchronized (syncResourceItems) {
                syncResourceItems.add(syncResourceItem.getId());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Places up to {@code count} resources on a hexagonally-offset grid covering the region polygon, keeping only the
     * grid points that lie inside the polygon and on free land. The grid is stretched as far as it still holds
     * {@code count} points, so the resources reach across the whole region instead of filling it from one side.
     * Each resource keeps its slot for good (see {@link #onResourceItemRemoved} and {@link #tick}). Falls back to
     * random placement for non-polygon regions.
     */
    private void generateEvenResources(int count) {
        PlaceConfig region = resourceRegionConfig.getRegion();
        if (region == null || region.getPolygon2D() == null) {
            generateResources(count);
            return;
        }
        Polygon2D polygon = region.getPolygon2D();
        double area = polygonArea(polygon);
        if (count <= 0 || area <= 0) {
            return;
        }
        double spacing = Math.sqrt(area / count);
        if (spacing <= 0) {
            return;
        }
        // A grid with sqrt(area / count) holds more than count points (the first row starts half a spacing in),
        // and stopping at count left the far side of a narrow region empty: stretch while it still holds count.
        for (int i = 0; i < SPACING_STRETCH_STEPS && gridSlots(polygon, spacing * 1.01).size() >= count; i++) {
            spacing *= 1.01;
        }
        List<DecimalPosition> slots = gridSlots(polygon, spacing);
        int placed = 0;
        for (DecimalPosition slot : slots) {
            if (placed >= count) {
                break;
            }
            placeEvenResource(slot, slot);
            placed++;
        }
        if (placed < count) {
            logger.info("ResourceRegion '" + resourceRegionConfig.getInternalName() + "' evenly placed " + placed + " of requested " + count + " spots (limited by free land inside the region).");
        }
    }

    private List<DecimalPosition> gridSlots(Polygon2D polygon, double spacing) {
        List<DecimalPosition> slots = new ArrayList<>();
        double rowHeight = spacing * 0.8660254; // sqrt(3)/2 → hexagonal packing, looks more even and less grid-like
        Rectangle2D aabb = polygon.toAabb();
        double minX = aabb.getStart().getX();
        double minY = aabb.getStart().getY();
        double maxX = minX + aabb.width();
        double maxY = minY + aabb.height();
        int row = 0;
        for (double y = minY + spacing / 2.0; y <= maxY; y += rowHeight, row++) {
            double xOffset = (row % 2 == 0) ? 0.0 : spacing / 2.0;
            for (double x = minX + spacing / 2.0 + xOffset; x <= maxX; x += spacing) {
                DecimalPosition position = new DecimalPosition(x, y);
                if (polygon.isInside(position) && isFreeSlot(position)) {
                    slots.add(position);
                }
            }
        }
        return slots;
    }

    private boolean isFreeSlot(DecimalPosition position) {
        double radius = resourceItemType.getRadius() + resourceRegionConfig.getMinDistanceToItems();
        return syncItemContainerService.isFreePosition(resourceItemType.getTerrainType(), position, radius, true);
    }

    private void placeEvenResource(DecimalPosition slot, DecimalPosition position) {
        SyncResourceItem syncResourceItem = resourceService.createResource(resourceItemType.getId(), position, MathHelper.getRandomAngle());
        synchronized (syncResourceItems) {
            syncResourceItems.add(syncResourceItem.getId());
        }
        evenSlotById.put(syncResourceItem.getId(), slot);
    }

    /** On the slot if it is free, else on the nearest free spot beside it; false if there is none. */
    private boolean respawnAtSlot(DecimalPosition slot) {
        DecimalPosition position = isFreeSlot(slot) ? slot : findFreeBeside(slot);
        if (position == null) {
            return false;
        }
        placeEvenResource(slot, position);
        return true;
    }

    /**
     * Tries the slots again that had no free spot at or beside them. After {@link #SLOT_BLOCKED_GIVE_UP_MS}
     * the resource is placed randomly, so the region keeps its count. Master only, called from the planet tick.
     */
    public void tick(long now) {
        synchronized (syncResourceItems) {
            if (pendingSlots.isEmpty()) {
                return;
            }
            for (Map.Entry<DecimalPosition, Long> entry : new ArrayList<>(pendingSlots.entrySet())) {
                DecimalPosition slot = entry.getKey();
                if (respawnAtSlot(slot)) {
                    pendingSlots.remove(slot);
                } else if (now - entry.getValue() > SLOT_BLOCKED_GIVE_UP_MS) {
                    logger.warning("ResourceRegion '" + resourceRegionConfig.getInternalName() + "' slot " + slot + " blocked with no free spot beside it - placed randomly");
                    pendingSlots.remove(slot);
                    generateResource();
                }
            }
        }
    }

    private DecimalPosition findFreeBeside(DecimalPosition slot) {
        Polygon2D polygon = resourceRegionConfig.getRegion().getPolygon2D();
        for (double distance = SLOT_MOVE_STEP; distance <= SLOT_MOVE_MAX_DISTANCE; distance += SLOT_MOVE_STEP) {
            int steps = (int) Math.ceil(2 * Math.PI * distance / SLOT_MOVE_STEP);
            for (int i = 0; i < steps; i++) {
                DecimalPosition candidate = slot.getPointWithDistance(2 * Math.PI * i / steps, distance);
                if (polygon.isInside(candidate) && isFreeSlot(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static double polygonArea(Polygon2D polygon) {
        List<DecimalPosition> corners = polygon.getCorners();
        double area = 0;
        for (int i = 0, j = corners.size() - 1; i < corners.size(); j = i++) {
            area += (corners.get(j).getX() + corners.get(i).getX()) * (corners.get(j).getY() - corners.get(i).getY());
        }
        return Math.abs(area / 2.0);
    }

    public boolean onResourceItemRemoved(SyncResourceItem syncResourceItem) {
        synchronized (syncResourceItems) {
            if (syncResourceItems.remove(syncResourceItem.getId())) {
                DecimalPosition slot = evenSlotById.remove(syncResourceItem.getId());
                if (slot != null) {
                    // Evenly distributed: back at once, on the slot or beside the harvester that stands on it
                    if (!respawnAtSlot(slot)) {
                        pendingSlots.put(slot, System.currentTimeMillis());
                    }
                } else {
                    generateResource();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public void kill() {
        synchronized (syncResourceItems) {
            syncResourceItems.forEach(id -> {
                SyncResourceItem syncResourceItem = syncItemContainerService.getSyncResourceItem(id);
                syncResourceItem.setAmount(0);
                resourceService.removeSyncResourceItem(syncResourceItem);
            });
            syncResourceItems.clear();
            evenSlotById.clear();
            pendingSlots.clear();
        }
    }
}
