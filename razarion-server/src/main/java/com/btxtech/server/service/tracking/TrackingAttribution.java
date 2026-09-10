package com.btxtech.server.service.tracking;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grouping a tracking collection down to one attribution record per session or attempt.
 * <p>
 * Both tracking collections the history reads have the same shape of waste: every row carries its
 * own full copy of where the visitor came from - the referrer, the user agent, the click id - and a
 * single visit produces several rows. A page request session has a median of three, a startup
 * attempt has about seven. The history wants those values once per session, so the grouping belongs
 * in the database rather than in a scan on the server.
 */
final class TrackingAttribution {
    private TrackingAttribution() {
    }

    /**
     * Missing and null are the same absence here, and without {@code $ifNull} they are not:
     * an absent field resolves to "missing", and {@code {$ne: [missing, null]}} is <em>true</em> in
     * an aggregation expression - the opposite of what the same shape means in a {@code $match}.
     * Found by comparing this against the scan it replaces, on production data: every row without
     * click ids was taking the "has one" branch.
     */
    static Document notNull(String field) {
        return new Document("$ne", Arrays.asList(new Document("$ifNull", Arrays.asList(field, null)), null));
    }

    /**
     * The click ids as one triple from the earliest row that carried any of them, because
     * {@link TrackingPlatforms#ofClickIds} reads the three together.
     * <p>
     * A null and a {@code $filter} rather than {@code $$REMOVE}: that variable is honoured by
     * {@code $addToSet} but not by {@code $push}, where it leaves an empty document in the array -
     * which would hide a real click id behind it for a visit that arrived without one and picked
     * one up later.
     */
    static Document clickIds() {
        return new Document("$push", new Document("$cond", Arrays.asList(
                new Document("$or", List.of(notNull("$rdtCid"), notNull("$twclid"), notNull("$fbclid"))),
                new Document("rdtCid", "$rdtCid").append("twclid", "$twclid").append("fbclid", "$fbclid"),
                null)));
    }

    static Document filterNulls(String field) {
        return new Document("$addFields", new Document(field,
                new Document("$filter", new Document("input", "$" + field)
                        .append("cond", new Document("$ne", Arrays.asList("$$this", null))))));
    }

    /** {@code $$REMOVE} keeps the absent value out of the set rather than making it a member. */
    static Document addToSetNonNull(String field) {
        return new Document("$addToSet", new Document("$ifNull", Arrays.asList(field, "$$REMOVE")));
    }

    /**
     * The generic half: group by {@code keyField}, keep the distinct user agents, referrers and
     * utm sources, and the first click id triple. Callers that need more - the page requests want a
     * landing referrer and the first game request - build their own pipeline on these parts.
     *
     * @param refererField the collection's own spelling; page requests say {@code referer} and
     *                     startup rows say {@code referrer}
     */
    static Map<String, SessionAttribution> perKey(MongoTemplate mongoTemplate, String collection,
                                                  Document match, String keyField, String refererField) {
        List<Document> pipeline = new ArrayList<>();
        Document scoped = new Document(match);
        scoped.append(keyField, new Document("$ne", null));
        pipeline.add(new Document("$match", scoped));
        pipeline.add(new Document("$sort", new Document("serverTime", 1)));
        pipeline.add(new Document("$group", new Document("_id", "$" + keyField)
                .append("userAgents", addToSetNonNull("$userAgent"))
                .append("referers", addToSetNonNull("$" + refererField))
                .append("utmSources", addToSetNonNull("$utmSource"))
                .append("clickIds", clickIds())));
        pipeline.add(filterNulls("clickIds"));

        Map<String, SessionAttribution> perKey = new HashMap<>();
        for (Document document : mongoTemplate.getCollection(collection).aggregate(pipeline)) {
            String key = document.getString("_id");
            if (key == null) {
                continue;
            }
            perKey.put(key, of(document, List.of(), null));
        }
        return perKey;
    }

    static SessionAttribution of(Document document, List<String> landingReferers, java.util.Date firstGameTime) {
        List<Document> clickIds = document.getList("clickIds", Document.class, List.of());
        Document first = clickIds.isEmpty() ? new Document() : clickIds.get(0);
        return new SessionAttribution(
                strings(document, "userAgents"),
                landingReferers,
                strings(document, "referers"),
                strings(document, "utmSources"),
                first.getString("rdtCid"),
                first.getString("twclid"),
                first.getString("fbclid"),
                firstGameTime);
    }

    static List<String> strings(Document document, String field) {
        return document.getList(field, String.class, List.of());
    }
}
