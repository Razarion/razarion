package com.btxtech.server.rest.ui;

import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.btxtech.shared.rest.GameUiContextAccess.PATH;


/**
 * Created by Beat
 * 06.07.2016.
 */
@RestController
@RequestMapping("/rest" + PATH)
public class GameUiContextController implements GameUiContextAccess {
    private final Logger logger = LoggerFactory.getLogger(GameUiContextController.class);
    private final GameUiContextService gameUiContextService;
    private final UserService userService;

    public GameUiContextController(GameUiContextService gameUiContextService, UserService userService) {
        this.gameUiContextService = gameUiContextService;
        this.userService = userService;
    }

    @Override
    @PostMapping(value = CommonUrl.COLD, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ColdGameUiContext loadColdGameUiContext() {
        UserContext userContext = userService.getUserContextFromContext();
        try {
            return gameUiContextService.loadCold(userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return new ColdGameUiContext().userContext(userContext);
        }
    }

    @Override
    @GetMapping(value = CommonUrl.WARM, produces = MediaType.APPLICATION_JSON_VALUE)
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.getUserContextFromContext();
            return gameUiContextService.loadWarm(userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }
}
