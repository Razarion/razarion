package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
// Better name: something with game-control, client control
public class StoryboardConfig {
    private List<SceneConfig> sceneConfigs;
    private GameEngineConfig gameEngineConfig;
    private VisualConfig visualConfig;
    private UserContext userContext;

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

    public VisualConfig getVisualConfig() {
        return visualConfig;
    }

    public StoryboardConfig setVisualConfig(VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        return this;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public StoryboardConfig setUserContext(UserContext userContext) {
        this.userContext = userContext;
        return this;
    }
}
