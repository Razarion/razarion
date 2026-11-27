package com.btxtech.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorAlertServiceTest {

    @Test
    void saysWhoAndHowFarAlongButNotWhatWasWritten() {
        long now = 10 * 60_000L;
        assertEquals("Jade-63 wrote in the chat (level 3, base 4 min old)", OperatorAlertService.chatText("Jade-63", 3, now - 4 * 60_000L, now));
        assertEquals("Jade-63 wrote in the chat (level 1, no base)", OperatorAlertService.chatText("Jade-63", 1, null, now));
    }

    @Test
    void onePushPerPlayerInTenMinutes() {
        NtfyService ntfy = new NtfyService("");
        assertFalse(ntfy.isEnabled());
        assertTrue(ntfy.take("a", 0));
        assertFalse(ntfy.take("a", NtfyService.PER_KEY_MILLIS - 1));
        assertTrue(ntfy.take("b", 1));
        assertTrue(ntfy.take("a", NtfyService.PER_KEY_MILLIS + 1));
        assertEquals("Caf? chat", NtfyService.ascii("Café chat"));
    }
}
