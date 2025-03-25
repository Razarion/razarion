package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;

import javax.inject.Inject;

/**
 * Created by Beat
 * 26.07.2016.
 */

public class SyncPhysicalArea extends AbstractSyncPhysical {
    @Inject
    public SyncPhysicalArea(SyncItemContainerServiceImpl syncItemContainerService) {
        super(syncItemContainerService);
    }
}
