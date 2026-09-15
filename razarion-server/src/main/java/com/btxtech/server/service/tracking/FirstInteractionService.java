package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.FirstInteractionJson;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Records the first time a player used each of the controls in a game session.
 * <p>
 * The funnel says how many mobile players stop before the first quest; it cannot say whether they
 * could steer at all. This can: a session that never reports CAMERA_PAN_TOUCH never found the
 * gesture, and that is a yes/no question, so it needs days of data rather than the weeks a rate
 * comparison needs.
 */
@Service
public class FirstInteractionService {
    public static final String FIRST_INTERACTION_COLLECTION = "first_interaction";
    /**
     * Long enough to compare a release against the weeks before it, short enough that a collection
     * nobody prunes cannot grow without end.
     * <p>
     * Was 180 days, chosen when this collection was three days old and looked small. It is not:
     * 2838 documents and 0.8 MB a day, which at 180 days settles at 168 MB of a 512 MB cluster -
     * for a signal whose every question, "did this player ever pan the camera", is answered by
     * comparing a release against the weeks around it. Sixty days holds four such comparisons and
     * settles at 56 MB.
     */
    private static final long RETENTION_DAYS = 60;
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(FirstInteractionService.class);

    public FirstInteractionService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void ensureIndexes() {
        // No expiry on the serverTime index here: this collection stamps every document with its
        // own expireAt, and the TTL index below is what acts on it. Shortening RETENTION_DAYS
        // therefore only affects documents written from now on - the ones already stored keep the
        // expireAt they were given.
        TrackingIndexes.ensureServerTimeIndex(mongoTemplate, logger, null, FIRST_INTERACTION_COLLECTION);
        TrackingIndexes.ensureExpireAtIndex(mongoTemplate, logger, FIRST_INTERACTION_COLLECTION);
    }

    /**
     * Stores one interaction. Never throws: this comes from the running game, and a player must not
     * see an error because a statistic could not be written.
     * <p>
     * Deliberately not deduplicated here. The client already reports each kind once per session, and
     * a server-side check would cost a query per call to defend against a duplicate that is harmless
     * to the analysis - every question asked of this data is "did it happen at all".
     */
    public void onFirstInteraction(FirstInteractionJson firstInteractionJson, String userId) {
        try {
            Date now = new Date();
            // Written as a plain document rather than by mapping an object with an extra expireAt
            // field. Mapping one would stamp its class name into _class, and the reader below asks
            // for FirstInteractionJson - a mismatch that only shows up when someone reads the data
            // back, long after the writing looked fine.
            Document document = new Document()
                    .append("gameSessionUuid", firstInteractionJson.getGameSessionUuid())
                    .append("kind", firstInteractionJson.getKind())
                    .append("millisSincePageLoad", firstInteractionJson.getMillisSincePageLoad())
                    .append("detail", firstInteractionJson.getDetail())
                    .append("userId", userId)
                    .append("serverTime", now)
                    .append("expireAt", new Date(now.getTime() + RETENTION_DAYS * 24 * 3600 * 1000L));
            mongoTemplate.getCollection(FIRST_INTERACTION_COLLECTION).insertOne(document);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public List<FirstInteractionJson> loadFirstInteractions(Date fromDate, Date toDate) {
        Query query = new Query();
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate).lte(toDate));
        } else if (fromDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate));
        } else if (toDate != null) {
            query.addCriteria(Criteria.where("serverTime").lte(toDate));
        }
        return mongoTemplate.find(query, FirstInteractionJson.class, FIRST_INTERACTION_COLLECTION);
    }
}
