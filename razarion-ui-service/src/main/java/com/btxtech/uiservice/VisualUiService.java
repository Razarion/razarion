package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.enterprise.context.ApplicationScoped;
import com.btxtech.client.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.08.2016.
 */
@ApplicationScoped
public class VisualUiService {
    @Inject
    private Event<PlanetVisualConfig> planetVisualConfigTrigger;
    private PlanetVisualConfig planetVisualConfig;

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
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
