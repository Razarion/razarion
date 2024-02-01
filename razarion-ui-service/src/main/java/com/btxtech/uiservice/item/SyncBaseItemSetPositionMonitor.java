package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
import com.btxtech.uiservice.renderer.BabylonItem;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;

import java.util.Set;

/**
 * Created by Beat
 * on 13.09.2017.
 */
public class SyncBaseItemSetPositionMonitor extends AbstractSyncItemSetPositionMonitor {
    private Set<Integer> itemTypeFilter;
    private final Set<Integer> botIdFilter;

    public SyncBaseItemSetPositionMonitor(BabylonRendererService babylonRendererService, MarkerConfig markerConfig, Set<Integer> itemTypeFilter, Set<Integer> botIdFilter, Runnable releaseCallback) {
        super(babylonRendererService, markerConfig, releaseCallback);
        this.itemTypeFilter = itemTypeFilter;
        this.botIdFilter = botIdFilter;
    }

    @Override
    public void addVisible(BabylonItem babylonItem) {
        BabylonBaseItem babylonBaseItem = (BabylonBaseItem) babylonItem;
        if (itemTypeFilter != null && !itemTypeFilter.contains(babylonBaseItem.getBaseItemType().getId())) {
            return;
        }
        super.addVisible(babylonBaseItem);
    }

    public void setItemTypeFilter(Set<Integer> itemTypeFilter) {
        this.itemTypeFilter = itemTypeFilter;
        if(itemTypeFilter != null) {
            check4Visible(babylonItem -> this.itemTypeFilter.contains(((BabylonBaseItem) babylonItem).getBaseItemType().getId()));
        }
    }

    public void setInvisibleSyncBaseItemTickInfo(DecimalPosition position, BaseItemType baseItemType, DecimalPosition viewFiledCenter) {
        if (position != null) {
            if (itemTypeFilter != null && !itemTypeFilter.contains(baseItemType.getId())) {
                return;
            }
            setInvisible(position, viewFiledCenter);
        } else {
            setInvisible(null, null);
        }
    }
}
