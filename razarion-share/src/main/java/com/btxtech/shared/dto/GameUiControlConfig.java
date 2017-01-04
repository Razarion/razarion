package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
// Better name: something with game-control, client control
public class GameUiControlConfig {
    private List<SceneConfig> sceneConfigs;
    private GameEngineConfig gameEngineConfig;
    private VisualConfig visualConfig;
    private AudioConfig audioConfig;
    private UserContext userContext;
    private GameTipVisualConfig gameTipVisualConfig;

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public GameUiControlConfig setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
        return this;
    }

    public GameEngineConfig getGameEngineConfig() {
        return gameEngineConfig;
    }

    public GameUiControlConfig setGameEngineConfig(GameEngineConfig gameEngineConfig) {
        this.gameEngineConfig = gameEngineConfig;
        return this;
    }

    public VisualConfig getVisualConfig() {
        return visualConfig;
    }

    public GameUiControlConfig setVisualConfig(VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        return this;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public GameUiControlConfig setAudioConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
        return this;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public GameUiControlConfig setUserContext(UserContext userContext) {
        this.userContext = userContext;
        return this;
    }

    public GameTipVisualConfig getGameTipVisualConfig() {
        return gameTipVisualConfig;
    }

    public GameUiControlConfig setGameTipVisualConfig(GameTipVisualConfig gameTipVisualConfig) {
        this.gameTipVisualConfig = gameTipVisualConfig;
        return this;
    }
}
