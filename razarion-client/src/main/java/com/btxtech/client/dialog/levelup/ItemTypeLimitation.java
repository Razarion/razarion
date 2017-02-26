package com.btxtech.client.dialog.levelup;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * Created by Beat
 * 26.02.2017.
 */
public class ItemTypeLimitation {
    private Integer imageId;
    private int count;
    private String name;

    public ItemTypeLimitation(BaseItemType baseItemType, int count) {
        imageId =  baseItemType.getThumbnail();
        name = baseItemType.getName();
        this.count = count;
    }

    public Integer getImageId() {
        return imageId;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
