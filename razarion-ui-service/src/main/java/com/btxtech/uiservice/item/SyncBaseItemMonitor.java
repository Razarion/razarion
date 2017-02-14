package com.btxtech.uiservice.item;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class SyncBaseItemMonitor extends SyncItemMonitor {
    private Consumer<SyncItemMonitor> healthChangeListener;

    public SyncBaseItemMonitor(SyncBaseItemState syncBaseItemState) {
        super(syncBaseItemState);
    }

    public double getHealth() {
        return ((SyncBaseItemState) getSyncItemState()).getHealth();
    }

    public void setHealthChangeListener(Consumer<SyncItemMonitor> healthChangeListener) {
        this.healthChangeListener = healthChangeListener;
    }

    public void onHealthChanged() {
        if (healthChangeListener != null) {
            healthChangeListener.accept(this);
        }
    }

}
