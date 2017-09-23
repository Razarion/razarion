package com.btxtech.uiservice.unlock;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 22.09.2017.
 */
public abstract class UnlockUiService {
    private List<LevelUnlockConfig> levelUnlockConfigs;

    protected abstract void unlockViaCrystalCall(LevelUnlockConfig levelUnlockConfig, Consumer<UnlockResultInfo> callback);

    public void setLevelUnlockConfigs(List<LevelUnlockConfig> levelUnlockConfigs) {
        this.levelUnlockConfigs = levelUnlockConfigs;
    }

    public List<LevelUnlockConfig> getLevelUnlockConfigs() {
        return levelUnlockConfigs;
    }

    public boolean hasItems2Unlock() {
        return levelUnlockConfigs != null && !levelUnlockConfigs.isEmpty();
    }

    public void unlockViaCrystal(LevelUnlockConfig levelUnlockConfig, Consumer<Boolean> successCallback) {
        unlockViaCrystalCall(levelUnlockConfig, unlockResultInfo -> {
            if (unlockResultInfo.isNotEnoughCrystals()) {
                successCallback.accept(false);
            } else {
                this.levelUnlockConfigs = unlockResultInfo.getAvailableUnlocks();
                successCallback.accept(true);
            }
        });
    }

}
