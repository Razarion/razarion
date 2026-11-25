package com.btxtech.server.service;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.EmotePacket;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmoteServiceTest {
    private SyncItemContainerServiceImpl items;
    private ClientSystemConnectionService connections;
    private UserService userService;
    private EmoteService service;

    @BeforeEach
    void setUp() {
        items = mock(SyncItemContainerServiceImpl.class);
        connections = mock(ClientSystemConnectionService.class);
        userService = mock(UserService.class);
        service = new EmoteService(items, connections, userService, mock(UserActivityService.class));
        when(userService.getUserIdFromContextOrNull()).thenReturn("me");
        SyncBaseItem mine = unit("me", 3, "Cobalt-31");
        SyncBaseItem theirs = unit("somebody", 4, "Eagle-62");
        when(items.getSyncItem(5)).thenReturn(mine);
        when(items.getSyncItem(6)).thenReturn(theirs);
    }

    private static SyncBaseItem unit(String userId, int baseId, String name) {
        PlayerBase base = mock(PlayerBase.class);
        when(base.getUserId()).thenReturn(userId);
        when(base.getBaseId()).thenReturn(baseId);
        when(base.getName()).thenReturn(name);
        AbstractSyncPhysical physical = mock(AbstractSyncPhysical.class);
        when(physical.getPosition()).thenReturn(new DecimalPosition(10, 20));
        SyncBaseItem item = mock(SyncBaseItem.class);
        when(item.getBase()).thenReturn(base);
        when(item.getAbstractSyncPhysical()).thenReturn(physical);
        return item;
    }

    @Test
    void aWaveOverAnOwnUnitGoesToEverybody() {
        assertEquals(EmoteService.Result.SENT, service.send("WAVE", 5, false));

        ArgumentCaptor<EmotePacket> packet = ArgumentCaptor.forClass(EmotePacket.class);
        verify(connections).sendEmote(packet.capture());
        assertEquals("WAVE", packet.getValue().getEmote());
        assertEquals(5, packet.getValue().getSyncItemId());
        assertEquals(3, packet.getValue().getBaseId());
        assertEquals("Cobalt-31", packet.getValue().getName());
        assertEquals(10, packet.getValue().getX());
    }

    @Test
    void onlyOverOwnUnitsAndOnlyKnownSigns() {
        assertEquals(EmoteService.Result.NOT_YOUR_UNIT, service.send("WAVE", 6, false));
        assertEquals(EmoteService.Result.NOT_YOUR_UNIT, service.send("WAVE", 99, false));
        assertEquals(EmoteService.Result.UNKNOWN_EMOTE, service.send("<script>", 5, false));
        verify(connections, never()).sendEmote(any());
    }

    @Test
    void aPlayerCannotFloodTheOthers() {
        assertTrue(service.allowed("me", 0));
        assertFalse(service.allowed("me", 500));
        long t = 0;
        int sent = 1;
        while (service.allowed("me", t += EmoteService.MIN_GAP_MILLIS)) {
            sent++;
        }
        assertEquals(EmoteService.MAX_PER_MINUTE, sent);
        assertTrue(service.allowed("me", 120_000));
    }
}
