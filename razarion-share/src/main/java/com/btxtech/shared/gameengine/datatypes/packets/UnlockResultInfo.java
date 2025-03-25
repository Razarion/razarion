package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import java.util.List;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class UnlockResultInfo {
    private boolean notEnoughCrystals;
    private List<LevelUnlockConfig> availableUnlocks;

    public boolean isNotEnoughCrystals() {
        return notEnoughCrystals;
    }

    public UnlockResultInfo setNotEnoughCrystals(boolean notEnoughCrystals) {
        this.notEnoughCrystals = notEnoughCrystals;
        return this;
    }

    public List<LevelUnlockConfig> getAvailableUnlocks() {
        return availableUnlocks;
    }

    public UnlockResultInfo setAvailableUnlocks(List<LevelUnlockConfig> availableUnlocks) {
        this.availableUnlocks = availableUnlocks;
        return this;
    }
}
