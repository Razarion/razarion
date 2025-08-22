package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StartupTrackingService {
    public static final String STARTUP_TRACKING = "startup_tracking";
    private final MongoTemplate mongoTemplate;

    public StartupTrackingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void onStartupTask(StartupTaskJson startupTaskJson) {
        startupTaskJson.setServerTime(new Date());
        mongoTemplate.save(startupTaskJson, STARTUP_TRACKING);
    }

    public void onStartupTerminated(StartupTerminatedJson startupTerminatedJson) {
        startupTerminatedJson.setServerTime(new Date());
        mongoTemplate.save(startupTerminatedJson, STARTUP_TRACKING);
    }

    public List<StartupTerminatedJson> loadStartupTerminatedJson() {
        Query query = new Query()
                .addCriteria(Criteria.where("_class").is("com.btxtech.shared.dto.StartupTerminatedJson"))
                .with(Sort.by(Sort.Direction.DESC, "serverTime"));
        List<StartupTerminatedJson> terminatedList = mongoTemplate.find(query, StartupTerminatedJson.class, STARTUP_TRACKING);

        List<StartupTaskJson> allTasks = mongoTemplate.findAll(StartupTaskJson.class, STARTUP_TRACKING);
        Map<String, List<StartupTaskJson>> tasksBySession = allTasks.stream()
                .collect(Collectors.groupingBy(StartupTaskJson::getGameSessionUuid));


        Set<String> existingTerminatedSessions = terminatedList.stream()
                .map(StartupTerminatedJson::getGameSessionUuid)
                .collect(Collectors.toSet());

        tasksBySession.forEach((sessionId, tasks) -> {
            if (!existingTerminatedSessions.contains(sessionId)) {
                StartupTerminatedJson generated = new StartupTerminatedJson()
                        .gameSessionUuid(sessionId)
                        .serverTime(new Date())
                        .successful(false) // oder Logik anpassen
                        .totalTime(tasks.stream().mapToInt(StartupTaskJson::getDuration).sum());
                mongoTemplate.save(generated, STARTUP_TRACKING);
                terminatedList.add(generated);
            }
        });

        return terminatedList;
    }

    public List<StartupTaskJson> loadStartupTaskJson(String gameSessionUuid) {
        Query query = new Query()
                .addCriteria(Criteria.where("gameSessionUuid").is(gameSessionUuid))
                .with(Sort.by(Sort.Direction.ASC, "startTime"));
        return mongoTemplate.find(query, StartupTaskJson.class, STARTUP_TRACKING);
    }
}
