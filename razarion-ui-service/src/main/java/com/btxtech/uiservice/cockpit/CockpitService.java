package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 16.11.2016.
 */
@ApplicationScoped
public class CockpitService implements PlanetTickListener {
    @Inject
    private PlanetService planetService;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private Instance<SideCockpit> sideCockpitInstance;
    private SideCockpit sideCockpit;

    @PostConstruct
    public void postConstruct() {
        planetService.addTickListener(this);
        sideCockpit = sideCockpitInstance.get();
        sideCockpit.show();
    }

    public void init() {
        // TODO set planet info
    }

    @Override
    public void onTick() {
        sideCockpit.displayResources(storyboardService.getResources());
    }
}
