package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class StoryboardConfig {
    private List<SceneConfig> sceneConfigs;
    private GameEngineConfig gameEngineConfig;

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public StoryboardConfig setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
        return this;
    }

    public GameEngineConfig getGameEngineConfig() {
        return gameEngineConfig;
    }

    public StoryboardConfig setGameEngineConfig(GameEngineConfig gameEngineConfig) {
        this.gameEngineConfig = gameEngineConfig;
        return this;
    }
}
