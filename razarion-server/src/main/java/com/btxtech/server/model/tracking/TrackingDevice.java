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
        // The Facebook app fetching for itself sends only its own token - no browser string, and
        // therefore none of the words above. FB4A is Facebook for Android and FBIOS is Facebook for
        // iOS, so the device is known exactly; without this the whole lot counted as desktops. See
        // isAppFetch for what those rows are.
        if (normalized.startsWith("[fban/fb4a")) {
            return MOBILE;
        }
        if (normalized.startsWith("[fban/fbios")) {
            return MOBILE;
        }
        return DESKTOP;
    }

    /**
     * Whether the Facebook app fetched this page for itself rather than a browser showing it to
     * somebody. Such a request carries the app's token alone, with no browser string in front of
     * it - a real in-app browser sends the full Mozilla string and appends the token.
     * <p>
     * Over seven days of PROD these 952 requests produced: no play click, no exit event, no game
     * page and no client start, and not one of them was ever reported visible. They are the app
     * warming a link, and counting them as landing page views made every Meta conversion rate a
     * fifth too small.
     * <p>
     * Kept as a row rather than dropped at the door - how much of this there is, is worth reading -
     * but it is not part of any funnel denominator.
     */
    public static boolean isAppFetch(String userAgent) {
        return userAgent != null && userAgent.trim().toLowerCase().startsWith("[fban");
    }
}
