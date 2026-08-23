package com.btxtech.server.model.tracking;

/**
 * What the visitor was holding, as far as the user agent admits it.
 * <p>
 * UNKNOWN is not a device but a missing user agent: the later beacons of a cookie-less browser
 * arrive without one, and a crawler sends none at all. It is a value of its own rather than being
 * folded into the desktops, because how much of the traffic cannot be classified is worth reading.
 */
public enum TrackingDevice {
    MOBILE,
    TABLET,
    DESKTOP,
    UNKNOWN;

    /**
     * Tablet before mobile: an Android tablet carries "Android" too. iPadOS sends a Macintosh user
     * agent with no phone token at all and therefore lands in DESKTOP - the same error the Controls
     * tab reports rather than corrects, because correcting it would be a guess.
     * <p>
     * Kept in step with classifyDevice() in first-interaction-analyzer.ts, so the funnel and this
     * agree on what a phone is.
     */
    public static TrackingDevice of(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }
        String normalized = userAgent.toLowerCase();
        if (normalized.contains("ipad") || normalized.contains("tablet")) {
            return TABLET;
        }
        if (normalized.contains("mobile") || normalized.contains("android")
                || normalized.contains("iphone") || normalized.contains("ipod")) {
            return MOBILE;
        }
        return DESKTOP;
    }
}
