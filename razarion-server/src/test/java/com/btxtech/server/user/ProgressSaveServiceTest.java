package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.tracking.UserActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "Save your progress": an address, a link, no password - and the address never kept.
 */
class ProgressSaveServiceTest {
    private UserRepository userRepository;
    private UserService userService;
    private RegisterService registerService;
    private AppTokenService appTokenService;
    private ProgressSaveService service;
    private UserEntity asking;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        registerService = mock(RegisterService.class);
        appTokenService = mock(AppTokenService.class);
        service = new ProgressSaveService(userRepository, userService, registerService, mock(UserActivityService.class), appTokenService, "https://example.test/");
        asking = player("asking");
        when(userService.getUserIdFromContextOrNull()).thenReturn("asking");
        when(userService.hasOpenSystemConnection("asking")).thenReturn(true);
        when(userRepository.findByUserId("asking")).thenReturn(Optional.of(asking));
        when(userRepository.findByEmailHash(anyString())).thenReturn(Optional.empty());
        when(userRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userService.isPlayerAccount(any())).thenReturn(true);
    }

    private static UserEntity account(String userId, String email) {
        UserEntity userEntity = player(userId);
        userEntity.setEmail(email);
        userEntity.setPasswordHash("{bcrypt}x");
        userEntity.setVerifiedDone();
        return userEntity;
    }

    /**
     * Saving under the address of a password account made a second player behind it (2026-11-23).
     * The mail signs in to the account instead, and nothing is saved under the asking player.
     */
    @Test
    void theAddressOfAPasswordAccountSendsASignInToIt() {
        when(userRepository.findFirstByEmailIgnoreCase("player@example.com")).thenReturn(Optional.of(account("account", "Player@Example.com")));

        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink(" Player@Example.com"));

        verify(userRepository).applyLoginLink(eq("account"), anyString(), any(Date.class), isNull());
        verify(registerService).sendProgressLink(eq("player@example.com"), anyString(), eq(ProgressSaveService.LinkMail.ACCOUNT));
        verify(userRepository, never()).applyLoginLink(eq("asking"), anyString(), any(), any());
    }

    /** The admin keeps his password: the form answers the same and sends nothing. */
    @Test
    void noSignInLinkForAnAccountWithARole() {
        UserEntity admin = account("admin", "admin@example.com");
        when(userRepository.findFirstByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(userService.isPlayerAccount(admin)).thenReturn(false);

        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("admin@example.com"));

        verify(registerService, never()).sendProgressLink(anyString(), anyString(), any());
        verify(userRepository, never()).applyLoginLink(anyString(), anyString(), any(), any());
    }

    /** An account never confirmed is cleaned away after a day - saving goes on as for a stranger. */
    @Test
    void anUnconfirmedAccountIsNoAccount() {
        UserEntity unconfirmed = player("unconfirmed");
        unconfirmed.setEmail("player@example.com");
        unconfirmed.setPasswordHash("{bcrypt}x");
        when(userRepository.findFirstByEmailIgnoreCase("player@example.com")).thenReturn(Optional.of(unconfirmed));

        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("player@example.com"));

        verify(registerService).sendProgressLink(eq("player@example.com"), anyString(), eq(ProgressSaveService.LinkMail.SAVE));
    }

    @Test
    void openingAnAccountLinkHandsOutItsLoginToken() {
        UserEntity account = account("account", "player@example.com");
        account.setProgressLinkState(null, null, ProgressSaveService.sha256Hex("the-token"), new Date(System.currentTimeMillis() + 60_000));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("the-token"))).thenReturn(Optional.of(account));
        when(appTokenService.issue("player@example.com", "")).thenReturn("jwt");

        ProgressSaveService.LinkUse use = service.useLink("the-token");

        assertEquals(ProgressSaveService.LinkResult.ACCOUNT, use.result());
        assertEquals("jwt", use.appToken());
        verify(userRepository).applyLinkUsed(eq("account"), isNull(), isNull());
        verify(userService, never()).adoptPlayer(anyString());
    }

    private static UserEntity player(String userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        return userEntity;
    }

    @Test
    void theFingerprintIgnoresCaseAndSpacesAndDoesNotContainTheAddress() {
        String hash = ProgressSaveService.hashEmail(" Player@Example.COM ");
        assertEquals(hash, ProgressSaveService.hashEmail("player@example.com"));
        assertEquals(64, hash.length());
        assertFalse(hash.contains("example"));
    }

    @Test
    void aNewAddressSavesTheAskingPlayer() {
        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("Player@Example.com"));

        ArgumentCaptor<String> linkHash = ArgumentCaptor.forClass(String.class);
        verify(userRepository).applyLoginLink(eq("asking"), linkHash.capture(), any(Date.class), eq(ProgressSaveService.hashEmail("player@example.com")));
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(registerService).sendProgressLink(eq("player@example.com"), link.capture(), eq(ProgressSaveService.LinkMail.SAVE));
        assertTrue(link.getValue().startsWith("https://example.test/rest/progress/link/"));
        // Only the hash of the link is stored, never the link.
        String token = link.getValue().substring(link.getValue().lastIndexOf('/') + 1);
        assertEquals(ProgressSaveService.sha256Hex(token), linkHash.getValue());
    }

    @Test
    void anAddressInUseSignsInThePlayerSavedUnderIt() {
        UserEntity saved = player("saved");
        when(userRepository.findByEmailHash(ProgressSaveService.hashEmail("player@example.com"))).thenReturn(Optional.of(saved));

        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("player@example.com"));

        verify(userRepository).applyLoginLink(eq("saved"), anyString(), any(Date.class), isNull());
        verify(registerService).sendProgressLink(eq("player@example.com"), anyString(), eq(ProgressSaveService.LinkMail.SIGN_IN));
        verify(userRepository, never()).applyLoginLink(eq("asking"), anyString(), any(), any());
    }

    @Test
    void noMailForSomethingThatIsNoAddress() {
        assertEquals(ProgressSaveService.RequestResult.INVALID_EMAIL, service.requestLink("not an address"));
        assertEquals(ProgressSaveService.RequestResult.INVALID_EMAIL, service.requestLink(null));
        verify(registerService, never()).sendProgressLink(anyString(), anyString(), any());
    }

    @Test
    void theFormCannotFloodAnInbox() {
        for (int i = 0; i < 3; i++) {
            assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("player@example.com"));
        }
        assertEquals(ProgressSaveService.RequestResult.TOO_MANY, service.requestLink("player@example.com"));
        assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("other@example.com"));
    }

    /**
     * The request used to create a player of its own: anyone could mail any address without ever
     * opening the game. Now it needs a player with a running game behind it.
     */
    @Test
    void noMailWithoutARunningGame() {
        when(userService.hasOpenSystemConnection("asking")).thenReturn(false);
        assertEquals(ProgressSaveService.RequestResult.NOT_IN_GAME, service.requestLink("player@example.com"));

        when(userService.getUserIdFromContextOrNull()).thenReturn(null);
        assertEquals(ProgressSaveService.RequestResult.NOT_IN_GAME, service.requestLink("player@example.com"));

        verify(registerService, never()).sendProgressLink(anyString(), anyString(), any());
        verify(userRepository, never()).applyLoginLink(anyString(), anyString(), any(), any());
    }

    /** One player cannot walk through a list of addresses. */
    @Test
    void onePlayerCannotMailManyAddresses() {
        for (int i = 0; i < ProgressSaveService.MAX_PER_PLAYER_PER_HOUR; i++) {
            assertEquals(ProgressSaveService.RequestResult.SENT, service.requestLink("player" + i + "@example.com"));
        }
        assertEquals(ProgressSaveService.RequestResult.TOO_MANY, service.requestLink("one-more@example.com"));
    }

    /**
     * Many players, many client addresses: the cap over everybody still holds, so the form can never
     * send enough mail to get razarion.com suspended at Zoho.
     */
    @Test
    void theCapOverEverybodyHolds() {
        int sent = 0;
        for (int i = 0; i < ProgressSaveService.MAX_OVERALL_PER_HOUR; i++) {
            if (service.allowed("hash" + i, "10.0.0." + i, "player" + i) == ProgressSaveService.Limit.NONE) {
                sent++;
            }
        }
        assertEquals(ProgressSaveService.MAX_OVERALL_PER_HOUR, sent);
        assertEquals(ProgressSaveService.Limit.OVERALL, service.allowed("fresh", "10.1.0.1", "fresh-player"));
        assertEquals(ProgressSaveService.RequestResult.BUSY, service.requestLink("player@example.com"));
    }

    @Test
    void aPasswordAccountIsNotOfferedASecondWay() {
        asking.setVerifiedDone();
        assertEquals(ProgressSaveService.RequestResult.REGISTERED, service.requestLink("player@example.com"));
        verify(registerService, never()).sendProgressLink(anyString(), anyString(), any());
    }

    @Test
    void openingASaveLinkKeepsTheFingerprintAndAdoptsThePlayer() {
        String pending = ProgressSaveService.hashEmail("player@example.com");
        asking.setProgressLinkState(null, pending, ProgressSaveService.sha256Hex("the-token"), new Date(System.currentTimeMillis() + 60_000));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("the-token"))).thenReturn(Optional.of(asking));

        assertEquals(ProgressSaveService.LinkResult.SAVED, service.useLink("the-token").result());

        verify(userRepository).applyLinkUsed(eq("asking"), eq(pending), any(Date.class));
        verify(userService).adoptPlayer("asking");
    }

    @Test
    void openingASignInLinkAdoptsTheSavedPlayer() {
        String hash = ProgressSaveService.hashEmail("player@example.com");
        UserEntity saved = player("saved");
        saved.setProgressLinkState(hash, null, ProgressSaveService.sha256Hex("the-token"), new Date(System.currentTimeMillis() + 60_000));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("the-token"))).thenReturn(Optional.of(saved));

        assertEquals(ProgressSaveService.LinkResult.SIGNED_IN, service.useLink("the-token").result());

        verify(userRepository).applyLinkUsed(eq("saved"), eq(hash), any(Date.class));
        verify(userService).adoptPlayer("saved");
    }

    @Test
    void anOldOrUnknownLinkDoesNothing() {
        asking.setProgressLinkState(null, "pending", ProgressSaveService.sha256Hex("old"), new Date(System.currentTimeMillis() - 1));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("old"))).thenReturn(Optional.of(asking));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("unknown"))).thenReturn(Optional.empty());

        assertEquals(ProgressSaveService.LinkResult.EXPIRED, service.useLink("old").result());
        assertEquals(ProgressSaveService.LinkResult.EXPIRED, service.useLink("unknown").result());
        verify(userService, never()).adoptPlayer(anyString());
    }

    @Test
    void anAddressTakenMeanwhileIsNotSavedTwice() {
        String pending = ProgressSaveService.hashEmail("player@example.com");
        asking.setProgressLinkState(null, pending, ProgressSaveService.sha256Hex("the-token"), new Date(System.currentTimeMillis() + 60_000));
        when(userRepository.findByLoginLinkHash(ProgressSaveService.sha256Hex("the-token"))).thenReturn(Optional.of(asking));
        when(userRepository.findByEmailHash(pending)).thenReturn(Optional.of(player("faster")));

        assertEquals(ProgressSaveService.LinkResult.EXPIRED, service.useLink("the-token").result());
        verify(userRepository).applyLinkUsed(eq("asking"), isNull(), isNull());
        verify(userService, never()).adoptPlayer(anyString());
        assertNull(asking.getEmailHash());
    }
}
