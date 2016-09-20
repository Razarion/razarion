package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.planet.PlanetService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.07.2016.
 */
@ApplicationScoped
public class GameEngine {
    @Inject
    private PlanetService planetService;

    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;

    public void initialise(GameEngineConfig gameEngineConfig) {
        gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
        planetService.initialise(gameEngineConfig.getPlanetConfig());
    }

    public void start() {
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }
}
