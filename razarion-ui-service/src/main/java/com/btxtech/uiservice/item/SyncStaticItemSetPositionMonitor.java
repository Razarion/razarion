package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.uiservice.renderer.ViewField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 12.09.2017.
 */
public class SyncStaticItemSetPositionMonitor extends AbstractSyncItemSetPositionMonitor {
    private Set<SyncItemSimpleDto> syncItems = new HashSet<>();
    private List<Vertex> inViewVertices = new ArrayList<>();
    private Polygon2D viewFieldPolygon;
    private DecimalPosition viewFieldCenter;
    private DecimalPosition nearestOutOfViewPosition;

    public SyncStaticItemSetPositionMonitor(Collection<? extends SyncItemSimpleDto> syncItems, ViewField viewField, Runnable releaseCallback) {
        super(releaseCallback);
        viewFieldPolygon = viewField.toPolygon();
        viewFieldCenter = viewField.calculateCenter();
        this.syncItems = new HashSet<>(syncItems);
        setupVertices();
    }

    @Override
    public DecimalPosition getNearestOutOfViewPosition2d() {
        return nearestOutOfViewPosition;
    }

    @Override
    public boolean hasInViewPositions() {
        return !inViewVertices.isEmpty();
    }

    @Override
    public List<Vertex> getInViewPosition3d() {
        return inViewVertices;
    }

    public void add(SyncItemSimpleDto resource) {
        syncItems.add(resource);
        setupVertices();
    }

    public void remove(SyncItemSimpleDto resource) {
        syncItems.remove(resource);
        setupVertices();
    }

    public void onViewChanged(ViewField viewField) {
        viewFieldPolygon = viewField.toPolygon();
        viewFieldCenter = viewField.calculateCenter();
        setupVertices();
    }

    private void setupVertices() {
        inViewVertices.clear();
        double distance = Double.MAX_VALUE;
        for (SyncItemSimpleDto resource : syncItems) {
            if (viewFieldPolygon.isInside(resource.getPosition2d())) {
                inViewVertices.add(resource.getPosition3d());
                nearestOutOfViewPosition = null;
            } else {
                if (inViewVertices.isEmpty()) {
                    double newDistance = viewFieldCenter.getDistance(resource.getPosition2d());
                    if (newDistance < distance) {
                        distance = newDistance;
                        nearestOutOfViewPosition = resource.getPosition2d();
                    }
                }
            }
        }
    }
}
