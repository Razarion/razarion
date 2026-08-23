package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.TrackingDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
