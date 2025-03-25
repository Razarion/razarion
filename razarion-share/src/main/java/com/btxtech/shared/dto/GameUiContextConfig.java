package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameUiContextConfig implements Config {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private Integer planetId;
    private GameEngineMode gameEngineMode;
    private boolean detailedTracking;
    private List<SceneConfig> scenes;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public void setMinimalLevelId(Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
    }

    public Integer getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Integer planetId) {
        this.planetId = planetId;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public void setGameEngineMode(GameEngineMode gameEngineMode) {
        this.gameEngineMode = gameEngineMode;
    }

    public boolean isDetailedTracking() {
        return detailedTracking;
    }

    public void setDetailedTracking(boolean detailedTracking) {
        this.detailedTracking = detailedTracking;
    }

    public List<SceneConfig> getScenes() {
        return scenes;
    }

    public void setScenes(List<SceneConfig> scenes) {
        this.scenes = scenes;
    }

    public GameUiContextConfig id(int id) {
        this.id = id;
        return this;
    }

    public GameUiContextConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public GameUiContextConfig minimalLevel(Integer minimalLevel) {
        setMinimalLevelId(minimalLevel);
        return this;
    }

    public GameUiContextConfig planetId(Integer planetId) {
        setPlanetId(planetId);
        return this;
    }

    public GameUiContextConfig gameEngineMode(GameEngineMode gameEngineMode) {
        setGameEngineMode(gameEngineMode);
        return this;
    }

    public GameUiContextConfig detailedTracking(boolean detailedTracking) {
        setDetailedTracking(detailedTracking);
        return this;
    }

    public GameUiContextConfig scenes(List<SceneConfig> scenes) {
        setScenes(scenes);
        return this;
    }

    public Map<String, Class> metaListTypes() {
        return Collections.singletonMap("scenes", SceneConfig.class);
    }
}
