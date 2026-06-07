/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 09.05.2010
 * Time: 11:36:12
 */

public class ResourceRegion {
    private final Logger logger = Logger.getLogger(ResourceRegion.class.getName());
    private final ItemTypeService itemTypeService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final ResourceService resourceService;
    private final Set<Integer> syncResourceItems = new HashSet<>();
    // For evenly-distributed regions: maps each live resource's sync-item id to its fixed grid slot,
    // so an exhausted spot respawns at the same position and the even pattern stays stable over time.
    private final Map<Integer, DecimalPosition> evenSlotById = new HashMap<>();
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
     * grid points that lie inside the polygon and on free land. Slot positions are remembered so exhausted spots
     * respawn at the same place (see {@link #onResourceItemRemoved}). Falls back to random placement for non-polygon regions.
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
        double rowHeight = spacing * 0.8660254; // sqrt(3)/2 → hexagonal packing, looks more even and less grid-like
        double radius = resourceItemType.getRadius() + resourceRegionConfig.getMinDistanceToItems();
        Rectangle2D aabb = polygon.toAabb();
        double minX = aabb.getStart().getX();
        double minY = aabb.getStart().getY();
        double maxX = minX + aabb.width();
        double maxY = minY + aabb.height();
        int placed = 0;
        int row = 0;
        for (double y = minY + spacing / 2.0; y <= maxY && placed < count; y += rowHeight, row++) {
            double xOffset = (row % 2 == 0) ? 0.0 : spacing / 2.0;
            for (double x = minX + spacing / 2.0 + xOffset; x <= maxX && placed < count; x += spacing) {
                DecimalPosition position = new DecimalPosition(x, y);
                if (!polygon.isInside(position)) {
                    continue;
                }
                if (!syncItemContainerService.isFreePosition(resourceItemType.getTerrainType(), position, radius, true)) {
                    continue;
                }
                placeEvenResource(position);
                placed++;
            }
        }
        if (placed < count) {
            logger.info("ResourceRegion '" + resourceRegionConfig.getInternalName() + "' evenly placed " + placed + " of requested " + count + " spots (limited by free land inside the region).");
        }
    }

    private void placeEvenResource(DecimalPosition position) {
        SyncResourceItem syncResourceItem = resourceService.createResource(resourceItemType.getId(), position, MathHelper.getRandomAngle());
        synchronized (syncResourceItems) {
            syncResourceItems.add(syncResourceItem.getId());
        }
        evenSlotById.put(syncResourceItem.getId(), position);
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
                    placeEvenResource(slot); // evenly distributed: respawn at the same slot to keep the pattern
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
        }
    }
}
