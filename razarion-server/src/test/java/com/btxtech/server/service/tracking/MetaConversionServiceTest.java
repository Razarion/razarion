package com.btxtech.server.service.tracking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * The click id is not sent as itself: Meta matches on the value its own browser pixel would have
 * written into the _fbc cookie. The format is Meta's, and getting it wrong fails silently - the
 * events are accepted and matched to nobody, which is indistinguishable from a campaign that
 * converts nothing.
 */
class MetaConversionServiceTest {
    @Test
    void theClickIdIsSentAsTheCookieValueMetaMatchesOn() {
        assertEquals("fb.1.1756400000000.IwcGRvZgRle",
                MetaConversionService.fbc("IwcGRvZgRle", 1756400000000L));
    }

    /** The time in it is when the click was seen, not when the event happened hours later. */
    @Test
    void theClickTimeIsPartOfTheValue() {
        assertEquals("fb.1.1000.abc", MetaConversionService.fbc("abc", 1000L));
        assertEquals("fb.1.2000.abc", MetaConversionService.fbc("abc", 2000L));
    }

    /**
     * Without credentials nothing is sent and nothing fails: a missing token must never take a
     * page request or a quest with it. The same MOCK behaviour the other two networks have.
     */
    @Test
    void withoutCredentialsNothingIsSentAndNothingBreaks() {
        MetaConversionService service = new MetaConversionService(
                "v21.0", "", "", "", "https://www.razarion.com", "", "", "", "", "");

        assertDoesNotThrow(() -> {
            service.sendPageVisitEvent("IwcGRvZgRle", "Mozilla/5.0 (iPhone) Instagram");
            service.registerUser("user-1", "IwcGRvZgRle", "Mozilla/5.0 (iPhone) Instagram");
            service.sendBuilderDeployedEvent("user-1");
            service.sendQuestPassedEvent("user-1", 388, 7);
            service.sendLevelUpEvent("user-1", 8);
            service.unregisterUser("user-1");
        });
    }

    /** A visitor who never clicked an ad has no click id, and no event is invented for them. */
    @Test
    void aVisitorWithoutAClickIdProducesNoEvent() {
        MetaConversionService service = new MetaConversionService(
                "v21.0", "", "", "", "https://www.razarion.com", "", "", "", "", "");

        assertDoesNotThrow(() -> {
            service.sendPageVisitEvent(null, "Mozilla/5.0");
            service.sendPageVisitEvent("", "Mozilla/5.0");
            service.registerUser("user-2", null, "Mozilla/5.0");
            // Nothing was registered, so this finds nothing rather than sending an unmatched event.
            service.sendBuilderDeployedEvent("user-2");
        });
    }

    /**
     * Reading Meta's answer instead of only noticing that the request did not throw.
     * <p>
     * A 200 from the events endpoint means the request was well formed, not that anything was
     * counted. Two hundred and thirty-nine events were logged as "sent successfully" while Meta
     * reported receiving none, and the body that would have said which of the two was true had
     * been thrown away unread.
     */
    @Test
    void oneEventInAndOneReceivedIsNothingToReport() {
        assertNull(MetaConversionService.ackProblem(
                "{\"events_received\":1,\"messages\":[],\"fbtrace_id\":\"Abc123\"}"));
    }

    @Test
    void aWarningFromMetaIsWorthALine() {
        // Where it says things like a click id it cannot match, without failing the request.
        String problem = MetaConversionService.ackProblem(
                "{\"events_received\":1,\"messages\":[\"Invalid parameter fbc\"]}");

        assertNotNull(problem);
        assertTrue(problem.contains("Invalid parameter fbc"));
    }

    @Test
    void anEventThatWasNotCountedIsWorthALine() {
        assertNotNull(MetaConversionService.ackProblem("{\"events_received\":0,\"messages\":[]}"));
    }

    @Test
    void anAnswerInAnotherShapeIsNotReadAsSuccess() {
        // A proxy page, an empty body, a redirect: none of these is an accepted event, and none
        // of them throws.
        assertNotNull(MetaConversionService.ackProblem(null));
        assertNotNull(MetaConversionService.ackProblem(""));
        assertNotNull(MetaConversionService.ackProblem("<html>gateway timeout</html>"));
        assertNotNull(MetaConversionService.ackProblem("{\"fbtrace_id\":\"Abc123\"}"));
    }

    /**
     * Where the event happened, which Meta asks for on every website event.
     * <p>
     * Without it a request is still answered 200 and the event is still not processed - the exact
     * shape of "the connection exists but no events came through" that the diagnostics reported
     * while the log here said 239 sent successfully. Every remaining step happens in the game.
     */
    @Test
    void everyEventSaysWhichPageItHappenedOn() {
        MetaConversionService service = new MetaConversionService("v21.0", "", "", "",
                "https://www.razarion.com", "", "", "", "", "");

        assertEquals("https://www.razarion.com/game", service.sourceUrl());
    }

    @Test
    void aTrailingSlashDoesNotBecomeADoubleOne() {
        MetaConversionService service = new MetaConversionService("v21.0", "", "", "",
                "https://www.razarion.com/", "", "", "", "", "");

        assertEquals("https://www.razarion.com/game", service.sourceUrl());
    }
}
