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
    private Consumer<SyncItemMonitor> containingChangeListener;
    private Consumer<SyncItemMonitor> factoryQueueChangeListener;

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

    public int[] getFactoryBuildQueue() {
        return ((SyncBaseItemState) getSyncItemState()).getFactoryBuildQueue();
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

    public void setContainingChangeListener(Consumer<SyncItemMonitor> containingChangeListener) {
        this.containingChangeListener = containingChangeListener;
    }

    public void setFactoryQueueChangeListener(Consumer<SyncItemMonitor> factoryQueueChangeListener) {
        this.factoryQueueChangeListener = factoryQueueChangeListener;
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

    public void onContainingChanged() {
        if (containingChangeListener != null) {
            containingChangeListener.accept(this);
        }
    }

    public void onFactoryQueueChanged() {
        if (factoryQueueChangeListener != null) {
            factoryQueueChangeListener.accept(this);
        }
    }

    public SyncBaseItemState getSyncBaseItemState() {
        return (SyncBaseItemState) getSyncItemState();
    }

}
