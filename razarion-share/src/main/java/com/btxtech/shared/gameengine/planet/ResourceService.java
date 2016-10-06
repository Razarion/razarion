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

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: beat
 * Date: 08.05.2010
 * Time: 21:57:48
 */
@ApplicationScoped
public class ResourceService {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainService terrainService;
    private final Map<Integer, SyncResourceItem> resources = new HashMap<>();

    public void createResources(Collection<ResourceItemPosition> resourceItemPositions) {
        for (ResourceItemPosition resourceItemPosition : resourceItemPositions) {
            ResourceItemType resourceItem = itemTypeService.getResourceItemType(resourceItemPosition.getResourceItemTypeId());
            Vertex position = terrainService.calculatePositionGroundMesh(resourceItemPosition.getPosition());
            SyncResourceItem syncResourceItem = syncItemContainerService.createSyncResourceItem(resourceItem, position, resourceItemPosition.getRotationZ());
            syncResourceItem.setup(resourceItem.getAmount());
            synchronized (resources) {
                resources.put(syncResourceItem.getId(), syncResourceItem);
            }
        }
    }

    public SyncResourceItem getSyncResourceItem(int id) {
        throw new UnsupportedOperationException();
    }

    public List<ModelMatrices> provideModelMatrices(ResourceItemType resourceItemType, double scale) {
        List<ModelMatrices> modelMatrices = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItem syncResourceItem : resources.values()) {
                if (!syncResourceItem.getItemType().equals(resourceItemType)) {
                    continue;
                }
                modelMatrices.add(syncResourceItem.getSyncPhysicalArea().createModelMatrices(null, scale));
            }
        }
        return modelMatrices;
    }
}
