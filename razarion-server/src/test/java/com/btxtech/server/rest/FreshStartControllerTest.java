package com.btxtech.server.rest;

import com.btxtech.server.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class FreshStartControllerTest {

    @Test
    void theButtonOnThePageClearsTheBrowser() {
        UserService userService = mock(UserService.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/fresh-start");
        request.addHeader("Sec-Fetch-Site", "same-origin");

        ResponseEntity<String> response = new FreshStartController(userService).clear(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("\"cookies\", \"storage\"", response.getHeaders().getFirst("Clear-Site-Data"));
        assertTrue(response.getHeaders().get("Set-Cookie").stream().anyMatch(c -> c.startsWith(UserService.ANONYMOUS_COOKIE + "=;") && c.contains("Max-Age=0")));
        verify(userService).forgetBrowser(request);
    }

    /** A form on another site must not wipe a visitor's unsaved game. */
    @Test
    void aFormOnAnotherSiteClearsNothing() {
        UserService userService = mock(UserService.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/fresh-start");
        request.addHeader("Sec-Fetch-Site", "cross-site");

        ResponseEntity<String> response = new FreshStartController(userService).clear(request);

        assertEquals(403, response.getStatusCode().value());
        assertNull(response.getHeaders().getFirst("Clear-Site-Data"));
        verify(userService, never()).forgetBrowser(any());
    }

    @Test
    void openingThePageClearsNothing() {
        ResponseEntity<String> response = new FreshStartController(mock(UserService.class)).page();
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getHeaders().getFirst("Clear-Site-Data"));
    }
}
