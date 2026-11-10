package com.btxtech.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What lets anonymous players into the chat without letting a flood in: a stable guest name, a
 * cleaned and capped message, and a limit per player.
 */
class ChatServiceTest {
    private final ChatService chatService = new ChatService(null, null, null, null, null);

    @Test
    void guestNameIsTheTickerCallSign() {
        String name = ChatService.guestName("761c5379-30b3-4616-a208-4a34431f9d8e");
        assertEquals(name, ChatService.guestName("761c5379-30b3-4616-a208-4a34431f9d8e"));
        assertEquals(com.btxtech.server.rest.CallSign.of("761c5379-30b3-4616-a208-4a34431f9d8e"), name);
        assertTrue(name.matches("[A-Z][a-z]+-\\d{2}"), name);
    }

    @Test
    void takesOnlyAnMp4() {
        byte[] mp4 = new byte[64];
        System.arraycopy("ftypisom".getBytes(), 0, mp4, 4, 8);
        assertTrue(ChatService.isMp4(mp4));
        assertFalse(ChatService.isMp4(new byte[64]));
        assertFalse(ChatService.isMp4(null));
    }

    @Test
    void leftNoobIslandNamesThePlayer() {
        assertEquals("Falcon-42 just left the Noob Island and landed on the mainland!", ChatService.leftNoobIslandText("Falcon-42"));
    }

    @Test
    void cleanTrimsJoinsLinesAndCaps() {
        assertEquals("how do i play", ChatService.clean("  how do i play \n", 300));
        assertEquals("a b", ChatService.clean("a\r\nb", 300));
        assertEquals(300, ChatService.clean("x".repeat(1000), 300).length());
        assertEquals("", ChatService.clean(null, 300));
    }

    @Test
    void oneMessagePerGap() {
        assertTrue(chatService.allowed("a", 0));
        assertFalse(chatService.allowed("a", ChatService.MIN_GAP_MS - 1));
        assertTrue(chatService.allowed("a", ChatService.MIN_GAP_MS));
        // Somebody else is not held up by it
        assertTrue(chatService.allowed("b", 1));
    }

    @Test
    void burstPerWindow() {
        long t = 0;
        for (int i = 0; i < ChatService.BURST; i++, t += ChatService.MIN_GAP_MS) {
            assertTrue(chatService.allowed("a", t));
        }
        assertFalse(chatService.allowed("a", t));
        // Once the window has moved past the first one, there is room again
        assertTrue(chatService.allowed("a", ChatService.WINDOW_MS + 1));
    }
}
