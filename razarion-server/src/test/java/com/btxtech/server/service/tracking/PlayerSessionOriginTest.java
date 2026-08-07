package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.TrackingPlatform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A page of our own is not an origin. Getting this wrong is not a visible failure but a plausible
 * wrong answer: the history reported razarion.com for 130 of 139 sessions - the site the visitors
 * were already on - while all of them had in fact come from X.
 */
class PlayerSessionOriginTest {
    @Test
    void ourOwnPagesAreNotAnOrigin() {
        assertFalse(PlayerSessionService.isForeign("https://www.razarion.com/"));
        assertFalse(PlayerSessionService.isForeign("https://razarion.com/game?utm_source=twitter"));
        assertFalse(PlayerSessionService.isForeign("https://WWW.RAZARION.COM/game"));
    }

    /** Development runs against localhost, where every referrer would otherwise look foreign. */
    @Test
    void localhostIsNotAnOrigin() {
        assertFalse(PlayerSessionService.isForeign("http://localhost:4200/game"));
    }

    @Test
    void anotherSiteIsAnOrigin() {
        assertTrue(PlayerSessionService.isForeign("https://x.com/AloRtsDev/status/2084534615864496500"));
        assertTrue(PlayerSessionService.isForeign("https://www.reddit.com/r/rts/"));
    }

    /**
     * Mail and social apps hand over a scheme of their own rather than a web url. It is still the
     * best answer there is to where the visitor came from.
     */
    @Test
    void appSchemesAreAnOrigin() {
        assertTrue(PlayerSessionService.isForeign("android-app://com.google.android.gm/"));
    }

    /** A host that only ends in ours is somebody else - notrazarion.com is not razarion.com. */
    @Test
    void aLookalikeHostIsAnOrigin() {
        assertTrue(PlayerSessionService.isForeign("https://notrazarion.com/"));
    }

    @Test
    void nothingIsNotAnOrigin() {
        assertFalse(PlayerSessionService.isForeign(null));
        assertFalse(PlayerSessionService.isForeign(""));
    }

    /** Shown rather than dropped: it is not a page of ours, and ours are written by us. */
    @Test
    void anUnparseableReferrerIsKept() {
        assertTrue(PlayerSessionService.isForeign("not a url at all"));
    }

    /**
     * A link shared onward keeps nothing but its referrer. t.co is X's own shortener, so a session
     * arriving from it is X traffic - it was counted as organic because neither a click id nor a
     * campaign name survived the trip.
     */
    @Test
    void aShortenerAndTheSiteItselfAreTheirPlatform() {
        assertEquals(TrackingPlatform.X, PlayerSessionService.platformOfOrigin("https://t.co/6TzmtWLVdT"));
        assertEquals(TrackingPlatform.X, PlayerSessionService.platformOfOrigin("https://x.com/AloRtsDev"));
        assertEquals(TrackingPlatform.X, PlayerSessionService.platformOfOrigin("https://mobile.twitter.com/"));
        assertEquals(TrackingPlatform.REDDIT, PlayerSessionService.platformOfOrigin("https://www.reddit.com/r/rts/"));
        assertEquals(TrackingPlatform.REDDIT, PlayerSessionService.platformOfOrigin("https://redd.it/abc123"));
    }

    /**
     * A visit from a search engine is organic. Naming the search engine in the origin column while
     * the platform stays empty is the honest answer; inventing a platform for it is not.
     */
    @Test
    void aSearchEngineStaysOrganic() {
        assertNull(PlayerSessionService.platformOfOrigin("https://duckduckgo.com/"));
        assertNull(PlayerSessionService.platformOfOrigin("https://search.brave.com/"));
    }

    /** The utm source reaches this as a plain word rather than a url, and is not a host. */
    @Test
    void anOriginThatIsNotAUrlMatchesNothing() {
        assertNull(PlayerSessionService.platformOfOrigin("twitter"));
        assertNull(PlayerSessionService.platformOfOrigin(null));
    }

    /**
     * "X · twitter" says one thing twice: the platform column already carries it, whether it was
     * derived from the click id or from this very utm source.
     */
    @Test
    void aUtmSourceThatRepeatsThePlatformIsDropped() {
        assertTrue(PlayerSessionService.saysNothingBeyondThePlatform("twitter", TrackingPlatform.X));
        assertTrue(PlayerSessionService.saysNothingBeyondThePlatform("twitter", null));
        assertTrue(PlayerSessionService.saysNothingBeyondThePlatform("reddit", TrackingPlatform.REDDIT));
    }

    /** A campaign that names something of its own is the reason the column exists. */
    @Test
    void aUtmSourceWithSomethingToSayIsKept() {
        assertFalse(PlayerSessionService.saysNothingBeyondThePlatform("newsletter", TrackingPlatform.X));
        assertFalse(PlayerSessionService.saysNothingBeyondThePlatform("newsletter", null));
        assertFalse(PlayerSessionService.saysNothingBeyondThePlatform(null, TrackingPlatform.X));
    }

    /** Two answers that disagree is information - hiding one leaves the row looking settled. */
    @Test
    void aUtmSourceThatContradictsTheClickIdIsKept() {
        assertFalse(PlayerSessionService.saysNothingBeyondThePlatform("twitter", TrackingPlatform.REDDIT));
    }

    /** Somebody else's domain that merely ends in ours is not ours - nor is fake-x.com X's. */
    @Test
    void aLookalikeDomainIsNotThePlatform() {
        assertNull(PlayerSessionService.platformOfOrigin("https://notx.com/"));
        assertNull(PlayerSessionService.platformOfOrigin("https://myreddit.com/"));
    }
}
