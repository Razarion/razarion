package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.tracking.UserActivityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "Continue with Facebook": the code flow with only the app-scoped id, kept as a fingerprint.
 */
class FacebookLoginServiceTest {
    private static final String SECRET = "app-secret";
    private UserRepository userRepository;
    private UserService userService;
    private FacebookLoginService service;
    private UserEntity asking;

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        service = newService("12345", SECRET);
        service.setGraph((code, redirectUri) -> "fb-" + code);
        asking = new UserEntity();
        asking.setUserId("asking");
        when(userService.getOrCreateUserIdFromContext()).thenReturn("asking");
        when(userRepository.findByUserId("asking")).thenReturn(Optional.of(asking));
        when(userRepository.findByFacebookIdHash(anyString())).thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private FacebookLoginService newService(String appId, String secret) {
        return new FacebookLoginService(userRepository, userService, mock(UserActivityService.class),
                appId, secret, "v21.0", "https://fb.test", "https://graph.test", "https://example.test");
    }

    private String stateOf(String startUrl) {
        return startUrl.replaceAll(".*[?&]state=([^&]+).*", "$1");
    }

    @Test
    void offWithoutAppIdAndSecret() {
        FacebookLoginService off = newService("", "");
        assertFalse(off.isEnabled());
        assertNull(off.startUrl());
    }

    @Test
    void asksForThePublicProfileOnly() {
        String url = service.startUrl();
        assertTrue(url.startsWith("https://fb.test/v21.0/dialog/oauth?client_id=12345"));
        assertTrue(url.contains("&scope=public_profile"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fexample.test%2Frest%2Fprogress%2Ffacebook%2Fcallback"));
        assertFalse(url.contains("email"));
    }

    @Test
    void aNewFacebookAccountSavesThisPlayer() {
        String state = stateOf(service.startUrl());

        assertEquals(FacebookLoginService.CallbackResult.SAVED, service.callback("abc", state, null));

        verify(userRepository).applyFacebookLink(eq("asking"), eq(service.hashFacebookId("fb-abc")), any(Date.class));
        verify(userService).adoptPlayer("asking");
        assertFalse(service.hashFacebookId("fb-abc").contains("fb-abc"));
    }

    @Test
    void aKnownFacebookAccountContinuesItsPlayerHere() {
        UserEntity saved = new UserEntity();
        saved.setUserId("saved");
        when(userRepository.findByFacebookIdHash(service.hashFacebookId("fb-abc"))).thenReturn(Optional.of(saved));
        String state = stateOf(service.startUrl());

        assertEquals(FacebookLoginService.CallbackResult.SIGNED_IN, service.callback("abc", state, null));

        verify(userService).adoptPlayer("saved");
        verify(userRepository, never()).applyFacebookLink(anyString(), anyString(), any());
    }

    @Test
    void aWrongOrReusedStateIsRefused() {
        service.startUrl();
        assertEquals(FacebookLoginService.CallbackResult.FAILED, service.callback("abc", "forged", null));
        // The state is used up by the first callback, right or wrong.
        String state = stateOf(service.startUrl());
        assertEquals(FacebookLoginService.CallbackResult.SAVED, service.callback("abc", state, null));
        assertEquals(FacebookLoginService.CallbackResult.FAILED, service.callback("abc", state, null));
    }

    @Test
    void sayingNoAtFacebookIsACancel() {
        String state = stateOf(service.startUrl());
        assertEquals(FacebookLoginService.CallbackResult.CANCELLED, service.callback(null, state, "access_denied"));
        verify(userService, never()).adoptPlayer(anyString());
    }

    @Test
    void aDataDeletionRequestRemovesTheLink() throws Exception {
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"algorithm\":\"HMAC-SHA256\",\"user_id\":\"777\"}".getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

        assertNotNull(service.deleteData(signature + "." + payload));
        verify(userRepository).removeFacebookLink(service.hashFacebookId("777"));

        assertNull(service.deleteData("AAAA." + payload), "a request not signed by Facebook changes nothing");
    }
}
