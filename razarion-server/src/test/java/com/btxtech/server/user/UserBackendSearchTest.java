package com.btxtech.server.user;

import com.btxtech.server.rest.CallSign;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The backend user search finds an anonymous player by the call sign the chat shows. */
class UserBackendSearchTest {

    @Test
    void searchTextIsTrimmedLowerCasedAndFreeOfWildcards() {
        assertEquals("falcon-42", UserService.normalizeSearch("  Falcon-42 "));
        assertEquals("ab", UserService.normalizeSearch("a%_b"));
        assertNull(UserService.normalizeSearch(" x "));
        assertNull(UserService.normalizeSearch(null));
    }

    @Test
    void findsTheUserBehindACallSign() {
        String userId = "761c5379-30b3-4616-a208-4a34431f9d8e";
        String other = "0b8f2d1e-0000-4000-8000-000000000001";
        String callSign = CallSign.of(userId).toLowerCase();
        Set<String> found = UserService.userIdsWithCallSign(List.of(userId, other), callSign);
        assertTrue(found.contains(userId));
        assertEquals(CallSign.of(other).toLowerCase().equals(callSign) ? 2 : 1, found.size());
    }
}
