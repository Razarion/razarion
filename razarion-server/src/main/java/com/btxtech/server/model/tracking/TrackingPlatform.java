package com.btxtech.server.model.tracking;

/**
 * Where a visitor is attributed to. Reddit tags with rdt_cid (stored as rdtCid), X (Twitter) with
 * twclid, Meta - Facebook and Instagram, which are one advertiser account and one campaign - with
 * fbclid.
 * <p>
 * {@link #SOCIAL} is not an ad platform: it is the account's own, unpaid posts, whose links the
 * social pipeline tags with a {@code utm_source} starting {@code social-} (see
 * {@link com.btxtech.server.service.tracking.TrackingPlatforms#isOwnPost}).
 */
public enum TrackingPlatform {
    REDDIT,
    X,
    META,
    SOCIAL
}
