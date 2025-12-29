package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserActivityService {
    public static final String USER_ACTIVITY = "user_activity";
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserActivityService.class);

    public UserActivityService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
