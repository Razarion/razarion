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
    private BaseItemUiService baseItemUiService;
    private Set<Integer> itemTypeFilter;
    private Set<Integer> botIdFilter;
    private List<Vertex> inViewVertices = new ArrayList<>();
    private DecimalPosition nearestOutOfViewPosition;
    private double minDistance;
    private DecimalPosition viewFieldCenter;

    public SyncBaseItemSetPositionMonitor(BaseItemUiService baseItemUiService, Set<Integer> itemTypeFilter, Set<Integer> botIdFilter, Runnable releaseCallback) {
        super(releaseCallback);
        this.baseItemUiService = baseItemUiService;
        this.itemTypeFilter = itemTypeFilter;
        this.botIdFilter = botIdFilter;
    }

    public void init(DecimalPosition viewFieldCenter) {
        this.viewFieldCenter = viewFieldCenter;
        inViewVertices.clear();
        nearestOutOfViewPosition = null;
        minDistance = Double.MAX_VALUE;
    }

    public void inViewAabb(int baseId, Vertex position, BaseItemType baseItemType) {
        if (!isAllowed(baseId, baseItemType)) {
            return;
        }
        inViewVertices.add(position);
    }

    public void notInViewAabb(int baseId, DecimalPosition position, BaseItemType baseItemType) {
        if (!isAllowed(baseId, baseItemType)) {
            return;
        }
        double distance = position.getDistance(viewFieldCenter);
        if (distance < minDistance) {
            minDistance = distance;
            nearestOutOfViewPosition = position;
        }
    }

    private boolean isAllowed(int baseId, BaseItemType baseItemType) {
        if (itemTypeFilter != null) {
            if (!itemTypeFilter.contains(baseItemType.getId())) {
                return false;
            }
        }
        if (botIdFilter != null) {
            Integer botId = baseItemUiService.getBase(baseId).getBotId();
            if (botId == null) {
                return false;
            }
            if (!botIdFilter.contains(botId)) {
                return false;
            }
        }
        return true;
    }
}
