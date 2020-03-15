package com.btxtech.server.rest;

import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
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
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput) {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.loadCold(gameUiControlInput, sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            exceptionHandler.handleException("Using fallback. No ColdGameUiContext configured", e);
            return FallbackConfig.coldGameUiControlConfig(userService.getUserContextFromSession());
        }
    }

    @Override
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.loadWarm(sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            exceptionHandler.handleException("Using fallback. No WarmGameUiContext configured", e);
            return FallbackConfig.warmGameUiControlConfig();
        }
    }
}
