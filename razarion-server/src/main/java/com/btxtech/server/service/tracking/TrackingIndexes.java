package com.btxtech.server.service.tracking;

import org.slf4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * The one index every tracking collection needs.
 * <p>
 * All of them are written once and read by time range - the backend asks for a window and gets the
 * whole range back. None of them had an index on serverTime, so each such read scanned the entire
 * collection, and none of them has a TTL either, so they only ever get bigger.
 */
final class TrackingIndexes {
    private TrackingIndexes() {
    }

    /**
     * Descending, because every reader wants the newest end of the range first.
     * <p>
     * Never throws: an index that could not be created is a slow backend, not a broken server, and
     * this runs during startup.
     */
    static void ensureServerTimeIndex(MongoTemplate mongoTemplate, Logger logger, String... collections) {
        for (String collection : collections) {
            try {
                mongoTemplate.indexOps(collection)
                        .ensureIndex(new Index().on("serverTime", Sort.Direction.DESC));
            } catch (Exception e) {
                logger.warn("Could not ensure the serverTime index on {}: {}", collection, e.getMessage());
            }
        }
    }
}
