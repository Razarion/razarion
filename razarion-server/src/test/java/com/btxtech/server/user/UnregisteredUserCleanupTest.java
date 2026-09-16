package com.btxtech.server.user;

import com.btxtech.server.gameengine.ClientSystemConnection;
import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Anonymous players are deleted sixty minutes after their connection closed, and that is the only
 * thing that ever frees a start-region spawn point again. Two ways of never being seen by it were
 * measured on PROD on 2026-09-16, and both leave the row standing until the next server restart:
 * a connection that ended without a close event, and a user that never connected at all.
 */
class UnregisteredUserCleanupTest {
    private static final String PLAYING = "playing";
    private static final String GONE = "gone";

    private UserRepository userRepository;
    private ClientSystemConnectionService connectionService;
    private UserService userService;

    private static UserEntity anonymous(String userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setCreationDate(new Date(0));
        return userEntity;
    }

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        connectionService = mock(ClientSystemConnectionService.class);
        userService = new UserService(null, userRepository, null, null, null, null, null, null);
        ReflectionTestUtils.setField(userService, "clientSystemConnectionService", connectionService);
        when(userRepository.findInactiveSince(any())).thenReturn(List.of());
        when(userRepository.findOpenSince(any())).thenReturn(List.of());
        when(userRepository.findNeverConnectedBefore(any())).thenReturn(List.of());
        when(connectionService.getOpenConnections()).thenReturn(new TreeMap<>());
    }

    /**
     * The row says connected and the socket is gone. Nothing else would ever notice: the close
     * timestamp comes from the close event alone, and no idle timeout is configured.
     */
    @Test
    void stampsAConnectionThatEndedWithoutACloseEvent() {
        UserEntity gone = anonymous(GONE);
        gone.setSystemConnectionOpened(LocalDateTime.now().minusHours(3));
        when(userRepository.findOpenSince(any())).thenReturn(List.of(gone));

        userService.cleanupDisconnectedUnregisteredUsers();

        assertNotNull(gone.getSystemConnectionClosed(),
                "a connection nobody holds any more has ended, whatever the row says");
        verify(userRepository).save(gone);
    }

    /**
     * The open-connection map is the authority, not the row - which is the whole point, because the
     * row is the thing that is wrong. A player who is still holding the socket keeps his base.
     */
    @Test
    void leavesAPlayerWhoIsStillConnectedAlone() {
        UserEntity playing = anonymous(PLAYING);
        playing.setSystemConnectionOpened(LocalDateTime.now().minusHours(3));
        when(userRepository.findOpenSince(any())).thenReturn(List.of(playing));
        Map<String, ClientSystemConnection> open = new TreeMap<>();
        open.put(PLAYING, mock(ClientSystemConnection.class));
        when(connectionService.getOpenConnections()).thenReturn(open);

        userService.cleanupDisconnectedUnregisteredUsers();

        assertNull(playing.getSystemConnectionClosed());
        verify(userRepository, never()).save(playing);
    }

    /**
     * Stamped rather than deleted, so the sixty minutes keep meaning "after the connection ended".
     * A player who is reloading is stamped and clears it again on reconnect; deleting here would
     * take his base during those seconds.
     */
    @Test
    void doesNotDeleteTheStampedUserInTheSamePass() {
        UserEntity gone = anonymous(GONE);
        gone.setSystemConnectionOpened(LocalDateTime.now().minusHours(3));
        when(userRepository.findOpenSince(any())).thenReturn(List.of(gone));

        userService.cleanupDisconnectedUnregisteredUsers();

        verify(userRepository, never()).delete(gone);
    }

    /**
     * The bigger of the two leaks: every game start writes more than one anonymous user, and only
     * one of them ever connects. The rest own nothing and were invisible to every cleanup.
     */
    @Test
    void deletesAnAnonymousUserThatNeverConnected() {
        UserEntity neverConnected = anonymous("never");
        when(userRepository.findNeverConnectedBefore(any())).thenReturn(List.of(neverConnected));

        userService.cleanupDisconnectedUnregisteredUsers();

        verify(userRepository).delete(neverConnected);
    }

    /**
     * The expensive mistake this guards. A registered player is expected to come back and keeps his
     * base regardless - and the query cannot tell the two apart, so the check has to be here.
     */
    @Test
    void keepsARegisteredUserThatNeverConnected() {
        UserEntity registered = anonymous("registered");
        // createRegisterState() reads the verification dates, not the email.
        registered.setVerifiedDone();
        when(userRepository.findNeverConnectedBefore(any())).thenReturn(List.of(registered));

        userService.cleanupDisconnectedUnregisteredUsers();

        verify(userRepository, never()).delete(registered);
    }
}
