package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
public class UserActivityService {
    public static final String USER_ACTIVITY = "user_activity";
    /**
     * The backbone of the daily funnel - USER_CREATED, BASE_CREATED and every LEVEL_UP - and cheap
     * at 1687 documents and 0.43 MB a day, so it gets the longest window of the tracking
     * collections.
     * <p>
     * Two of its three reads are deliberately unbounded: attribution has to see a user created
     * weeks ago, and "first base" means the first one ever. Ninety days is where that stops being
     * true, and it is also {@code DailyProgressService.MAX_DAYS}, past which the daily table
     * refuses to report at all.
     */
    private static final Duration RETENTION = Duration.ofDays(90);
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserActivityService.class);

    public UserActivityService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void ensureIndexes() {
        TrackingIndexes.ensureServerTimeIndex(mongoTemplate, logger, RETENTION, USER_ACTIVITY);
        // Separate call, separate try: an index that collides with an Atlas-created one fails with
        // error 85, and one failure must not take the other index with it.
        TrackingIndexes.ensureActivityTypeIndex(mongoTemplate, logger, USER_ACTIVITY);
    }

    public void onBaseCreated(String userId, int baseId) {
        try {
            var userActivity = new UserActivity()
                    .userActivityType(UserActivityType.BASE_CREATED)
                    .serverTime(new Date())
                    .userId(userId)
                    .detail(Integer.toString(baseId));
            mongoTemplate.save(userActivity, USER_ACTIVITY);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void onQuestPassed(String userId, int questConfigId, int levelNumber) {
        try {
            var userActivity = new UserActivity()
                    .userActivityType(UserActivityType.QUEST_PASSED)
                    .serverTime(new Date())
                    .userId(userId)
                    .detail(Integer.toString(questConfigId))
                    .detail2(Integer.toString(levelNumber));
            mongoTemplate.save(userActivity, USER_ACTIVITY);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void onLevelUp(String userId, int levelNumber) {
        try {
            var userActivity = new UserActivity()
                    .userActivityType(UserActivityType.LEVEL_UP)
                    .serverTime(new Date())
                    .userId(userId)
                    .detail(Integer.toString(levelNumber));
            mongoTemplate.save(userActivity, USER_ACTIVITY);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void onUserCreated(String userId, String httpSessionId) {
        try {
            var userActivity = new UserActivity()
                    .userActivityType(UserActivityType.USER_CREATED)
                    .serverTime(new Date())
                    .userId(userId)
                    .httpSessionId(httpSessionId);
            mongoTemplate.save(userActivity, USER_ACTIVITY);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Ties a startup session to the user it belongs to. Written when the client reports its
     * game session uuid, which is the one moment where the server holds both ids at once.
     */
    public void onGameSessionStarted(String userId, String gameSessionUuid) {
        try {
            var userActivity = new UserActivity()
                    .userActivityType(UserActivityType.GAME_SESSION_STARTED)
                    .serverTime(new Date())
                    .userId(userId)
                    .gameSessionUuid(gameSessionUuid);
            mongoTemplate.save(userActivity, USER_ACTIVITY);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public List<UserActivity> loadUserActivities(Date fromDate, Date toDate) {
        Query query = new Query();
        if (fromDate != null && toDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").gte(fromDate).lte(toDate)
            );
        } else if (fromDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").gte(fromDate)
            );
        } else if (toDate != null) {
            query.addCriteria(
                    Criteria.where("serverTime").lte(toDate)
            );
        }

        return mongoTemplate.find(query, UserActivity.class, USER_ACTIVITY);
    }
}
