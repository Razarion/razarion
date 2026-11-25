package com.btxtech.server.rest;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PresenceControllerTest {

    @Test
    void namesTheOthersAndTheirBasesPlayersWithABaseFirst() {
        PlayerBase base = mock(PlayerBase.class);
        when(base.getBaseId()).thenReturn(7);
        PresenceController.Presence presence = PresenceController.build(Set.of("a", "b"),
                Map.of("a", "Anna"),
                userId -> userId.equals("b") ? base : null);

        assertEquals(2, presence.count());
        assertEquals(List.of(new PresenceController.OnlinePlayer(CallSign.of("b"), 7), new PresenceController.OnlinePlayer("Anna", null)),
                presence.players());
    }

    @Test
    void countsAllButNamesAtMostTwenty() {
        Set<String> many = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            many.add("user" + i);
        }
        PresenceController.Presence presence = PresenceController.build(many, Map.of(), userId -> null);

        assertEquals(30, presence.count());
        assertEquals(PresenceController.MAX_NAMES, presence.players().size());
    }
}
