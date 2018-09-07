package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2017.
 */
public class WarmGameUiControlConfig {
    private int gameUiControlConfigId;
    private GameEngineMode gameEngineMode;
    private SlavePlanetConfig slavePlanetConfig;
    private SlaveQuestInfo slaveQuestInfo;
    private List<BotSceneIndicationInfo> botSceneIndicationInfos;
    private PlanetConfig planetConfig;
    private List<SceneConfig> sceneConfigs;
    private PlanetVisualConfig planetVisualConfig;
    private boolean detailedTracking;
    private PlaybackGameUiControlConfig playbackGameUiControlConfig;

    public int getGameUiControlConfigId() {
        return gameUiControlConfigId;
    }

    public WarmGameUiControlConfig setGameUiControlConfigId(int gameUiControlConfigId) {
        this.gameUiControlConfigId = gameUiControlConfigId;
        return this;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public WarmGameUiControlConfig setGameEngineMode(GameEngineMode gameEngineMode) {
        this.gameEngineMode = gameEngineMode;
        return this;
    }

    public SlavePlanetConfig getSlavePlanetConfig() {
        return slavePlanetConfig;
    }

    public WarmGameUiControlConfig setSlavePlanetConfig(SlavePlanetConfig slavePlanetConfig) {
        this.slavePlanetConfig = slavePlanetConfig;
        return this;
    }

    public SlaveQuestInfo getSlaveQuestInfo() {
        return slaveQuestInfo;
    }

    public WarmGameUiControlConfig setSlaveQuestInfo(SlaveQuestInfo slaveQuestInfo) {
        this.slaveQuestInfo = slaveQuestInfo;
        return this;
    }

    public List<BotSceneIndicationInfo> getBotSceneIndicationInfos() {
        return botSceneIndicationInfos;
    }

    public WarmGameUiControlConfig setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        this.botSceneIndicationInfos = botSceneIndicationInfos;
        return this;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public WarmGameUiControlConfig setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        return this;
    }

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public WarmGameUiControlConfig setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
        return this;
    }

    public PlanetVisualConfig getPlanetVisualConfig() {
        return planetVisualConfig;
    }

    public WarmGameUiControlConfig setPlanetVisualConfig(PlanetVisualConfig planetVisualConfig) {
        this.planetVisualConfig = planetVisualConfig;
        return this;
    }

    public boolean isDetailedTracking() {
        return detailedTracking;
    }

    public WarmGameUiControlConfig setDetailedTracking(boolean detailedTracking) {
        this.detailedTracking = detailedTracking;
        return this;
    }

    public PlaybackGameUiControlConfig getPlaybackGameUiControlConfig() {
        return playbackGameUiControlConfig;
    }

    public WarmGameUiControlConfig setPlaybackGameUiControlConfig(PlaybackGameUiControlConfig playbackGameUiControlConfig) {
        this.playbackGameUiControlConfig = playbackGameUiControlConfig;
        return this;
    }
}
