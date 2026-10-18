package com.btxtech.server.gameengine;

import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.service.tracking.MetaConversionService;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.XConversionService;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A reload, or the reconnect after a dropped connection, opens the new connection while the server
 * may not yet have seen the old one end - an unclean drop surfaces late. The old close used to
 * remove the user's entry whatever it held, and that was the new connection: the player's only tab
 * got nothing from the server any more, and the stamped close let the cleanup delete the anonymous
 * player an hour later while he played on. Reproduced locally with two tabs on 2026-10-11.
 * <p>
 * The same two tabs as a player would have them: a second tab of the same player used to replace
 * the first, which then got nothing from the server while its orders still went through. Every tab
 * of a user is served now.
 */
class ConnectionReplacementTest {
    private static final String USER = "player";

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        when(userService.getOrCreateUserId(any(), any(), any())).thenReturn(USER);
    }

    private static WebSocketSession session() {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(attributes);
        return session;
    }

    @Test
    void systemConnectionLateCloseOfTheOldOneKeepsTheNewOne() {
        ClientSystemConnection oldConnection = mock(ClientSystemConnection.class);
        ClientSystemConnection newConnection = mock(ClientSystemConnection.class);
        when(oldConnection.getUserId()).thenReturn(USER);
        when(newConnection.getUserId()).thenReturn(USER);
        @SuppressWarnings("unchecked")
        Provider<ClientSystemConnection> provider = mock(Provider.class);
        when(provider.get()).thenReturn(oldConnection, newConnection);
        @SuppressWarnings("unchecked")
        ObjectProvider<ServerRestartAnnouncementService> announcements = mock(ObjectProvider.class);
        when(announcements.getObject()).thenReturn(mock(ServerRestartAnnouncementService.class));
        ClientSystemConnectionService service = new ClientSystemConnectionService(provider, null, userService, announcements, mock(ObjectProvider.class));
        WebSocketSession oldSession = session();
        WebSocketSession newSession = session();

        service.afterConnectionEstablished(oldSession);
        service.afterConnectionEstablished(newSession);
        service.afterConnectionClosed(oldSession, CloseStatus.GOING_AWAY);

        assertSame(newConnection, service.getOpenConnections().get(USER), "the reload's connection stays the user's");
        verify(userService, never()).onClientSystemConnectionClosed(USER);

        service.afterConnectionClosed(newSession, CloseStatus.GOING_AWAY);

        assertTrue(service.getOpenConnections().isEmpty());
        verify(userService, times(1)).onClientSystemConnectionClosed(USER);
    }

    @Test
    void gameConnectionLateCloseOfTheOldOneKeepsTheNewOne() {
        ClientGameConnection oldConnection = mock(ClientGameConnection.class);
        ClientGameConnection newConnection = mock(ClientGameConnection.class);
        when(oldConnection.getUserId()).thenReturn(USER);
        when(newConnection.getUserId()).thenReturn(USER);
        @SuppressWarnings("unchecked")
        Provider<ClientGameConnection> provider = mock(Provider.class);
        when(provider.get()).thenReturn(oldConnection, newConnection);
        RedditConversionService reddit = mock(RedditConversionService.class);
        ClientGameConnectionService service = new ClientGameConnectionService(provider, null, userService,
                mock(PageRequestService.class), reddit, mock(XConversionService.class), mock(MetaConversionService.class));
        WebSocketSession oldSession = session();
        WebSocketSession newSession = session();

        service.afterConnectionEstablished(oldSession);
        service.afterConnectionEstablished(newSession);
        service.afterConnectionClosed(oldSession, CloseStatus.GOING_AWAY);

        assertSame(newConnection, service.getOpenConnections().get(USER), "the reload's connection stays the user's");
        verify(reddit, never()).unregisterUser(USER);

        service.afterConnectionClosed(newSession, CloseStatus.GOING_AWAY);

        assertTrue(service.getOpenConnections().isEmpty());
        verify(reddit, times(1)).unregisterUser(USER);
    }

    @Test
    void systemConnectionEveryTabOfTheUserIsServed() throws Exception {
        ClientSystemConnection first = mock(ClientSystemConnection.class);
        ClientSystemConnection second = mock(ClientSystemConnection.class);
        when(first.getUserId()).thenReturn(USER);
        when(second.getUserId()).thenReturn(USER);
        @SuppressWarnings("unchecked")
        Provider<ClientSystemConnection> provider = mock(Provider.class);
        when(provider.get()).thenReturn(first, second);
        @SuppressWarnings("unchecked")
        ObjectProvider<ServerRestartAnnouncementService> announcements = mock(ObjectProvider.class);
        when(announcements.getObject()).thenReturn(mock(ServerRestartAnnouncementService.class));
        ClientSystemConnectionService service = new ClientSystemConnectionService(provider, null, userService, announcements, mock(ObjectProvider.class));
        WebSocketSession firstSession = session();
        WebSocketSession secondSession = session();
        service.afterConnectionEstablished(firstSession);
        service.afterConnectionEstablished(secondSession);

        service.onXpChanged(USER, 12);

        verify(first).sendToClient(anyString());
        verify(second).sendToClient(anyString());

        // The newer tab goes first: the older one is still served, and nothing is stamped yet.
        service.afterConnectionClosed(secondSession, CloseStatus.GOING_AWAY);
        assertSame(first, service.getOpenConnections().get(USER));
        verify(userService, never()).onClientSystemConnectionClosed(USER);
        service.onXpChanged(USER, 13);
        verify(first, times(2)).sendToClient(anyString());
        verify(second, times(1)).sendToClient(anyString());

        service.afterConnectionClosed(firstSession, CloseStatus.GOING_AWAY);
        assertTrue(service.getOpenConnections().isEmpty());
        verify(userService, times(1)).onClientSystemConnectionClosed(USER);
    }

    @Test
    void gameConnectionEveryTabOfTheUserIsServed() throws Exception {
        ClientGameConnection first = mock(ClientGameConnection.class);
        ClientGameConnection second = mock(ClientGameConnection.class);
        when(first.getUserId()).thenReturn(USER);
        when(second.getUserId()).thenReturn(USER);
        @SuppressWarnings("unchecked")
        Provider<ClientGameConnection> provider = mock(Provider.class);
        when(provider.get()).thenReturn(first, second);
        ClientGameConnectionService service = new ClientGameConnectionService(provider, null, userService,
                mock(PageRequestService.class), mock(RedditConversionService.class), mock(XConversionService.class), mock(MetaConversionService.class));
        service.afterConnectionEstablished(session());
        service.afterConnectionEstablished(session());
        PlayerBase base = mock(PlayerBase.class);
        when(base.getUserId()).thenReturn(USER);
        when(base.getBaseId()).thenReturn(7);

        service.onBaseDeleted(base);                    // to everybody
        service.sendResourcesBalanceChanged(base, 100); // to the user

        verify(first, times(2)).sendToClient(anyString());
        verify(second, times(2)).sendToClient(anyString());
    }
}
