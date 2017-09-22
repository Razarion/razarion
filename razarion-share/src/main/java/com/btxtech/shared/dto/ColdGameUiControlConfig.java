package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
// Better name: something with game-control, client control
public class ColdGameUiControlConfig {
    // User
    private UserContext userContext;
    private List<LevelUnlockConfig> levelUnlockConfigs;
    private StaticGameConfig staticGameConfig;
    private List<Shape3D> shape3Ds;
    private AudioConfig audioConfig;
    private GameTipVisualConfig gameTipVisualConfig;
    private InGameQuestVisualConfig inGameQuestVisualConfig;
    private WarmGameUiControlConfig warmGameUiControlConfig;

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }

    public ColdGameUiControlConfig setStaticGameConfig(StaticGameConfig staticGameConfig) {
        this.staticGameConfig = staticGameConfig;
        return this;
    }

    public List<Shape3D> getShape3Ds() {
        return shape3Ds;
    }

    public ColdGameUiControlConfig setShape3Ds(List<Shape3D> shape3Ds) {
        this.shape3Ds = shape3Ds;
        return this;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public ColdGameUiControlConfig setAudioConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
        return this;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public ColdGameUiControlConfig setUserContext(UserContext userContext) {
        this.userContext = userContext;
        return this;
    }

    public List<LevelUnlockConfig> getLevelUnlockConfigs() {
        return levelUnlockConfigs;
    }

    public ColdGameUiControlConfig setLevelUnlockConfigs(List<LevelUnlockConfig> levelUnlockConfigs) {
        this.levelUnlockConfigs = levelUnlockConfigs;
        return this;
    }

    public GameTipVisualConfig getGameTipVisualConfig() {
        return gameTipVisualConfig;
    }

    public ColdGameUiControlConfig setGameTipVisualConfig(GameTipVisualConfig gameTipVisualConfig) {
        this.gameTipVisualConfig = gameTipVisualConfig;
        return this;
    }

    public InGameQuestVisualConfig getInGameQuestVisualConfig() {
        return inGameQuestVisualConfig;
    }

    public ColdGameUiControlConfig setInGameQuestVisualConfig(InGameQuestVisualConfig inGameQuestVisualConfig) {
        this.inGameQuestVisualConfig = inGameQuestVisualConfig;
        return this;
    }

    public WarmGameUiControlConfig getWarmGameUiControlConfig() {
        return warmGameUiControlConfig;
    }

    public ColdGameUiControlConfig setWarmGameUiControlConfig(WarmGameUiControlConfig warmGameUiControlConfig) {
        this.warmGameUiControlConfig = warmGameUiControlConfig;
        return this;
    }
}
