package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.TabHiddenJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The page reports its own startup tasks from a hand written script in index.html, before any
 * generated code exists. Nothing checks those field names: unknown properties are ignored by
 * default, so a typo does not fail the request - it leaves the field null and the record looks
 * like it was never filled in. These are the exact bodies index.html sends.
 * <p>
 * Keep in sync with the inline script in razarion-frontend/src/index.html.
 */
class StartupBeaconPayloadTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** What the page posts to /rest/tracker/startupTask as soon as it has loaded. */
    @Test
    void pageLoadedTaskIsRead() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","rdtCid":"rdt-42","twclid":null,
                 "utmCampaign":"launch","utmSource":"reddit","taskEnum":"PAGE_LOADED",
                 "startTime":1753430000000,"duration":0,
                 "userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64)","error":null}""";

        StartupTaskJson startupTaskJson = objectMapper.readValue(body, StartupTaskJson.class);

        assertEquals("PGABC123", startupTaskJson.getGameSessionUuid());
        assertEquals("PAGE_LOADED", startupTaskJson.getTaskEnum());
        // The browser is the point of this record - it names who cannot run the engine at all.
        assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", startupTaskJson.getUserAgent());
        assertEquals("rdt-42", startupTaskJson.getRdtCid());
        assertEquals("reddit", startupTaskJson.getUtmSource());
        // Date.now() is epoch millis, and the field is a Date.
        assertNotNull(startupTaskJson.getStartTime());
        assertEquals(1753430000000L, startupTaskJson.getStartTime().getTime());
    }

    /** A browser too old for WebAssembly reports itself here and nowhere else. */
    @Test
    void unsupportedBrowserErrorIsRead() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","taskEnum":"PAGE_LOADED","startTime":1753430000000,
                 "duration":0,"error":"No WebAssembly support"}""";

        assertEquals("No WebAssembly support",
                objectMapper.readValue(body, StartupTaskJson.class).getError());
    }

    /** What the pagehide beacon posts to /rest/tracker/startupTerminated: the player is gone. */
    @Test
    void abortBeaconIsRead() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","rdtCid":"rdt-42","twclid":null,"utmCampaign":null,
                 "utmSource":"reddit","successful":false,"aborted":true,"hidden":false,
                 "lastTaskEnum":"LOAD_THREE_JS_MODELS"}""";

        StartupTerminatedJson startupTerminatedJson = objectMapper.readValue(body, StartupTerminatedJson.class);

        assertTrue(startupTerminatedJson.isAborted());
        assertFalse(startupTerminatedJson.isSuccessful());
        assertEquals(Boolean.FALSE, startupTerminatedJson.getHidden());
        assertEquals("LOAD_THREE_JS_MODELS", startupTerminatedJson.getLastTaskEnum());
        assertEquals("rdt-42", startupTerminatedJson.getRdtCid());
    }

    /**
     * The same beacon on a tab switch. Told apart from the one above by a single field, and the
     * difference decides what to do: this player is still reachable and will come back to a
     * finished game, the one above is gone.
     */
    @Test
    void tabSwitchDuringStartupIsMarkedHidden() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","successful":false,"aborted":true,"hidden":true,
                 "lastTaskEnum":"INIT_GAME_UI"}""";

        StartupTerminatedJson startupTerminatedJson = objectMapper.readValue(body, StartupTerminatedJson.class);

        assertTrue(startupTerminatedJson.isAborted());
        assertEquals(Boolean.TRUE, startupTerminatedJson.getHidden());
    }

    /**
     * An abort the server derived itself never saw the browser, so it cannot claim either case.
     * Null has to survive - false would count a vanished player as a tab switch.
     */
    @Test
    void derivedAbortHasNoHiddenFlag() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","successful":false,"aborted":true}""";

        assertNull(objectMapper.readValue(body, StartupTerminatedJson.class).getHidden());
    }

    /** What the beacon posts to /rest/tracker/tabHidden once the game is already running. */
    @Test
    void tabHiddenAfterStartupIsRead() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","rdtCid":null,"twclid":"tw-7","utmCampaign":"launch",
                 "utmSource":"twitter","millisSincePageLoad":8421}""";

        TabHiddenJson tabHiddenJson = objectMapper.readValue(body, TabHiddenJson.class);

        assertEquals("PGABC123", tabHiddenJson.getGameSessionUuid());
        assertEquals(Integer.valueOf(8421), tabHiddenJson.getMillisSincePageLoad());
        assertEquals("tw-7", tabHiddenJson.getTwclid());
        assertEquals("twitter", tabHiddenJson.getUtmSource());
    }

    /**
     * The engine sends no totalTime only when it never finished. An absent value has to stay
     * absent: a zero would show up in the startup list as an instant startup.
     */
    @Test
    void missingTotalTimeStaysNull() throws IOException {
        String body = """
                {"gameSessionUuid":"PGABC123","successful":false,"aborted":true}""";

        assertEquals(null, objectMapper.readValue(body, StartupTerminatedJson.class).getTotalTime());
    }
}
