package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.uiservice.renderer.BabylonItem;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * on 12.09.2017.
 */
public class SyncStaticItemSetPositionMonitor extends AbstractSyncItemSetPositionMonitor {
    private final Set<BabylonItem> visibleBabylonItems = new HashSet<>();
    private final BabylonRendererService babylonRendererService;
    private final MarkerConfig markerConfig;
    private SyncItemSimpleDto nearestOutOfViewSyncItem;
    private double nearestOutOfViewDistance;

    public SyncStaticItemSetPositionMonitor(BabylonRendererService babylonRendererService, MarkerConfig markerConfig, Runnable releaseCallback) {
        super(releaseCallback);
        this.babylonRendererService = babylonRendererService;
        this.markerConfig = markerConfig;
    }

    public void setInvisible(SyncItemSimpleDto syncItemSimpleDto, DecimalPosition viewFieldCenter) {
        if (syncItemSimpleDto == null) {
            this.nearestOutOfViewSyncItem = null;
        } else {
            if (this.nearestOutOfViewSyncItem == null) {
                this.nearestOutOfViewSyncItem = syncItemSimpleDto;
                this.nearestOutOfViewDistance = viewFieldCenter.getDistance(syncItemSimpleDto.getPosition2d());
            } else {
                double distance = viewFieldCenter.getDistance(syncItemSimpleDto.getPosition2d());
                if (distance < this.nearestOutOfViewDistance) {
                    this.nearestOutOfViewSyncItem = syncItemSimpleDto;
                    this.nearestOutOfViewDistance = distance;
                }
            }
        }
    }

    public void addVisible(BabylonItem babylonItem) {
        visibleBabylonItems.add(babylonItem);
        babylonItem.mark(markerConfig);
    }

    public void removeVisible(BabylonItem babylonItem) {
        visibleBabylonItems.remove(babylonItem);
        babylonItem.mark(null);
    }

    @Override
    public void release() {
        visibleBabylonItems.forEach(babylonItem -> babylonItem.mark(null));
        visibleBabylonItems.clear();
        super.release();
    }

    public void handleOutOfView(DecimalPosition viewFieldCenter) {
        if (visibleBabylonItems.isEmpty()) {
            babylonRendererService.showOutOfViewMarker(null, 0);
        } else {
            if (nearestOutOfViewSyncItem != null) {
                babylonRendererService.showOutOfViewMarker(markerConfig,
                        viewFieldCenter.getAngle(nearestOutOfViewSyncItem.getPosition2d()));
            } else {
                babylonRendererService.showOutOfViewMarker(null, 0);
            }
        }
    }
}
