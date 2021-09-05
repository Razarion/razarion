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
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * User: beat
 * Date: 09.05.2010
 * Time: 11:36:12
 */
@Dependent
public class ResourceRegion {
    // private Logger logger = Logger.getLogger(ResourceRegion.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SyncItemContainerServiceImpl syncItemContainerService;
    @Inject
    private ResourceService resourceService;
    private final Set<Integer> syncResourceItems = new HashSet<>();
    private ResourceRegionConfig resourceRegionConfig;
    private ResourceItemType resourceItemType;

    public void init(ResourceRegionConfig resourceRegionConfig) {
        this.resourceRegionConfig = resourceRegionConfig;
        resourceItemType = itemTypeService.getResourceItemType(resourceRegionConfig.getResourceItemTypeId());
        generateResources(resourceRegionConfig.getCount());
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
            exceptionHandler.handleException(e);
        }
    }

    public boolean onResourceItemRemoved(SyncResourceItem syncResourceItem) {
        synchronized (syncResourceItems) {
            if (syncResourceItems.remove(syncResourceItem.getId())) {
                generateResource();
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
        }
    }
}
