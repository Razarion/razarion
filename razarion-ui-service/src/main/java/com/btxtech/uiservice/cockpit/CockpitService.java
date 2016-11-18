package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
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
    private LevelService levelService;
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
        UserContext userContext = storyboardService.getUserContext();
        sideCockpit.displayXps(userContext.getXp());
        sideCockpit.displayLevel(levelService.getLevel(userContext.getLevelId()).getNumber());
    }
}
