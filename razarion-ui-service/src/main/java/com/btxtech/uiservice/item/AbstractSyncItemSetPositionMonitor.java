package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.renderer.BabylonItem;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Beat
 * on 13.09.2017.
 */
public abstract class AbstractSyncItemSetPositionMonitor {
    private final BabylonRendererService babylonRendererService;
    private final MarkerConfig markerConfig;
    private Runnable releaseCallback;
    private final Set<BabylonItem> visibleBabylonItems = new HashSet<>();
    private DecimalPosition nearestOutOfViewPosition;
    private double nearestOutOfViewDistance;

    public AbstractSyncItemSetPositionMonitor(BabylonRendererService babylonRendererService, MarkerConfig markerConfig, Runnable releaseCallback) {
        this.releaseCallback = releaseCallback;
        this.babylonRendererService = babylonRendererService;
        this.markerConfig = markerConfig;
    }

    public void addVisible(BabylonItem babylonItem) {
        visibleBabylonItems.add(babylonItem);
        babylonItem.mark(markerConfig);
    }

    public void removeVisible(BabylonItem babylonItem) {
        visibleBabylonItems.remove(babylonItem);
        babylonItem.mark(null);
    }

    protected void setInvisible(DecimalPosition position, DecimalPosition viewFieldCenter) {
        if (position == null) {
            this.nearestOutOfViewPosition = null;
        } else {
            if (this.nearestOutOfViewPosition == null) {
                this.nearestOutOfViewPosition = position;
                this.nearestOutOfViewDistance = viewFieldCenter.getDistance(position);
            } else {
                double distance = viewFieldCenter.getDistance(position);
                if (distance < this.nearestOutOfViewDistance) {
                    this.nearestOutOfViewPosition = position;
                    this.nearestOutOfViewDistance = distance;
                }
            }
        }
    }

    public void handleOutOfView(DecimalPosition viewFieldCenter) {
        if (visibleBabylonItems.isEmpty()) {
            if (nearestOutOfViewPosition != null) {
                showOutOfViewMarker(markerConfig, viewFieldCenter.getAngle(nearestOutOfViewPosition));
            } else {
                showOutOfViewMarker(null, 0);
            }
        } else {
            showOutOfViewMarker(null, 0);
        }
    }

    private void showOutOfViewMarker(MarkerConfig markerConfig, double angle) {
        babylonRendererService.showOutOfViewMarker(markerConfig, angle);
    }

    public void release() {
        visibleBabylonItems.forEach(babylonItem -> babylonItem.mark(null));
        visibleBabylonItems.clear();

        showOutOfViewMarker(null, 0);

        if (releaseCallback == null) {
            throw new IllegalStateException("AbstractSyncItemSetPositionMonitor.release() releaseCallback == null");
        }
        releaseCallback.run();
        releaseCallback = null;
    }

    protected void check4Visible(Predicate<BabylonItem> filter) {
        visibleBabylonItems.removeIf(babylonItem -> {
            if(!filter.test(babylonItem)) {
                babylonItem.mark(null);
                return true;
            }
            return false;
        });
    }

}
