package com.btxtech.server.rest;

import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlController;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class GameUiControlControllerImpl implements GameUiControlController {
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
    public ColdGameUiControlConfig loadGameUiControlConfig(GameUiControlInput gameUiControlInput) {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.load(gameUiControlInput, sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            logger.severe("Using ColdGameUiControlConfig. No planets configured");
            return FallbackConfig.coldGameUiControlConfig();
        }
    }

    @Override
    public WarmGameUiControlConfig loadWarmGameUiControlConfig() {
        try {
            UserContext userContext = userService.getUserContextFromSession();
            return gameUiControlConfigPersistence.loadWarm(sessionHolder.getPlayerSession().getLocale(), userContext);
        } catch (Throwable e) {
            logger.severe("Using Fallback. No WarmGameUiControlConfig configured");
            return FallbackConfig.warmGameUiControlConfig();
        }
    }
}
