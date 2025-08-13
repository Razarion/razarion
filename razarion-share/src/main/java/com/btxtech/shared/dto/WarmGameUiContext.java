package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2017.
 */
public class WarmGameUiContext {
    private int gameUiControlConfigId;
    private GameEngineMode gameEngineMode;
    private boolean availableUnlocks;
    private SlavePlanetConfig slavePlanetConfig;
    private SlaveQuestInfo slaveQuestInfo;
    private PlanetConfig planetConfig;
    private List<SceneConfig> sceneConfigs;
    private boolean detailedTracking;

    public int getGameUiControlConfigId() {
        return gameUiControlConfigId;
    }

    public void setGameUiControlConfigId(int gameUiControlConfigId) {
        this.gameUiControlConfigId = gameUiControlConfigId;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public void setGameEngineMode(GameEngineMode gameEngineMode) {
        this.gameEngineMode = gameEngineMode;
    }

    public boolean isAvailableUnlocks() {
        return availableUnlocks;
    }

    public void setAvailableUnlocks(boolean availableUnlocks) {
        this.availableUnlocks = availableUnlocks;
    }

    public SlavePlanetConfig getSlavePlanetConfig() {
        return slavePlanetConfig;
    }

    public void setSlavePlanetConfig(SlavePlanetConfig slavePlanetConfig) {
        this.slavePlanetConfig = slavePlanetConfig;
    }

    public SlaveQuestInfo getSlaveQuestInfo() {
        return slaveQuestInfo;
    }

    public void setSlaveQuestInfo(SlaveQuestInfo slaveQuestInfo) {
        this.slaveQuestInfo = slaveQuestInfo;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public void setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
    }

    public boolean isDetailedTracking() {
        return detailedTracking;
    }

    public void setDetailedTracking(boolean detailedTracking) {
        this.detailedTracking = detailedTracking;
    }

    public WarmGameUiContext gameUiControlConfigId(int gameUiControlConfigId) {
        setGameUiControlConfigId(gameUiControlConfigId);
        return this;
    }

    public WarmGameUiContext gameEngineMode(GameEngineMode gameEngineMode) {
        setGameEngineMode(gameEngineMode);
        return this;
    }

    public WarmGameUiContext availableUnlocks(boolean availableUnlocks) {
        setAvailableUnlocks(availableUnlocks);
        return this;
    }

    public WarmGameUiContext slavePlanetConfig(SlavePlanetConfig slavePlanetConfig) {
        setSlavePlanetConfig(slavePlanetConfig);
        return this;
    }

    public WarmGameUiContext slaveQuestInfo(SlaveQuestInfo slaveQuestInfo) {
        setSlaveQuestInfo(slaveQuestInfo);
        return this;
    }

    public WarmGameUiContext planetConfig(PlanetConfig planetConfig) {
        setPlanetConfig(planetConfig);
        return this;
    }

    public WarmGameUiContext sceneConfigs(List<SceneConfig> sceneConfigs) {
        setSceneConfigs(sceneConfigs);
        return this;
    }

    public WarmGameUiContext detailedTracking(boolean detailedTracking) {
        setDetailedTracking(detailedTracking);
        return this;
    }
}
