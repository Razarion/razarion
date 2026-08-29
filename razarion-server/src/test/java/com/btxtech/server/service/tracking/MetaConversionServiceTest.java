package com.btxtech.server.service.tracking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                "v21.0", "", "", "", "", "", "", "", "", "");

        assertDoesNotThrow(() -> {
            service.sendLandingViewEvent("IwcGRvZgRle", "Mozilla/5.0 (iPhone) Instagram");
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
                "v21.0", "", "", "", "", "", "", "", "", "");

        assertDoesNotThrow(() -> {
            service.sendLandingViewEvent(null, "Mozilla/5.0");
            service.sendPageVisitEvent(null, "Mozilla/5.0");
            service.sendPageVisitEvent("", "Mozilla/5.0");
            service.registerUser("user-2", null, "Mozilla/5.0");
            // Nothing was registered, so this finds nothing rather than sending an unmatched event.
            service.sendBuilderDeployedEvent("user-2");
        });
    }
}
