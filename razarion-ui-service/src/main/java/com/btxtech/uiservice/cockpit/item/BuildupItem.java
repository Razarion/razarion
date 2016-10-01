package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 30.09.2016.
 */
public class BuildupItem {
    private final BaseItemType itemType;
    private final Consumer<DecimalPosition> callback;

    /**
     *
     * @param itemType to be build type
     * @param callback where to place the ToBeBuildPlacer
     */
    public BuildupItem(BaseItemType itemType, Consumer<DecimalPosition> callback) {

        this.itemType = itemType;
        this.callback = callback;
    }

    void onMoneyChanged(double accountBalance) {
        throw new UnsupportedOperationException();
    }

    public BaseItemType getItemType() {
        return itemType;
    }

    public void onBuild(DecimalPosition position) {
        callback.accept(position);
    }
}
