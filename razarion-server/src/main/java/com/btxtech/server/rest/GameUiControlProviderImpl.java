package com.btxtech.server.rest;

import com.btxtech.server.persistence.impl.GameUiControlConfigPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FacebookUserLoginInfo;
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
    @Inject
    private UserService userService;

    @Override
    @Transactional
    public GameUiControlConfig loadGameUiControlConfig(FacebookUserLoginInfo facebookUserLoginInfo) {
        try {
            UserContext userContext = userService.handleUserLoginInfo(facebookUserLoginInfo);
            GameUiControlConfig gameUiControlConfig = gameUiControlConfigPersistence.load(userContext);
            return gameUiControlConfig;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
