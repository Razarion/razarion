package com.btxtech.shared.datatypes;

import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import java.util.List;

/**
 * Created by Beat
 * on 22.09.2017.
 */
public class LevelUpPacket {
    private UserContext userContext;
    private List<LevelUnlockConfig> levelUnlockConfigs;

    public UserContext getUserContext() {
        return userContext;
    }

    public LevelUpPacket setUserContext(UserContext userContext) {
        this.userContext = userContext;
        return this;
    }

    public List<LevelUnlockConfig> getLevelUnlockConfigs() {
        return levelUnlockConfigs;
    }

    public LevelUpPacket setLevelUnlockConfigs(List<LevelUnlockConfig> levelUnlockConfigs) {
        this.levelUnlockConfigs = levelUnlockConfigs;
        return this;
    }
}
