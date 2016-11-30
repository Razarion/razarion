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
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
    @Inject
    private GameLogicService gameLogicService;
    private final Map<Integer, SyncResourceItem> resources = new HashMap<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        synchronized (resources) {
            resources.clear();
        }
    }

    public void createResources(Collection<ResourceItemPosition> resourceItemPositions) {
        for (ResourceItemPosition resourceItemPosition : resourceItemPositions) {
            createResources(resourceItemPosition.getResourceItemTypeId(), resourceItemPosition.getPosition(), resourceItemPosition.getRotationZ());
        }
    }

    public SyncResourceItem createResources(int resourceItemTypeId, DecimalPosition position2d, double rotationZ) {
        ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resourceItemTypeId);
        SyncResourceItem syncResourceItem = syncItemContainerService.createSyncResourceItem(resourceItemType, position2d, rotationZ);
        syncResourceItem.setup(resourceItemType.getAmount());
        synchronized (resources) {
            resources.put(syncResourceItem.getId(), syncResourceItem);
        }
        gameLogicService.onResourceCreated(syncResourceItem);
        return syncResourceItem;
    }

    public void resourceExhausted(SyncResourceItem syncResourceItem) {
        gameLogicService.onResourceExhausted(syncResourceItem);
        synchronized (resources) {
            resources.remove(syncResourceItem.getId());
        }
    }

    public SyncResourceItem getSyncResourceItem(int id) {
        SyncResourceItem syncResourceItem = resources.get(id);
        if (syncResourceItem == null) {
            throw new ItemDoesNotExistException(id);
        }
        return syncResourceItem;
    }

    public List<ModelMatrices> provideModelMatrices(ResourceItemType resourceItemType) {
        List<ModelMatrices> modelMatrices = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItem syncResourceItem : resources.values()) {
                if (!syncResourceItem.getItemType().equals(resourceItemType)) {
                    continue;
                }
                modelMatrices.add(syncResourceItem.getModelMatrices());
            }
        }
        return modelMatrices;
    }
}
