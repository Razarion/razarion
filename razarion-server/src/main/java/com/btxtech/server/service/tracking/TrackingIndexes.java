package com.btxtech.server.service.tracking;

import org.bson.Document;
import org.slf4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.time.Duration;

/**
 * The one index every tracking collection needs.
 * <p>
 * All of them are written once and read by time range - the backend asks for a window and gets the
 * whole range back. None of them had an index on serverTime, so each such read scanned the entire
 * collection.
 * <p>
 * The same index now also carries the expiry. Every caller has to name a retention, because none of
 * these collections has a natural end: measured over seven days in September 2026 they grew by
 * 16 MB a day against a 512 MB cluster, which is twelve days from the point where writes fail. How
 * long each one is worth keeping is a question only its own reader can answer, so it is asked at
 * every call site rather than defaulted here.
 */
final class TrackingIndexes {
    private TrackingIndexes() {
    }

    /**
     * Descending, because every reader wants the newest end of the range first, and expiring, so
     * the collection has a size rather than only a growth rate. One index does both: a TTL index is
     * an ordinary single-field index that Mongo also sweeps, so this costs nothing over the index
     * these collections already needed.
     * <p>
     * Retention is changed in place. The index already exists on the server without an expiry, and
     * {@code ensureIndex} with a different specification does not silently adjust it - it fails with
     * error 85 - so the conflict is caught and answered with {@code collMod}, which is the only way
     * to put an expiry on an index that is already there. That path also runs whenever a retention
     * constant below is edited: the first start after the change re-times the existing index.
     * <p>
     * Never throws: an index that could not be created is a slow backend, not a broken server, and
     * this runs during startup.
     *
     * @param retention how long a document is kept, or null for a plain index with no expiry - which
     *                  is right only where the collection expires its documents some other way.
     *                  Never pass {@link Duration#ZERO}: on this index that does not mean "no
     *                  expiry", it means "expire the moment serverTime passes", which is every
     *                  document immediately.
     */
    static void ensureServerTimeIndex(MongoTemplate mongoTemplate, Logger logger,
                                      Duration retention, String... collections) {
        if (Duration.ZERO.equals(retention)) {
            throw new IllegalArgumentException("A zero retention on serverTime empties the collection");
        }
        for (String collection : collections) {
            try {
                Index index = new Index().on("serverTime", Sort.Direction.DESC);
                mongoTemplate.indexOps(collection)
                        .ensureIndex(retention != null ? index.expire(retention) : index);
            } catch (Exception e) {
                if (retention == null) {
                    logger.warn("Could not ensure the serverTime index on {}: {}", collection, e.getMessage());
                } else {
                    retimeServerTimeIndex(mongoTemplate, logger, retention, collection, e);
                }
            }
        }
    }

    /**
     * Puts the new expiry on the serverTime index that is already there.
     * <p>
     * <b>This deletes data the moment it succeeds.</b> Mongo's background sweeper starts removing
     * everything past the retention within a minute, and nothing brings it back. Shortening a
     * constant below is therefore a one-way step, and the first server start after the edit is where
     * it happens.
     */
    private static void retimeServerTimeIndex(MongoTemplate mongoTemplate, Logger logger,
                                              Duration retention, String collection, Exception cause) {
        try {
            mongoTemplate.getDb().runCommand(new Document("collMod", collection)
                    .append("index", new Document("keyPattern", new Document("serverTime", -1))
                            .append("expireAfterSeconds", retention.toSeconds())));
            logger.info("Retention on {} is now {} days", collection, retention.toDays());
        } catch (Exception e) {
            logger.warn("Could not set the {} day retention on {}: {} (after {})",
                    retention.toDays(), collection, e.getMessage(), cause.getMessage());
        }
    }

    /**
     * For the reads that ask for one kind of activity rather than a time range.
     * <p>
     * {@code DailyProgressService} asks user_activity three separate questions - every USER_CREATED,
     * every BASE_CREATED, and the LEVEL_UPs in a window - and the collection had no index on
     * userActivityType at all, so each of the three scanned all 31,194 documents. The first two
     * carry no time bound by design: attribution has to see a user created three weeks ago, and
     * "first base" means the first one ever.
     * <p>
     * Compound and in this order, so one index serves all three: the userActivityType prefix
     * answers the two unbounded ones, and the full pair answers the windowed LEVEL_UP without a
     * separate sort. Descending on serverTime to match the other reads on this collection.
     */
    static void ensureActivityTypeIndex(MongoTemplate mongoTemplate, Logger logger, String collection) {
        try {
            mongoTemplate.indexOps(collection)
                    .ensureIndex(new Index()
                            .on("userActivityType", Sort.Direction.ASC)
                            .on("serverTime", Sort.Direction.DESC));
        } catch (Exception e) {
            logger.warn("Could not ensure the userActivityType/serverTime index on {}: {}",
                    collection, e.getMessage());
        }
    }

    /**
     * Per-document expiry: Mongo drops a document once its own expireAt has passed, which is what
     * expireAfterSeconds(0) means. Documents without the field are never dropped.
     * <p>
     * One try per collection on purpose, like above. Atlas creates indexes of its own, and an
     * ensureIndex that collides with one of them fails with error 85 - inside a shared try block
     * that failure would also skip every collection after it.
     */
    static void ensureExpireAtIndex(MongoTemplate mongoTemplate, Logger logger, String... collections) {
        for (String collection : collections) {
            try {
                mongoTemplate.indexOps(collection)
                        .ensureIndex(new Index().on("expireAt", Sort.Direction.ASC).expire(Duration.ZERO));
            } catch (Exception e) {
                logger.warn("Could not ensure the expireAt TTL index on {}: {}", collection, e.getMessage());
            }
        }
    }
}
