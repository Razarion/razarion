package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.GameEngineConfigPersistence;
import com.btxtech.shared.gameengine.GameEngineInitEvent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class GameEngineService {
    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;
    @Inject
    private PlanetService planetService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameEngineConfigPersistence gameEngineConfigPersistence;

    public void start() {
        GameEngineConfig gameEngineConfig = gameEngineConfigPersistence.load4Server();
        gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
        planetService.initialise(gameEngineConfig.getPlanetConfig());
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }

}
