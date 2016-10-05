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
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    public void createResources(Map<Integer, DecimalPosition> resourceItemType) {
        for (Map.Entry<Integer, DecimalPosition> entry : resourceItemType.entrySet()) {
            ResourceItemType resourceItem =  itemTypeService.getResourceItemType(entry.getKey());
            Vertex position = terrainService.calculatePositionGroundMesh(entry.getValue());
            SyncResourceItem syncResourceItem = syncItemContainerService.createSyncResourceItem(resourceItem, position);
            syncResourceItem.setup(resourceItem.getAmount());
        }
    }

    public SyncResourceItem getSyncResourceItem(int id) {
        throw new UnsupportedOperationException();
    }
}
