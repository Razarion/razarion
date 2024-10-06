package com.btxtech.server.rest;

import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class GameUiContextControllerImpl implements GameUiContextController {
    @Inject
    private GameUiContextCrudPersistence gameUiContextCrudPersistence;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput) {
        UserContext userContext = userService.getUserContextFromSession();
        try {
            return gameUiContextCrudPersistence.loadCold(gameUiControlInput, userContext);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            return new ColdGameUiContext().userContext(userContext);
        }
    }

    @Override
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiContextCrudPersistence.loadWarm(userContext);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            return null;
        }
    }
}
