package com.btxtech.server.model.tracking;

/**
 * Ad platform a visitor is attributed to, identified by the click id its links carry.
 * Reddit tags with rdt_cid (stored as rdtCid), X (Twitter) with twclid, Meta - Facebook and
 * Instagram, which are one advertiser account and one campaign - with fbclid.
 */
public enum TrackingPlatform {
    REDDIT,
    X,
    META
}
