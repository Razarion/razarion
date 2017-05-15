package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
// Better name: something with game-control, client control
public class GameUiControlConfig {
    // User
    private UserContext userContext;
    // Static
    private StaticGameConfig staticGameConfig;
    private List<Shape3D> shape3Ds;
    private AudioConfig audioConfig;
    private GameTipVisualConfig gameTipVisualConfig;
    // Planet
    private List<SceneConfig> sceneConfigs;
    private PlanetConfig planetConfig;
    private PlanetVisualConfig planetVisualConfig;
    private SlavePlanetConfig slavePlanetConfig;
    private SlaveSyncItemInfo slaveSyncItemInfo;

    public List<SceneConfig> getSceneConfigs() {
        return sceneConfigs;
    }

    public GameUiControlConfig setSceneConfigs(List<SceneConfig> sceneConfigs) {
        this.sceneConfigs = sceneConfigs;
        return this;
    }

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }

    public GameUiControlConfig setStaticGameConfig(StaticGameConfig staticGameConfig) {
        this.staticGameConfig = staticGameConfig;
        return this;
    }

    public List<Shape3D> getShape3Ds() {
        return shape3Ds;
    }

    public GameUiControlConfig setShape3Ds(List<Shape3D> shape3Ds) {
        this.shape3Ds = shape3Ds;
        return this;
    }

    public PlanetVisualConfig getPlanetVisualConfig() {
        return planetVisualConfig;
    }

    public GameUiControlConfig setPlanetVisualConfig(PlanetVisualConfig planetVisualConfig) {
        this.planetVisualConfig = planetVisualConfig;
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

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public GameUiControlConfig setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        return this;
    }

    public SlavePlanetConfig getSlavePlanetConfig() {
        return slavePlanetConfig;
    }

    public GameUiControlConfig setSlavePlanetConfig(SlavePlanetConfig slavePlanetConfig) {
        this.slavePlanetConfig = slavePlanetConfig;
        return this;
    }

    public SlaveSyncItemInfo getSlaveSyncItemInfo() {
        return slaveSyncItemInfo;
    }

    public GameUiControlConfig setSlaveSyncItemInfo(SlaveSyncItemInfo slaveSyncItemInfo) {
        this.slaveSyncItemInfo = slaveSyncItemInfo;
        return this;
    }
}
