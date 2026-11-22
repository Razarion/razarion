package com.btxtech.server.rest;

import com.btxtech.server.service.engine.StartPositionFinderService;
import com.btxtech.server.service.ui.GameUiContextService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The landing cell a new player chooses on the loading screen (index.html, arm land=pick).
 * <p>
 * The page shows the cells of the spawn path while the game loads, with how many players have a
 * base in each, and the player may tap one. The client sends the choice along with the cold
 * context - the request that fixes where the player lands - as ?landingCell= (see
 * LoadGameUiContextlTask and StartPositionFinderService#findFreePosition). So it counts exactly when
 * it came before that request, and the page stops offering it the moment the request goes out: a
 * late tap does not pretend to do something.
 * <p>
 * Why a choice at all: the loading time costs some 2 % of the waiting players per second, flat,
 * however the wait is dressed up - two passive loading screens were a draw (PROD 09-10/2026). This
 * tests whether something to do, that matters for the game, keeps them.
 * <p>
 * Open to everybody and asks for nothing: no user is created by looking at the cells.
 */
@RestController
@RequestMapping("/rest/landing")
public class LandingController {
    static final String PARAMETER = "landingCell";
    /** The counts change with every base placed; a few seconds stale is still the right picture. */
    private static final long CACHE_MILLIS = 3000;
    private static final int MAX_CELLS = 64;
    private final Logger logger = LoggerFactory.getLogger(LandingController.class);
    private final GameUiContextService gameUiContextService;
    private final UserService userService;
    private final BaseItemService baseItemService;
    private final Map<Integer, Cached> cache = new ConcurrentHashMap<>();

    public LandingController(GameUiContextService gameUiContextService, UserService userService, BaseItemService baseItemService) {
        this.gameUiContextService = gameUiContextService;
        this.userService = userService;
        this.baseItemService = baseItemService;
    }

    /**
     * The cells, or an empty list when there is nothing to choose: a player who already has a base,
     * or a level that does not place new players by itself.
     */
    @GetMapping(value = "cells", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> cells() {
        try {
            Integer levelId = null;
            String userId = userService.getUserIdFromContextOrNull();
            if (userId != null) {
                if (baseItemService.getPlayerBase4UserId(userId) != null) {
                    return nothingToChoose();
                }
                UserContext userContext = userService.getUserContextTransactional(userId);
                levelId = userContext != null ? userContext.getLevelId() : null;
            }
            StartPositionFinderService.LandingOverview overview = overview(levelId);
            if (overview == null || overview.cells().isEmpty()) {
                return nothingToChoose();
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("cells", overview.cells());
            result.put("recommended", overview.recommended());
            return result;
        } catch (Throwable t) {
            // The loading screen then shows its plain card - never worth more than a log line.
            logger.warn("Landing cells failed: {}", t.getMessage(), t);
            return nothingToChoose();
        }
    }

    /**
     * The cell sent with the current request (the cold context), or null.
     */
    public static Integer takeChosenCell() {
        try {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return null;
            }
            String value = attributes.getRequest().getParameter(PARAMETER);
            if (value == null) {
                return null;
            }
            int cell = Integer.parseInt(value);
            return cell >= 0 && cell < MAX_CELLS ? cell : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private StartPositionFinderService.LandingOverview overview(Integer levelId) {
        int key = levelId != null ? levelId : -1;
        long now = System.currentTimeMillis();
        Cached cached = cache.get(key);
        if (cached == null || now - cached.at() > CACHE_MILLIS) {
            cached = new Cached(gameUiContextService.landingOverview(levelId), now);
            cache.put(key, cached);
        }
        return cached.overview();
    }

    private static Map<String, Object> nothingToChoose() {
        return Map.of("cells", List.of());
    }

    private record Cached(StartPositionFinderService.LandingOverview overview, long at) {
    }
}
