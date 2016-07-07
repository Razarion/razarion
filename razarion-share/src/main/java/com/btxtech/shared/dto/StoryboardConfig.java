package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class StoryboardConfig {
    private List<SceneConfig> sceneConfigs;
    private PlanetConfig planetConfig;

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public void setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }
}
