package com.btxtech.uiservice;

import com.btxtech.shared.dto.PlanetVisualConfig;
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
        planetVisualConfig = gameUiControlInitEvent.getColdGameUiControlConfig().getWarmGameUiControlConfig().getPlanetVisualConfig();
        planetVisualConfigTrigger.fire(planetVisualConfig);
    }

    public PlanetVisualConfig getPlanetVisualConfig() {
        return planetVisualConfig;
    }
}
