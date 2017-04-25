package com.btxtech.shared.gameengine.planet;

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
    private Type type;

    public PlanetActivationEvent(PlanetConfig planetConfig, Type type) {
        this.planetConfig = planetConfig;
        this.type = type;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public Type getType() {
        return type;
    }
}
