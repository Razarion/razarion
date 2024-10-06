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
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
@Singleton
public class ResourceService {
    private SyncItemContainerServiceImpl syncItemContainerService;
    private ItemTypeService itemTypeService;
    private GameLogicService gameLogicService;
    private Provider<ResourceRegion> instance;
    private ExceptionHandler exceptionHandler;
    private final Map<Integer, SyncResourceItem> resources = new HashMap<>();
    private final Collection<ResourceRegion> resourceRegions = new ArrayList<>();
    private GameEngineMode gameEngineMode;
    private List<ResourceRegionConfig> resourceRegionConfigs;

    @Inject
    public ResourceService(ExceptionHandler exceptionHandler,
                           Provider<ResourceRegion> instance,
                           GameLogicService gameLogicService,
                           ItemTypeService itemTypeService,
                           SyncItemContainerServiceImpl syncItemContainerService,
                           InitializeService initializeService) {
        this.exceptionHandler = exceptionHandler;
        this.instance = instance;
        this.gameLogicService = gameLogicService;
        this.itemTypeService = itemTypeService;
        this.syncItemContainerService = syncItemContainerService;
        initializeService.receivePlanetActivationEvent(this::onPlanetActivation);
    }

    private void onPlanetActivation(PlanetActivationEvent planetActivationEvent) {
        switch (planetActivationEvent.getType()) {

            case INITIALIZE:
                setup(planetActivationEvent);
                break;
            case STOP:
                stop();
                break;
            default:
                throw new IllegalArgumentException("ResourceService.onPlanetActivation() can not handle: " + planetActivationEvent.getType());
        }
    }

    private void setup(PlanetActivationEvent planetActivationEvent) {
        synchronized (resources) {
            resources.clear();
        }
        gameEngineMode = planetActivationEvent.getGameEngineMode();
        if (planetActivationEvent.getMasterPlanetConfig() != null) {
            resourceRegionConfigs = planetActivationEvent.getMasterPlanetConfig().getResourceRegionConfigs();
        }
    }

    public void setupSlave(InitialSlaveSyncItemInfo initialSlaveSyncItemInfo) {
        if (initialSlaveSyncItemInfo.getSyncResourceItemInfos() != null) {
            for (SyncResourceItemInfo syncResourceItemInfo : initialSlaveSyncItemInfo.getSyncResourceItemInfos()) {
                createSyncResourceItemSlave(syncResourceItemInfo);
            }
        }
    }

    public void reloadResourceRegions(List<ResourceRegionConfig> resourceRegionConfigs) {
        this.resourceRegionConfigs = resourceRegionConfigs;
        synchronized (resourceRegions) {
            resourceRegions.forEach(ResourceRegion::kill);
            resourceRegions.clear();
        }
        startResourceRegions();
    }

    public void startResourceRegions() {
        synchronized (resourceRegions) {
            for (ResourceRegionConfig resourceRegionConfig : resourceRegionConfigs) {
                try {
                    ResourceRegion resourceRegion = instance.get();
                    resourceRegion.init(resourceRegionConfig);
                    resourceRegions.add(resourceRegion);
                } catch (Exception e) {
                    exceptionHandler.handleException("ResourceService.startResourceRegions()", e);
                }
            }
        }
    }

    private void stop() {
        resources.clear();
        resourceRegions.clear();
    }

    public void onSlaveSyncResourceItemChanged(SyncResourceItemInfo syncResourceItemInfo) {
        SyncResourceItem syncResourceItem = resources.get(syncResourceItemInfo.getId());
        if (syncResourceItem == null) {
            createSyncResourceItemSlave(syncResourceItemInfo);
        } else {
            syncResourceItem.synchronize(syncResourceItemInfo);
        }
    }

    private void createSyncResourceItemSlave(SyncResourceItemInfo syncResourceItemInfo) {
        ResourceItemType resourceItemType = itemTypeService.getResourceItemType(syncResourceItemInfo.getResourceItemTypeId());
        SyncResourceItem syncResourceItem = syncItemContainerService.createSyncResourceItemSlave(resourceItemType, syncResourceItemInfo.getId(), syncResourceItemInfo.getSyncPhysicalAreaInfo().getPosition(), syncResourceItemInfo.getSyncPhysicalAreaInfo().getAngle());
        syncResourceItem.setAmount(syncResourceItemInfo.getAmount());
        synchronized (resources) {
            resources.put(syncResourceItem.getId(), syncResourceItem);
        }
        gameLogicService.onResourceCreated(syncResourceItem);
    }

    public void createResources(Collection<ResourceItemPosition> resourceItemPositions) {
        for (ResourceItemPosition resourceItemPosition : resourceItemPositions) {
            if (resourceItemPosition.getResourceItemTypeId() == null) {
                continue;
            }
            if (resourceItemPosition.getPosition() == null) {
                continue;
            }
            createResource(resourceItemPosition.getResourceItemTypeId(), resourceItemPosition.getPosition(), resourceItemPosition.getRotationZ());
        }
    }

    public SyncResourceItem createResource(int resourceItemTypeId, DecimalPosition position2d, double rotationZ) {
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
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }
        removeSyncResourceItem(syncResourceItem);
        synchronized (resourceRegions) {
            for (ResourceRegion resourceRegion : resourceRegions) {
                if (resourceRegion.onResourceItemRemoved(syncResourceItem)) {
                    break;
                }
            }
        }
    }

    public void removeSyncResourceItem(SyncResourceItem syncResourceItem) {
        gameLogicService.onResourceExhausted(syncResourceItem);
        synchronized (resources) {
            resources.remove(syncResourceItem.getId());
        }
        syncItemContainerService.destroySyncItem(syncResourceItem);
    }

    public SyncResourceItem getSyncResourceItem(int id) {
        SyncResourceItem syncResourceItem = resources.get(id);
        if (syncResourceItem == null) {
            throw new ItemDoesNotExistException(id);
        }
        return syncResourceItem;
    }

    public List<SyncResourceItemInfo> getSyncResourceItemInfos() {
        List<SyncResourceItemInfo> syncResourceItemInfos = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItem syncResourceItem : resources.values()) {
                syncResourceItemInfos.add(syncResourceItem.getSyncInfo());
            }
        }
        return syncResourceItemInfos;
    }
}
