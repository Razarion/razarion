package com.btxtech.uiservice.cockpit;


import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ItemCockpitService {
    private boolean isActive = false;


    public boolean isActive() {
        return isActive;
    }

    public void onMoneyChanged(double accountBalance) {
        // TODO
    }

    public void onStateChanged() {
        // TODO
    }

    public void onTargetSelectionChanged(SyncItem selection) {
        // TODO
    }

    public void onSelectionCleared() {
    }

    public void onOwnSelectionChanged(Group selectedGroup) {
    }
}
