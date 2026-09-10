package com.btxtech.server.service.tracking;

import java.util.Date;
import java.util.List;

/**
 * What the page requests of one http session say about where that visitor came from.
 * <p>
 * The history view asks the same five questions of every row - which browser, which landing
 * referrer, which referrer at all, which utm_source, which click id - and each answer is a property
 * of the <em>session</em>, not of a single request. Reading the raw page requests to answer them
 * meant carrying 5.71 MB of documents for a 24-hour window, of which 75 % was four long strings
 * repeated on every row of the same session: the query string, the referrer, the user agent and a
 * Meta click id of about 170 characters. This is that reduced to one record per session, built by
 * the database rather than by the server.
 * <p>
 * Lists rather than single values, and deliberately so: whether a referrer counts is
 * {@link TrackingPlatforms#isForeign} - our own domain does not - and whether a utm_source counts
 * is an emptiness test. Both are Java, and moving them into an aggregation pipeline would mean
 * maintaining the same rule twice in two languages. So the pipeline answers the cheap half
 * (group by session, drop duplicates) and the caller keeps the rule. The lists are short: a session
 * has a median of three page requests and usually one distinct referrer among them.
 *
 * @param userAgents      distinct non-null user agents seen in the session
 * @param landingReferers distinct referrers of this session's LANDING requests
 * @param referers        distinct referrers of any request in the session
 * @param utmSources      distinct non-null utm_source values
 * @param rdtCid          the three click ids, taken together from the earliest request that
 * @param twclid          carried any of them - together, because
 * @param fbclid          {@link TrackingPlatforms#ofClickIds} reads them as a triple
 * @param firstGameTime   when the game page was first requested in this session, or null if it
 *                        never was. The history builds a row for a session that reached the game
 *                        without ever reporting a startup task, and this is that row's start time.
 */
public record SessionAttribution(List<String> userAgents,
                                 List<String> landingReferers,
                                 List<String> referers,
                                 List<String> utmSources,
                                 String rdtCid,
                                 String twclid,
                                 String fbclid,
                                 Date firstGameTime) {

    public static final SessionAttribution EMPTY =
            new SessionAttribution(List.of(), List.of(), List.of(), List.of(), null, null, null, null);
}
