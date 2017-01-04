package com.btxtech.server.rest;

import com.btxtech.servercommon.GameUiControlConfigPersistence;
import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class GameUiControlProviderImpl implements GameUiControlProvider {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public GameUiControlConfig loadGameUiControlConfig() {
        try {
            return gameUiControlConfigPersistence.load();
        } catch (ParserConfigurationException | ColladaException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
