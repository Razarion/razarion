package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class StartupTrackingService {
    public static final String STARTUP_TASK_COLLECTION = "startup_task";
    public static final String STARTUP_TERMINATED_COLLECTION = "startup_terminated";
    private final MongoTemplate mongoTemplate;

    public StartupTrackingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void onStartupTask(StartupTaskJson startupTaskJson) {
        startupTaskJson.setServerTime(new Date());
        mongoTemplate.save(startupTaskJson, STARTUP_TASK_COLLECTION);
    }

    public void onStartupTerminated(StartupTerminatedJson startupTerminatedJson) {
        startupTerminatedJson.setServerTime(new Date());
        mongoTemplate.save(startupTerminatedJson, STARTUP_TERMINATED_COLLECTION);
    }

    public List<StartupTaskJson> loadStartupTaskJsons(Date fromDate, Date toDate) {
        Query query = buildTimeRangeQuery(fromDate, toDate);
        return mongoTemplate.find(query, StartupTaskJson.class, STARTUP_TASK_COLLECTION);
    }

    public List<StartupTerminatedJson> loadStartupTerminatedJson(Date fromDate, Date toDate) {
        Query query = buildTimeRangeQuery(fromDate, toDate);
        return mongoTemplate.find(query, StartupTerminatedJson.class, STARTUP_TERMINATED_COLLECTION);
    }

    private Query buildTimeRangeQuery(Date fromDate, Date toDate) {
        Query query = new Query();
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate).lte(toDate));
        } else if (fromDate != null) {
            query.addCriteria(Criteria.where("serverTime").gte(fromDate));
        } else if (toDate != null) {
            query.addCriteria(Criteria.where("serverTime").lte(toDate));
        }
        return query;
    }
}
