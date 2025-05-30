package com.btxtech.uiservice.item;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class SyncBaseItemMonitor extends SyncItemMonitor {
    private Consumer<SyncItemMonitor> healthChangeListener;
    private Consumer<SyncItemMonitor> constructingChangeListener;
    private Consumer<SyncItemMonitor> containedChangeListener;

    public SyncBaseItemMonitor(SyncBaseItemState syncBaseItemState) {
        super(syncBaseItemState);
    }

    public double getHealth() {
        return ((SyncBaseItemState) getSyncItemState()).getHealth();
    }

    public double getConstructing() {
        return ((SyncBaseItemState) getSyncItemState()).getConstructing();
    }

    public Integer getConstructingBaseItemTypeId() {
        return ((SyncBaseItemState) getSyncItemState()).getConstructingBaseItemTypeId();
    }

    public boolean checkConstructing() {
        return ((SyncBaseItemState) getSyncItemState()).checkConstructing();
    }

    public void setHealthChangeListener(Consumer<SyncItemMonitor> healthChangeListener) {
        this.healthChangeListener = healthChangeListener;
    }

    public void setConstructingChangeListener(Consumer<SyncItemMonitor> constructingChangeListener) {
        this.constructingChangeListener = constructingChangeListener;
    }

    public void setContainedChangeListener(Consumer<SyncItemMonitor> containedChangeListener) {
        this.containedChangeListener = containedChangeListener;
    }

    public void onHealthChanged() {
        if (healthChangeListener != null) {
            healthChangeListener.accept(this);
        }
    }

    public void onConstructingChanged() {
        if (constructingChangeListener != null) {
            constructingChangeListener.accept(this);
        }
    }

    public void onContainedChanged() {
        if (containedChangeListener != null) {
            containedChangeListener.accept(this);
        }
    }

    public SyncBaseItemState getSyncBaseItemState() {
        return (SyncBaseItemState) getSyncItemState();
    }

}
