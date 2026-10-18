package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.engine.LevelCrudService;
import com.btxtech.server.service.history.HistoryService;
import com.btxtech.server.service.tracking.UserActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An unregistered player used to be known by his http session alone - gone after 30 idle minutes,
 * a closed browser or a server restart - and came back at level 1 every time. 5.6 % come back
 * within three days (PROD, 2026-10-05). The anonymous cookie brings them back to their own row.
 */
class AnonymousUserReturnTest {
    private static final String TOKEN = "token-of-yesterday";
    private UserRepository userRepository;
    private HistoryService historyService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        historyService = mock(HistoryService.class);
        userService = new UserService(mock(LevelCrudService.class), userRepository, null, null, null, null,
                mock(UserActivityService.class), historyService);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.empty());
    }

    private static UserEntity yesterdaysPlayer() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("yesterday");
        userEntity.setAnonymousToken(TOKEN);
        userEntity.setCreationDate(new Date(0));
        return userEntity;
    }

    @Test
    void aReturningPlayerGetsHisOwnRowInANewSession() {
        UserEntity yesterday = yesterdaysPlayer();
        when(userRepository.findByAnonymousToken(TOKEN)).thenReturn(Optional.of(yesterday));

        String userId = userService.getOrCreateUserId(null, "todays-session", TOKEN);

        assertEquals("yesterday", userId);
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(historyService).onAnonymousUserReturned(yesterday, "todays-session");
    }

    @Test
    void theSessionRemembersHimAfterwards() {
        when(userRepository.findByAnonymousToken(TOKEN)).thenReturn(Optional.of(yesterdaysPlayer()));
        userService.getOrCreateUserId(null, "todays-session", TOKEN);
        when(userRepository.findByUserId("yesterday")).thenReturn(Optional.of(yesterdaysPlayer()));

        // The websocket handshake of the same page, cookie or not.
        assertEquals("yesterday", userService.getOrCreateUserId(null, "todays-session", null));
    }

    /**
     * Registration turns the same row into an account. The cookie still names it, but from then on
     * the way back is the login - otherwise logging out would hand the account to the browser.
     */
    @Test
    void aTokenOfAPlayerWhoRegisteredSinceIsIgnored() {
        UserEntity registered = yesterdaysPlayer();
        registered.setVerifiedDone();
        when(userRepository.findByAnonymousToken(TOKEN)).thenReturn(Optional.of(registered));

        String userId = userService.getOrCreateUserId(null, "todays-session", TOKEN);

        assertNotEquals("yesterday", userId);
        verify(historyService, never()).onAnonymousUserReturned(any(), any());
    }

    @Test
    void anUnknownTokenGetsANewPlayerWithANewToken() {
        String userId = userService.getOrCreateUserId(null, "todays-session", "deleted-two-weeks-ago");

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(saved.capture());
        assertEquals(userId, saved.getValue().getUserId());
        assertNotNull(saved.getValue().getAnonymousToken());
        assertNotEquals("deleted-two-weeks-ago", saved.getValue().getAnonymousToken());
        verify(historyService, never()).onAnonymousUserReturned(any(), eq("todays-session"));
    }
}
