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
class TrackingPlatformsTest {
    @Test
    void ourOwnPagesAreNotAnOrigin() {
        assertFalse(TrackingPlatforms.isForeign("https://www.razarion.com/"));
        assertFalse(TrackingPlatforms.isForeign("https://razarion.com/game?utm_source=twitter"));
        assertFalse(TrackingPlatforms.isForeign("https://WWW.RAZARION.COM/game"));
    }

    /** Development runs against localhost, where every referrer would otherwise look foreign. */
    @Test
    void localhostIsNotAnOrigin() {
        assertFalse(TrackingPlatforms.isForeign("http://localhost:4200/game"));
    }

    @Test
    void anotherSiteIsAnOrigin() {
        assertTrue(TrackingPlatforms.isForeign("https://x.com/AloRtsDev/status/2084534615864496500"));
        assertTrue(TrackingPlatforms.isForeign("https://www.reddit.com/r/rts/"));
    }

    /**
     * Mail and social apps hand over a scheme of their own rather than a web url. It is still the
     * best answer there is to where the visitor came from.
     */
    @Test
    void appSchemesAreAnOrigin() {
        assertTrue(TrackingPlatforms.isForeign("android-app://com.google.android.gm/"));
    }

    /** A host that only ends in ours is somebody else - notrazarion.com is not razarion.com. */
    @Test
    void aLookalikeHostIsAnOrigin() {
        assertTrue(TrackingPlatforms.isForeign("https://notrazarion.com/"));
    }

    @Test
    void nothingIsNotAnOrigin() {
        assertFalse(TrackingPlatforms.isForeign(null));
        assertFalse(TrackingPlatforms.isForeign(""));
    }

    /** Shown rather than dropped: it is not a page of ours, and ours are written by us. */
    @Test
    void anUnparseableReferrerIsKept() {
        assertTrue(TrackingPlatforms.isForeign("not a url at all"));
    }

    /**
     * A link shared onward keeps nothing but its referrer. t.co is X's own shortener, so a session
     * arriving from it is X traffic - it was counted as organic because neither a click id nor a
     * campaign name survived the trip.
     */
    @Test
    void aShortenerAndTheSiteItselfAreTheirPlatform() {
        assertEquals(TrackingPlatform.X, TrackingPlatforms.ofOrigin("https://t.co/6TzmtWLVdT"));
        assertEquals(TrackingPlatform.X, TrackingPlatforms.ofOrigin("https://x.com/AloRtsDev"));
        assertEquals(TrackingPlatform.X, TrackingPlatforms.ofOrigin("https://mobile.twitter.com/"));
        assertEquals(TrackingPlatform.REDDIT, TrackingPlatforms.ofOrigin("https://www.reddit.com/r/rts/"));
        assertEquals(TrackingPlatform.REDDIT, TrackingPlatforms.ofOrigin("https://redd.it/abc123"));
    }

    /**
     * Meta bills Facebook and Instagram as one campaign, and its links arrive from either. The
     * mobile site and Facebook's own link shim are subdomains, and both were the referrer of real
     * arrivals on the first campaign day.
     */
    @Test
    void facebookAndInstagramAreOnePlatform() {
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("https://www.facebook.com/"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("http://m.facebook.com"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("https://l.facebook.com/l.php"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("https://instagram.com/"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("https://l.instagram.com/"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofOrigin("https://fb.me/abc"));
    }

    /**
     * The click id is the only answer that is not a guess. Meta's is the one placements deliver
     * alone: twelve of the first campaign day's arrivals carried fbclid and no utm source at all.
     */
    @Test
    void aClickIdNamesItsPlatform() {
        assertEquals(TrackingPlatform.REDDIT, TrackingPlatforms.ofClickIds("rdt-1", null, null));
        assertEquals(TrackingPlatform.X, TrackingPlatforms.ofClickIds(null, "tw-1", null));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofClickIds(null, null, "IwcGRvZgRle"));
        assertNull(TrackingPlatforms.ofClickIds(null, null, null));
        assertNull(TrackingPlatforms.ofClickIds("", "", ""));
    }

    /** What the first campaign tagged its links with: utm_source=instagram, utm_medium=paid. */
    @Test
    void aCampaignThatNamesMetaIsMeta() {
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofUtmSource("instagram"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofUtmSource("Facebook"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofUtmSource("meta_ads"));
        assertEquals(TrackingPlatform.META, TrackingPlatforms.ofUtmSource("ig"));
        assertEquals(TrackingPlatform.X, TrackingPlatforms.ofUtmSource("twitter"));
        assertEquals(TrackingPlatform.REDDIT, TrackingPlatforms.ofUtmSource("reddit"));
        assertNull(TrackingPlatforms.ofUtmSource("newsletter"));
        // "meta" only as a word of its own - a site named metacritic is not an ad network.
        assertNull(TrackingPlatforms.ofUtmSource("metacritic"));
    }

    /** "Meta · instagram" says one thing twice, the same way "X · twitter" does. */
    @Test
    void aUtmSourceThatRepeatsMetaIsDropped() {
        assertTrue(TrackingPlatforms.saysNothingBeyondThePlatform("instagram", TrackingPlatform.META));
        assertTrue(TrackingPlatforms.saysNothingBeyondThePlatform("instagram", null));
        assertFalse(TrackingPlatforms.saysNothingBeyondThePlatform("instagram", TrackingPlatform.X));
    }

    /**
     * A visit from a search engine is organic. Naming the search engine in the origin column while
     * the platform stays empty is the honest answer; inventing a platform for it is not.
     */
    @Test
    void aSearchEngineStaysOrganic() {
        assertNull(TrackingPlatforms.ofOrigin("https://duckduckgo.com/"));
        assertNull(TrackingPlatforms.ofOrigin("https://search.brave.com/"));
    }

    /** The utm source reaches this as a plain word rather than a url, and is not a host. */
    @Test
    void anOriginThatIsNotAUrlMatchesNothing() {
        assertNull(TrackingPlatforms.ofOrigin("twitter"));
        assertNull(TrackingPlatforms.ofOrigin(null));
    }

    /**
     * "X · twitter" says one thing twice: the platform column already carries it, whether it was
     * derived from the click id or from this very utm source.
     */
    @Test
    void aUtmSourceThatRepeatsThePlatformIsDropped() {
        assertTrue(TrackingPlatforms.saysNothingBeyondThePlatform("twitter", TrackingPlatform.X));
        assertTrue(TrackingPlatforms.saysNothingBeyondThePlatform("twitter", null));
        assertTrue(TrackingPlatforms.saysNothingBeyondThePlatform("reddit", TrackingPlatform.REDDIT));
    }

    /** A campaign that names something of its own is the reason the column exists. */
    @Test
    void aUtmSourceWithSomethingToSayIsKept() {
        assertFalse(TrackingPlatforms.saysNothingBeyondThePlatform("newsletter", TrackingPlatform.X));
        assertFalse(TrackingPlatforms.saysNothingBeyondThePlatform("newsletter", null));
        assertFalse(TrackingPlatforms.saysNothingBeyondThePlatform(null, TrackingPlatform.X));
    }

    /** Two answers that disagree is information - hiding one leaves the row looking settled. */
    @Test
    void aUtmSourceThatContradictsTheClickIdIsKept() {
        assertFalse(TrackingPlatforms.saysNothingBeyondThePlatform("twitter", TrackingPlatform.REDDIT));
    }

    /** Somebody else's domain that merely ends in ours is not ours - nor is fake-x.com X's. */
    @Test
    void aLookalikeDomainIsNotThePlatform() {
        assertNull(TrackingPlatforms.ofOrigin("https://notx.com/"));
        assertNull(TrackingPlatforms.ofOrigin("https://myreddit.com/"));
        assertNull(TrackingPlatforms.ofOrigin("https://notfacebook.com/"));
    }
}
