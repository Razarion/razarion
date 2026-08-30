package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.TrackingDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Whether the phones stop where the desktops do is the question the funnel was split for, so the
 * split has to hold. It also has to agree with classifyDevice() in the frontend, which answers the
 * same question for the funnel table and the Controls tab.
 */
class TrackingDeviceTest {
    @Test
    void aPhoneIsMobile() {
        assertEquals(TrackingDevice.MOBILE, TrackingDevice.of(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15"));
        assertEquals(TrackingDevice.MOBILE, TrackingDevice.of(
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/139.0.0.0 Mobile Safari/537.36"));
    }

    /** An Android tablet carries "Android" too, so tablet has to be tested first. */
    @Test
    void aTabletIsATabletAndNotAPhone() {
        assertEquals(TrackingDevice.TABLET, TrackingDevice.of(
                "Mozilla/5.0 (iPad; CPU OS 18_5 like Mac OS X) AppleWebKit/605.1.15"));
        assertEquals(TrackingDevice.TABLET, TrackingDevice.of(
                "Mozilla/5.0 (Linux; Android 13; Tablet) AppleWebKit/537.36 Mobile Safari/537.36"));
    }

    @Test
    void aComputerIsDesktop() {
        assertEquals(TrackingDevice.DESKTOP, TrackingDevice.of(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:153.0) Gecko/20100101 Firefox/153.0"));
        assertEquals(TrackingDevice.DESKTOP, TrackingDevice.of(
                "Mozilla/5.0 (X11; CrOS x86_64 14541.0.0) AppleWebKit/537.36"));
    }

    /**
     * Not a device: the later beacons of a cookie-less browser carry no user agent, and a crawler
     * sends none at all. Folding those into the desktops would inflate the one group that is
     * already the biggest.
     */
    @Test
    void nothingIsUnknownRatherThanDesktop() {
        assertEquals(TrackingDevice.UNKNOWN, TrackingDevice.of(null));
        assertEquals(TrackingDevice.UNKNOWN, TrackingDevice.of(""));
    }

    /**
     * The user agent that made every Meta conversion rate wrong. The Facebook app fetching a link
     * for itself sends its own token and no browser string, so it contains none of the words the
     * classification looks for and landed in DESKTOP - 937 of 952 over seven days, all of them
     * phones. The screen is in the string: FBDM says 1080 by 2340.
     */
    @Test
    void theFacebookAppsOwnFetchIsAPhone() {
        assertEquals(TrackingDevice.MOBILE, TrackingDevice.of(
                "[FBAN/FB4A;FBAV/575.1.0.55.73;FBBV/1046287430;FBDM/{density=2.8125,width=1080,height=2340};FBLC/en_GB;]"));
        assertEquals(TrackingDevice.MOBILE, TrackingDevice.of("[FBAN/FBIOS;FBAV/510.0.0.44.107;FBBV/1234;]"));
    }

    /**
     * And it is not a visit. It fires the pixel like any other render but can never click, so left
     * in the funnel it sits in the denominator of every rate while being incapable of appearing in
     * any numerator.
     */
    @Test
    void theAppsOwnFetchIsNotAVisit() {
        assertTrue(TrackingDevice.isAppFetch("[FBAN/FB4A;FBAV/575.1.0.55.73;]"));
        assertTrue(TrackingDevice.isAppFetch("  [FBAN/FBIOS;FBAV/510.0.0.44.107;]"));
    }

    /**
     * The in-app browser is the opposite case and must stay in: a person really is looking at the
     * page. It sends the whole browser string and appends the token, so the difference is whether
     * the token stands alone.
     */
    @Test
    void theInAppBrowserIsAVisitorLikeAnyOther() {
        String inAppBrowser = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/151.0.0.0 Mobile Safari/537.36 [FBAN/FBAV;FBAV/575.0.0.48.76;]";
        assertFalse(TrackingDevice.isAppFetch(inAppBrowser));
        assertEquals(TrackingDevice.MOBILE, TrackingDevice.of(inAppBrowser));
    }

    @Test
    void anOrdinaryUserAgentIsNotAnAppFetch() {
        assertFalse(TrackingDevice.isAppFetch(null));
        assertFalse(TrackingDevice.isAppFetch(""));
        assertFalse(TrackingDevice.isAppFetch(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/149 Safari/537.36"));
    }
}
