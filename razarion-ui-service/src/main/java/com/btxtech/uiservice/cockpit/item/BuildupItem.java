package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 30.09.2016.
 */
public class BuildupItem {
    private BaseItemType itemType;
    private Runnable callback;
    private Consumer<Double> constructingConsumer;

    /**
     * @param itemType to be build type
     * @param callback where to place the ToBeBuildPlacer
     */
    BuildupItem(BaseItemType itemType, Runnable callback) {
        this.itemType = itemType;
        this.callback = callback;
    }

    public BaseItemType getItemType() {
        return itemType;
    }

    public void setConstructingConsumer(Consumer<Double> constructingConsumer) {
        this.constructingConsumer = constructingConsumer;
    }

    public void onBuild() {
        callback.run();
    }

    public void setConstructing(Double constructing) {
        if(constructingConsumer != null) {
            constructingConsumer.accept(constructing);
        }
    }
}
