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

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

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
    private Logger logger;

    @Override
    @Transactional
    public ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput) {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.load(gameUiControlInput, sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            logger.severe("Using ColdGameUiContext. No planets configured");
            return FallbackConfig.coldGameUiControlConfig(userService.getUserContextFromSession());
        }
    }

    @Override
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.loadWarm(sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            logger.severe("Using Fallback. No WarmGameUiContext configured");
            return FallbackConfig.warmGameUiControlConfig();
        }
    }
}
