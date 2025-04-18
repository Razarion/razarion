package com.btxtech.server.rest.ui;

import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


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

    public static String getCurrentHttpSessionId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return "no ServletRequestAttributes";
        }

        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession(true);
        return (session != null) ? session.getId() : "no HttpSession";
    }

    @Override
    @PostMapping(value = CommonUrl.COLD, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ColdGameUiContext loadColdGameUiContext(GameUiControlInput gameUiControlInput) {
        UserContext userContext = userService.getUserContext(getCurrentHttpSessionId());
        try {
            return gameUiContextService.loadCold(gameUiControlInput, userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return new ColdGameUiContext().userContext(userContext);
        }
    }

    @Override
    @GetMapping(value = CommonUrl.WARM, produces = MediaType.APPLICATION_JSON_VALUE)
    public WarmGameUiContext loadWarmGameUiContext() {
        try {
            UserContext userContext = userService.getUserContext(getCurrentHttpSessionId());
            return gameUiContextService.loadWarm(userContext);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }
}
