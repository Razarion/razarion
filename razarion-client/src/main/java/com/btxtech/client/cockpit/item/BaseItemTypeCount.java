package com.btxtech.client.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Deprecated
public class BaseItemTypeCount {
    private BaseItemType baseItemType;
    private int count;

    public BaseItemTypeCount(BaseItemType baseItemType, int count) {
        this.baseItemType = baseItemType;
        this.count = count;
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }

    public int getCount() {
        return count;
    }
}
