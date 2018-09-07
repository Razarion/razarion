package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

/**
 * Created by Beat
 * 13.07.2016.
 */
public class PlanetActivationEvent {
    public enum Type {
        INITIALIZE,
        STOP
    }

    private PlanetConfig planetConfig;
    private GameEngineMode gameEngineMode;
    private MasterPlanetConfig masterPlanetConfig;
    private Type type;

    public PlanetActivationEvent(PlanetConfig planetConfig, GameEngineMode gameEngineMode, MasterPlanetConfig masterPlanetConfig, Type type) {
        this.planetConfig = planetConfig;
        this.gameEngineMode = gameEngineMode;
        this.masterPlanetConfig = masterPlanetConfig;
        this.type = type;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public MasterPlanetConfig getMasterPlanetConfig() {
        return masterPlanetConfig;
    }

    public Type getType() {
        return type;
    }
}
