package com.btxtech.server.rest;

import com.btxtech.server.gameengine.GameEngineService;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.WarmGameConfig;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.rest.WarmGameConfigProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class WarmGameConfigProviderImpl implements WarmGameConfigProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private GameEngineService gameEngineService;
    @Inject
    private UserService userService;
    @Inject
    private PlanetService planetService;
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Override
    public WarmGameConfig loadWarmGameConfigTask() {
        try {
            WarmGameConfig warmGameConfig = new WarmGameConfig();
            if (userService.isMultiplayer()) {
                warmGameConfig.setSlaveSyncItemInfo(gameEngineService.generateSlaveSyncItemInfo(sessionHolder.getPlayerSession().getUserContext()));
                warmGameConfig.setPlanetConfig(planetPersistence.readMultiplayerPlanetConfig());
                warmGameConfig.setSlavePlanetConfig(serverGameEnginePersistence.readSlavePlanetConfig());
            } else {
                warmGameConfig.setPlanetConfig(planetPersistence.readTutorialPlanetConfig());
            }
            return warmGameConfig;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}