package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * 25.07.2016.
 */
public class SpawnItemType extends ItemType {
    private double duration;  // Spawn duration in seconds

    public double getDuration() {
        return duration;
    }

    public SpawnItemType setDuration(double duration) {
        this.duration = duration;
        return this;
    }
}
