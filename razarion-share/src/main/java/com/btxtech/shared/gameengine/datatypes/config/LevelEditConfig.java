package com.btxtech.shared.gameengine.datatypes.config;

import java.util.List;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class LevelEditConfig extends LevelConfig {
    private List<LevelUnlockConfig> levelUnlockConfigs;

    public List<LevelUnlockConfig> getLevelUnlockConfigs() {
        return levelUnlockConfigs;
    }

    public void setLevelUnlockConfigs(List<LevelUnlockConfig> levelUnlockConfigs) {
        this.levelUnlockConfigs = levelUnlockConfigs;
    }

    public LevelEditConfig levelUnlockConfigs(List<LevelUnlockConfig> levelUnlockConfigs) {
        setLevelUnlockConfigs(levelUnlockConfigs);
        return this;
    }
}
