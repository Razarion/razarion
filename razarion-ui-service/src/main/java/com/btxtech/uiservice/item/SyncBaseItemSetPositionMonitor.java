package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 13.09.2017.
 */
public class SyncBaseItemSetPositionMonitor extends AbstractSyncItemSetPositionMonitor {
    private Set<Integer> itemTypeFilter;
    private List<Vertex> inViewVertices = new ArrayList<>();
    private DecimalPosition nearestOutOfViewPosition;
    private double minDistance;
    private DecimalPosition viewFieldCenter;

    public SyncBaseItemSetPositionMonitor(Set<Integer> itemTypeFilter, Runnable releaseCallback) {
        super(releaseCallback);
        this.itemTypeFilter = itemTypeFilter;
    }

    @Override
    public boolean hasInViewPositions() {
        return !inViewVertices.isEmpty();
    }

    @Override
    public List<Vertex> getInViewPosition3d() {
        return inViewVertices;
    }

    @Override
    public DecimalPosition getNearestOutOfViewPosition2d() {
        return nearestOutOfViewPosition;
    }

    public void init(DecimalPosition viewFieldCenter) {
        this.viewFieldCenter = viewFieldCenter;
        inViewVertices.clear();
        nearestOutOfViewPosition = null;
        minDistance = Double.MAX_VALUE;
    }

    public void inViewAabb(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
        if (!isAllowed(baseItemType)) {
            return;
        }
        inViewVertices.add(syncBaseItem.getPosition3d());
    }

    public void notInViewAabb(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
        if (!isAllowed(baseItemType)) {
            return;
        }
        double distance = syncBaseItem.getPosition2d().getDistance(viewFieldCenter);
        if (distance < minDistance) {
            minDistance = distance;
            nearestOutOfViewPosition = syncBaseItem.getPosition2d();
        }
    }

    public void setItemTypeFilter(Set<Integer> itemTypeFilter) {
        this.itemTypeFilter = itemTypeFilter;
    }

    private boolean isAllowed(BaseItemType baseItemType) {
        return itemTypeFilter == null || itemTypeFilter.contains(baseItemType.getId());
    }
}
