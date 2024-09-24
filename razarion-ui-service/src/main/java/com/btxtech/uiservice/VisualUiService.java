package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.inject.Singleton;
import com.btxtech.client.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.08.2016.
 */
@Singleton
public class VisualUiService {

    private Event<PlanetVisualConfig> planetVisualConfigTrigger;
    private PlanetVisualConfig planetVisualConfig;

    @Inject
    public VisualUiService(Event<com.btxtech.shared.dto.PlanetVisualConfig> planetVisualConfigTrigger) {
        this.planetVisualConfigTrigger = planetVisualConfigTrigger;
    }

    public void onGameUiControlInitEvent( GameUiControlInitEvent gameUiControlInitEvent) {
        planetVisualConfig = gameUiControlInitEvent.getColdGameUiContext().getWarmGameUiContext().getPlanetVisualConfig();
        planetVisualConfigTrigger.fire(planetVisualConfig);
    }

    public PlanetVisualConfig getPlanetVisualConfig() {
        return planetVisualConfig;
    }

    public Vertex getLightDirection() {
        return planetVisualConfig.getLightDirection();
    }

    public Color getAmbient() {
        return planetVisualConfig.getAmbient();
    }

    public Color getDiffuse() {
        return planetVisualConfig.getDiffuse();
    }
}
