package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StartupTrackingService {
    public static final String STARTUP_TASK_COLLECTION = "startup_task";
    public static final String STARTUP_TERMINATED_COLLECTION = "startup_terminated";
    /**
     * A session whose last task is younger than this is not called aborted yet - it is probably
     * still booting. Generous on purpose: the slowest successful startups take about four
     * minutes, dominated by the model download.
     */
    private static final long ABORT_GRACE_MILLIS = 5 * 60 * 1000L;
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

    /**
     * Turns the stored records into the list the startup view shows: every startup that ran to
     * its own end, plus one entry per startup that never did.
     * <p>
     * An abandoned startup leaves no {@link StartupTerminatedJson} of its own - the client is
     * simply gone. Such sessions used to vanish from the startup list entirely, which made it
     * look almost failure-free while the funnel showed a third of the players never reaching the
     * game. Two sources fill the gap:
     * <ul>
     * <li>the page's own beacon, which reports leaving mid startup. It also fires when a tab
     * merely goes to the background, so an abort is dropped again as soon as the same session
     * reports a real end.</li>
     * <li>reconstruction from the startup tasks, for everything the beacon cannot catch - browser
     * killed, network gone, tab discarded. This also works on data recorded before the beacon
     * existed.</li>
     * </ul>
     * Sessions whose last task is younger than {@link #ABORT_GRACE_MILLIS} are left alone: those
     * are most likely still booting right now.
     */
    public List<StartupTerminatedJson> resolveTerminated(List<StartupTaskJson> startupTaskJsons,
                                                         List<StartupTerminatedJson> startupTerminatedJsons) {
        Set<String> endedUuids = startupTerminatedJsons.stream()
                .filter(startupTerminatedJson -> !startupTerminatedJson.isAborted())
                .map(StartupTerminatedJson::getGameSessionUuid)
                .collect(Collectors.toSet());

        List<StartupTerminatedJson> resolved = startupTerminatedJsons.stream()
                // An abort that names no session belongs to nobody: it can neither be dropped when
                // that session finished after all nor be told apart from the same startup
                // reconstructed below, which is where the duplicated aborted rows came from. The
                // session is reconstructed from its tasks anyway, so nothing is lost by ignoring it.
                .filter(startupTerminatedJson -> !startupTerminatedJson.isAborted()
                        || hasSession(startupTerminatedJson))
                // A session that came back and finished is not an abort, whatever the beacon said.
                .filter(startupTerminatedJson -> !startupTerminatedJson.isAborted()
                        || !endedUuids.contains(startupTerminatedJson.getGameSessionUuid()))
                .collect(Collectors.toCollection(ArrayList::new));

        Set<String> knownUuids = resolved.stream()
                .map(StartupTerminatedJson::getGameSessionUuid)
                .collect(Collectors.toSet());
        long youngest = System.currentTimeMillis() - ABORT_GRACE_MILLIS;

        Map<String, StartupTaskJson> lastTaskPerSession = new LinkedHashMap<>();
        for (StartupTaskJson startupTaskJson : startupTaskJsons) {
            String uuid = startupTaskJson.getGameSessionUuid();
            if (uuid == null || knownUuids.contains(uuid)) {
                continue;
            }
            StartupTaskJson known = lastTaskPerSession.get(uuid);
            if (known == null || isAfter(startupTaskJson.getServerTime(), known.getServerTime())) {
                lastTaskPerSession.put(uuid, startupTaskJson);
            }
        }

        lastTaskPerSession.values().stream()
                .filter(lastTask -> lastTask.getServerTime() == null || lastTask.getServerTime().getTime() < youngest)
                .map(lastTask -> new StartupTerminatedJson()
                        .successful(false)
                        .aborted(true)
                        .lastTaskEnum(lastTask.getTaskEnum())
                        // No duration: nobody ever reported an end, so any number would be made up.
                        .totalTime(null)
                        .gameSessionUuid(lastTask.getGameSessionUuid())
                        .serverTime(lastTask.getServerTime())
                        // Carried over so the platform filter sees these like any other row.
                        .rdtCid(lastTask.getRdtCid())
                        .twclid(lastTask.getTwclid())
                        .utmCampaign(lastTask.getUtmCampaign())
                        .utmSource(lastTask.getUtmSource()))
                .forEach(resolved::add);

        // The reconstructed aborts are appended at the end, so the list has to be put back into
        // chronological order - the view shows it as is, in the table and on the curve.
        resolved.sort(Comparator.comparing(StartupTerminatedJson::getServerTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return resolved;
    }

    private boolean hasSession(StartupTerminatedJson startupTerminatedJson) {
        String uuid = startupTerminatedJson.getGameSessionUuid();
        return uuid != null && !uuid.isEmpty();
    }

    private boolean isAfter(Date candidate, Date known) {
        if (candidate == null) {
            return false;
        }
        return known == null || candidate.after(known);
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
