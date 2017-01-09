package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * Created by Beat
 * 30.09.2016.
 */
public class BuildupItem {
    private final BaseItemType itemType;
    private final Runnable callback;

    /**
     * @param itemType to be build type
     * @param callback where to place the ToBeBuildPlacer
     */
    BuildupItem(BaseItemType itemType, Runnable callback) {
        this.itemType = itemType;
        this.callback = callback;
    }

    void onMoneyChanged(double accountBalance) {
        throw new UnsupportedOperationException();
    }

    public BaseItemType getItemType() {
        return itemType;
    }

    public void onBuild() {
        callback.run();
    }
}
