package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * Created by Beat
 * 10.02.2017.
 */
public class WreckageItem {
    private final BaseItemType baseItemType;
    private final long visibleTillTimeStamp;

    public WreckageItem(BaseItemType baseItemType, Vertex position) {
        visibleTillTimeStamp = System.currentTimeMillis() + TrailService.VISIBLE_WRECKAGE_MILLIS;
        this.baseItemType = baseItemType;
    }

    public boolean isExpired() {
        return visibleTillTimeStamp < System.currentTimeMillis();
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }
}
