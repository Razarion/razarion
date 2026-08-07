package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import com.btxtech.shared.dto.StartupTaskJson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Which player an attempt belongs to. The failure this guards against is a quiet one: the row named
 * a throwaway user the tracking call itself had created, so a player of weeks showed up as level 1
 * without a base, and everything the row said about them was true of nobody.
 */
class PlayerSessionUserTest {
    private static final String PLAYER = "cfa69ca2-real-player";
    private static final String THROWAWAY = "e1dfae45-created-by-the-first-task";

    private static StartupTaskJson task(String taskEnum, String userId) {
        StartupTaskJson startupTaskJson = new StartupTaskJson().taskEnum(taskEnum);
        startupTaskJson.setUserId(userId);
        return startupTaskJson;
    }

    private static UserActivity gameSessionStarted(String sessionUuid, String userId) {
        return new UserActivity()
                .userActivityType(UserActivityType.GAME_SESSION_STARTED)
                .gameSessionUuid(sessionUuid)
                .userId(userId);
    }

    /** The game itself named the player - nothing stamped on a request outranks that. */
    @Test
    void theGameSessionDecides() {
        List<StartupTaskJson> tasks = List.of(task("PAGE_LOADED", THROWAWAY), task("RUN_GAME", PLAYER));
        assertEquals(PLAYER, PlayerSessionService.userIdOfAttempt(tasks, null, "SESSION",
                List.of(gameSessionStarted("SESSION", PLAYER))));
    }

    /**
     * A startup that never opened its game session has only the stamped ids, and the first of them
     * is the one written before the page had attached its token.
     */
    @Test
    void withoutAGameSessionTheMajorityDecides() {
        List<StartupTaskJson> tasks = List.of(
                task("PAGE_LOADED", THROWAWAY),
                task("LOAD_START_JS", PLAYER),
                task("INIT_WORKER", PLAYER));
        assertEquals(PLAYER, PlayerSessionService.userIdOfAttempt(tasks, null, "SESSION", List.of()));
    }

    /** Another session's start says nothing about this one. */
    @Test
    void aForeignGameSessionIsIgnored() {
        List<StartupTaskJson> tasks = List.of(task("PAGE_LOADED", PLAYER));
        assertEquals(PLAYER, PlayerSessionService.userIdOfAttempt(tasks, null, "SESSION",
                List.of(gameSessionStarted("ANOTHER_SESSION", THROWAWAY))));
    }

    /** Nobody was known: the startup died before anything could name a player. */
    @Test
    void nothingKnownStaysUnknown() {
        assertNull(PlayerSessionService.userIdOfAttempt(List.of(task("PAGE_LOADED", null)), null,
                "SESSION", List.of()));
    }
}
