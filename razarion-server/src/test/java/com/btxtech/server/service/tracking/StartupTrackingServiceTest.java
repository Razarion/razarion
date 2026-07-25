package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abandoned startups are the point of these rules: for a long time the startup list showed
 * almost nothing but successes while a third of the players who opened the game never reached
 * it, because a player who leaves reports nothing at all.
 */
class StartupTrackingServiceTest {
    private static final long MINUTE = 60 * 1000L;
    /** Old enough to be past the grace period in which a session may still be booting. */
    private static final Date LONG_AGO = new Date(System.currentTimeMillis() - 60 * MINUTE);

    private final StartupTrackingService startupTrackingService = new StartupTrackingService(null);

    @Test
    void sessionWithoutTerminatedRecordBecomesAborted() {
        List<StartupTaskJson> tasks = List.of(
                task("session-1", "LOAD_START_JS", LONG_AGO),
                task("session-1", "LOAD_THREE_JS_MODELS", new Date(LONG_AGO.getTime() + 4000)));

        List<StartupTerminatedJson> resolved = startupTrackingService.resolveTerminated(tasks, List.of());

        assertEquals(1, resolved.size());
        StartupTerminatedJson aborted = resolved.get(0);
        assertTrue(aborted.isAborted());
        assertFalse(aborted.isSuccessful());
        // The task it got stuck on is the whole point - it names what to fix.
        assertEquals("LOAD_THREE_JS_MODELS", aborted.getLastTaskEnum());
        // Nobody reported an end, so there is no duration. A zero would read as an instant startup.
        assertNull(aborted.getTotalTime());
    }

    @Test
    void finishedSessionIsNotTouched() {
        List<StartupTaskJson> tasks = List.of(task("session-1", "RUN_GAME", LONG_AGO));
        StartupTerminatedJson successful = new StartupTerminatedJson()
                .gameSessionUuid("session-1").successful(true).totalTime(5000).serverTime(LONG_AGO);

        List<StartupTerminatedJson> resolved = startupTrackingService.resolveTerminated(tasks, List.of(successful));

        assertEquals(1, resolved.size());
        assertFalse(resolved.get(0).isAborted());
        assertEquals(5000, resolved.get(0).getTotalTime());
    }

    /**
     * The page beacon also fires when a tab merely goes to the background - Safari on iOS gives
     * no other signal. A player who comes back and finishes must not stay marked as aborted.
     */
    @Test
    void abortIsDroppedWhenTheSameSessionLaterFinishes() {
        List<StartupTaskJson> tasks = List.of(task("session-1", "LOAD_THREE_JS_MODELS", LONG_AGO));
        StartupTerminatedJson beaconAbort = new StartupTerminatedJson()
                .gameSessionUuid("session-1").successful(false).aborted(true).serverTime(LONG_AGO);
        StartupTerminatedJson successful = new StartupTerminatedJson()
                .gameSessionUuid("session-1").successful(true).totalTime(9000).serverTime(LONG_AGO);

        List<StartupTerminatedJson> resolved = startupTrackingService
                .resolveTerminated(tasks, List.of(beaconAbort, successful));

        assertEquals(1, resolved.size());
        assertTrue(resolved.get(0).isSuccessful());
    }

    /** A failed startup reported itself, so it keeps its own record and its duration. */
    @Test
    void failedSessionKeepsItsRecord() {
        List<StartupTaskJson> tasks = List.of(task("session-1", "INIT_WORKER", LONG_AGO));
        StartupTerminatedJson failed = new StartupTerminatedJson()
                .gameSessionUuid("session-1").successful(false).totalTime(3000).serverTime(LONG_AGO);

        List<StartupTerminatedJson> resolved = startupTrackingService.resolveTerminated(tasks, List.of(failed));

        assertEquals(1, resolved.size());
        assertFalse(resolved.get(0).isAborted());
        assertEquals(3000, resolved.get(0).getTotalTime());
    }

    /** A startup that began seconds ago is probably still running, not abandoned. */
    @Test
    void sessionStillWithinTheGracePeriodIsNotCalledAborted() {
        List<StartupTaskJson> tasks = List.of(
                task("session-1", "LOAD_THREE_JS_MODELS", new Date(System.currentTimeMillis() - 5000)));

        assertTrue(startupTrackingService.resolveTerminated(tasks, List.of()).isEmpty());
    }

    /** The click id has to survive, otherwise the platform filter hides the aborted sessions. */
    @Test
    void abortedSessionKeepsItsClickId() {
        List<StartupTaskJson> tasks = List.of(
                task("session-1", "PAGE_LOADED", LONG_AGO).rdtCid("rdt-42").utmSource("reddit"));

        StartupTerminatedJson aborted = startupTrackingService.resolveTerminated(tasks, List.of()).get(0);

        assertEquals("rdt-42", aborted.getRdtCid());
        assertEquals("reddit", aborted.getUtmSource());
    }

    private StartupTaskJson task(String gameSessionUuid, String taskEnum, Date serverTime) {
        return new StartupTaskJson().gameSessionUuid(gameSessionUuid).taskEnum(taskEnum).serverTime(serverTime);
    }
}
