package com.btxtech.server.rest;

import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlController;
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
public class GameUiControlControllerImpl implements GameUiControlController {
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;

    @Override
    @Transactional
    public ColdGameUiControlConfig loadGameUiControlConfig(GameUiControlInput gameUiControlInput) {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.load(gameUiControlInput, sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public WarmGameUiControlConfig loadWarmGameUiControlConfig() {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.loadWarm(sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}