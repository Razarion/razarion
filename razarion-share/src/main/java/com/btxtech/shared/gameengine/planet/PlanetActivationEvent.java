package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

/**
 * Created by Beat
 * 13.07.2016.
 */
public class PlanetActivationEvent {
    private PlanetConfig planetConfig;

    public PlanetActivationEvent(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }
}
