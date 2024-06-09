package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;

/**
 * Created by Beat
 * on 12.09.2017.
 */
public class SyncStaticItemSetPositionMonitor extends AbstractSyncItemSetPositionMonitor {

    public SyncStaticItemSetPositionMonitor(BabylonRendererService babylonRendererService, MarkerConfig markerConfig, Runnable releaseCallback) {
        super(babylonRendererService, markerConfig, releaseCallback);
    }

    public void setInvisibleSyncItem(SyncItemSimpleDto syncItemSimpleDto, DecimalPosition viewFiledCenter) {
        setInvisible(syncItemSimpleDto != null ? syncItemSimpleDto.getPosition() : null,
                viewFiledCenter);
    }
}
