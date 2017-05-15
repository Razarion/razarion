package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
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
        planetVisualConfig = gameUiControlInitEvent.getGameUiControlConfig().getPlanetVisualConfig();
        planetVisualConfigTrigger.fire(planetVisualConfig);
    }

    public PlanetVisualConfig getPlanetVisualConfig() {
        return planetVisualConfig;
    }
}
