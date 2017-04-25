package com.btxtech.server.rest;

import com.btxtech.server.gameengine.GameEngineService;
import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetProvider;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class PlanetProviderImpl implements PlanetProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private GameEngineService gameEngineService;

    @Override
    public PlanetConfig loadWarmPlanetConfig() {
        try {
            PlanetConfig planetConfig = gameUiControlConfigPersistence.load(sessionHolder.getPlayerSession().getUserContext()).getGameEngineConfig().getPlanetConfig();
            if (planetConfig.getGameEngineMode() == GameEngineMode.SLAVE) {
                gameEngineService.fillSyncItems(planetConfig, sessionHolder.getPlayerSession().getUserContext());
            }
            return planetConfig;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}