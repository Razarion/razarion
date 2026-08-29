package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.TrackingPlatform;

import java.net.URI;

/**
 * Which platform a visit came from, in three steps, and everything that goes with answering it.
 * <p>
 * The three are asked in this order and they are not interchangeable. A click id is the strongest
 * answer but only a paid click carries one - an organic post is never tagged. The campaign the
 * visit names itself comes next: a click id is lost easily, by a reload without it, a link passed
 * on, an in-app browser that strips the parameter. Last the site the visitor came from, which needs
 * no parameter to have survived at all.
 * <p>
 * It lives here rather than in one of its callers because the history, the funnel and the daily
 * report all have to give the same answer. A funnel that calls a visitor organic while the history
 * next to it calls them X is worse than either answer on its own - that mismatch is what made the
 * funnel top out at level 2 with players on level 7 in the table beside it.
 */
public final class TrackingPlatforms {
    private TrackingPlatforms() {
    }

    /** The platform a click id names - the only one of the three that is not a guess. */
    public static TrackingPlatform ofClickIds(String rdtCid, String twclid, String fbclid) {
        if (notEmpty(rdtCid)) {
            return TrackingPlatform.REDDIT;
        }
        if (notEmpty(twclid)) {
            return TrackingPlatform.X;
        }
        if (notEmpty(fbclid)) {
            return TrackingPlatform.META;
        }
        return null;
    }

    /**
     * The campaign a visitor names. Over two weeks this answered for 155 sessions that were
     * reported as organic although the visit itself said where it came from.
     */
    public static TrackingPlatform ofUtmSource(String utmSource) {
        if (utmSource == null) {
            return null;
        }
        String normalized = utmSource.toLowerCase();
        if (normalized.contains("reddit")) {
            return TrackingPlatform.REDDIT;
        }
        if (normalized.contains("twitter") || normalized.equals("x")) {
            return TrackingPlatform.X;
        }
        // Meta bills one campaign across both networks, so both names answer with the same
        // platform - "instagram" is what the first campaign tagged its links with, and the
        // placement it was actually delivered on is a question for the utm source column.
        // "meta" only as a word of its own: a site named metacritic is not an ad network.
        if (normalized.contains("instagram") || normalized.contains("facebook")
                || normalized.equals("meta") || normalized.startsWith("meta_")
                || normalized.startsWith("meta-") || normalized.equals("ig") || normalized.equals("fb")) {
            return TrackingPlatform.META;
        }
        return null;
    }

    /**
     * The platform a referring site belongs to, for a visit that carries no parameter at all.
     * <p>
     * A link shared onward keeps nothing but its referrer: t.co is X's own shortener, and a session
     * arriving from it is X traffic that was called organic because the click id and the campaign
     * name were both looked for and neither was there.
     * <p>
     * Only the two platforms that are advertised on are mapped. A visit from a search engine is
     * genuinely organic, and saying so while the origin column names the search engine is the
     * honest answer - not a third platform invented to fill the cell.
     */
    public static TrackingPlatform ofOrigin(String origin) {
        String host = host(origin != null ? origin : "");
        if (isHost(host, "t.co") || isHost(host, "x.com") || isHost(host, "twitter.com")) {
            return TrackingPlatform.X;
        }
        if (isHost(host, "reddit.com") || isHost(host, "redd.it")) {
            return TrackingPlatform.REDDIT;
        }
        // m.facebook.com and l.facebook.com - the mobile site and Facebook's own link shim - are
        // subdomains and covered by the same entry. Both were seen on the first campaign day.
        if (isHost(host, "facebook.com") || isHost(host, "instagram.com")
                || isHost(host, "fb.com") || isHost(host, "fb.me") || isHost(host, "fb.watch")) {
            return TrackingPlatform.META;
        }
        return null;
    }

    /**
     * Whether naming the utm source would only repeat the platform column next to it.
     * <p>
     * "X · twitter" says one thing twice. The utm source is worth showing when it carries something
     * the platform does not - a campaign named "newsletter" - and worth dropping when it is just
     * the platform's own name written differently.
     * <p>
     * A source that contradicts the click id is kept: two answers that disagree is information, and
     * quietly hiding one of them would leave the row looking settled when it is not.
     */
    public static boolean saysNothingBeyondThePlatform(String utmSource, TrackingPlatform clickIdPlatform) {
        TrackingPlatform utmPlatform = ofUtmSource(utmSource);
        return utmPlatform != null && (clickIdPlatform == null || clickIdPlatform == utmPlatform);
    }

    /**
     * Whether a referrer names somewhere other than this site. A page of our own is not an origin:
     * it is the step before, and reporting it as the origin hides the one thing the column is for.
     * <p>
     * Anything that does not parse counts as foreign. It is not a page of ours - ours are written
     * by us - and showing an odd value beats silently dropping it.
     */
    public static boolean isForeign(String referrer) {
        if (!notEmpty(referrer)) {
            return false;
        }
        String host = host(referrer);
        return !isOwnHost(host) && !host.equals("localhost");
    }

    /** The site itself and any subdomain of it - but not a host that merely ends in the same name. */
    private static boolean isOwnHost(String host) {
        return host.equals("razarion.com") || host.endsWith(".razarion.com");
    }

    /** The domain itself or a subdomain of it, never a host that merely ends in the same letters. */
    private static boolean isHost(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    private static String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}
