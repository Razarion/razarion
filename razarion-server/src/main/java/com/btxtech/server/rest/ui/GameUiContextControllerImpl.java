package com.btxtech.server.rest.ui;

import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 06.07.2016.
 */
@RestController
@RequestMapping("/rest/gz/game-ui-context-control")
public class GameUiContextControllerImpl implements GameUiContextController {
    private final Logger logger = LoggerFactory.getLogger(GameUiContextControllerImpl.class);
    private final GameUiContextService gameUiContextService;
    private final UserService userService;

    public GameUiContextControllerImpl(GameUiContextService gameUiContextService, UserService userService) {
        this.gameUiContextService = gameUiContextService;
        this.userService = userService;
    }

    @Override
    @PostMapping(value = CommonUrl.COLD, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput) {
        UserContext userContext = userService.createUserContext();
        try {
            return gameUiContextService.loadCold(gameUiControlInput, userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return new ColdGameUiContext().userContext(userContext);
        }
    }

    @Override
    @GetMapping(value = CommonUrl.WARM, produces = MediaType.APPLICATION_JSON)
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.createUserContext();
            return gameUiContextService.loadWarm(userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }
}
