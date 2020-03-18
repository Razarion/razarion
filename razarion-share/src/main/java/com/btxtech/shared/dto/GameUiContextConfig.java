package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;

public class GameUiContextConfig implements Config {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private Integer planetId;
    private GameEngineMode gameEngineMode;

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
}
