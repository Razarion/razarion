package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.DailyProgress;
import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.model.tracking.TrackingPlatform;
import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Builds the per-day funnel shown in the backend statistics, for one ad platform.
 * <p>
 * The chain that ties a level-up back to an ad click is indirect: a page request carries the
 * click id and an http session, USER_CREATED ties that session to a user id, and everything
 * after that (base, levels) is recorded against the user id only. So attribution runs
 * click id -&gt; session -&gt; user, and the counts are then filtered to those users.
 * <p>
 * Attribution reads the whole page-request history, not just the reported window: a player who
 * clicked the ad three weeks ago and levels up today still belongs to that platform. Only the
 * counting is windowed.
 * <p>
 * Aggregated on the server on purpose - shipping the raw page requests and user activities to
 * the browser just to count them would be wasteful.
 */
@Service
public class DailyProgressService {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Logger logger = LoggerFactory.getLogger(DailyProgressService.class);
    private final MongoTemplate mongoTemplate;

    public DailyProgressService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param days     number of days to report, today included
     * @param minLevel lowest level number to report a column for
     * @param maxLevel highest level number to report a column for
     * @param platform ad platform to report; only visitors carrying its click id are counted.
     *                 Null counts every visitor, organic included.
     * @return one entry per day, newest first; days without any traffic are included as zero rows
     */
    public List<DailyProgress> loadDailyProgress(int days, int minLevel, int maxLevel, TrackingPlatform platform) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate firstDay = today.minusDays(days - 1L);
        Date from = Date.from(firstDay.atStartOfDay(zone).toInstant());
        Date to = Date.from(today.plusDays(1).atStartOfDay(zone).toInstant());

        Map<String, Set<String>> homeSessions = new HashMap<>();
        Map<String, Set<String>> gameSessions = new HashMap<>();
        Set<String> platformSessionIds = new HashSet<>();
        collectPageRequests(platform, from, to, zone, homeSessions, gameSessions, platformSessionIds);

        // Null means "no platform filter": every user qualifies, so there is no id set to test
        // against and the collectors below take everyone.
        Set<String> platformUserIds = platform == null ? null : collectPlatformUserIds(platformSessionIds);
        Map<String, Set<String>> initialBaseUsers = collectInitialBaseCreated(firstDay, zone, platformUserIds);
        Map<String, Map<Integer, Set<String>>> levelUpUsers =
                collectLevelUps(from, to, zone, minLevel, maxLevel, platformUserIds);

        List<DailyProgress> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            String day = date.format(DAY_FORMAT);

            Map<Integer, Integer> levelUps = new TreeMap<>();
            Map<Integer, Set<String>> perLevel = levelUpUsers.getOrDefault(day, Map.of());
            for (int level = minLevel; level <= maxLevel; level++) {
                levelUps.put(level, perLevel.getOrDefault(level, Set.of()).size());
            }

            result.add(new DailyProgress()
                    .day(day)
                    .home(homeSessions.getOrDefault(day, Set.of()).size())
                    .game(gameSessions.getOrDefault(day, Set.of()).size())
                    .initialBaseCreated(initialBaseUsers.getOrDefault(day, Set.of()).size())
                    .levelUps(levelUps));
        }
        return result;
    }

    /**
     * One pass over every page request that carries the platform's click id: the sessions inside
     * the window become the Home/Game counts, all of them together become the attribution set.
     */
    private void collectPageRequests(TrackingPlatform platform, Date from, Date to, ZoneId zone,
                                     Map<String, Set<String>> homeSessions,
                                     Map<String, Set<String>> gameSessions,
                                     Set<String> platformSessionIds) {
        Query query = platform == null
                // No filter: only the window is needed, there is nothing to attribute.
                ? new Query(Criteria.where("serverTime").gte(from).lt(to))
                : new Query(Criteria.where(clickIdField(platform)).ne(null));
        for (PageRequest pageRequest : mongoTemplate.find(query, PageRequest.class, PageRequestService.PAGE_REQUEST)) {
            String httpSessionId = pageRequest.getHttpSessionId();
            Date serverTime = pageRequest.getServerTime();
            if (httpSessionId == null || serverTime == null
                    || (platform != null && clickId(pageRequest, platform) == null)) {
                continue;
            }
            platformSessionIds.add(httpSessionId);
            if (serverTime.before(from) || !serverTime.before(to)) {
                continue;
            }
            String day = toDay(serverTime, zone);
            if (pageRequest.getPageRequestType() == PageRequestType.HOME) {
                homeSessions.computeIfAbsent(day, key -> new HashSet<>()).add(httpSessionId);
            } else if (pageRequest.getPageRequestType() == PageRequestType.GAME) {
                gameSessions.computeIfAbsent(day, key -> new HashSet<>()).add(httpSessionId);
            }
        }
    }

    /**
     * USER_CREATED is the only activity carrying the http session, so it is the sole bridge from
     * a click id to a user id.
     */
    private Set<String> collectPlatformUserIds(Set<String> platformSessionIds) {
        if (platformSessionIds.isEmpty()) {
            return Set.of();
        }
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.USER_CREATED));
        Set<String> userIds = new HashSet<>();
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            if (userActivity.getUserId() != null && platformSessionIds.contains(userActivity.getHttpSessionId())) {
                userIds.add(userActivity.getUserId());
            }
        }
        return userIds;
    }

    /**
     * A player who lost their base and built a new one must not be counted again, so the whole
     * BASE_CREATED history is scanned for each user's earliest event. Only those earliest events
     * that fall into the reported window end up in the result.
     */
    private Map<String, Set<String>> collectInitialBaseCreated(LocalDate firstDay, ZoneId zone,
                                                               Set<String> platformUserIds) {
        Map<String, Set<String>> result = new HashMap<>();
        if (platformUserIds != null && platformUserIds.isEmpty()) {
            return result;
        }
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.BASE_CREATED));
        Map<String, Date> firstByUser = new HashMap<>();
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            if (userActivity.getUserId() == null || userActivity.getServerTime() == null
                    || (platformUserIds != null && !platformUserIds.contains(userActivity.getUserId()))) {
                continue;
            }
            firstByUser.merge(userActivity.getUserId(), userActivity.getServerTime(),
                    (existing, candidate) -> candidate.before(existing) ? candidate : existing);
        }

        firstByUser.forEach((userId, serverTime) -> {
            LocalDate date = serverTime.toInstant().atZone(zone).toLocalDate();
            if (date.isBefore(firstDay)) {
                return;
            }
            result.computeIfAbsent(date.format(DAY_FORMAT), key -> new HashSet<>()).add(userId);
        });
        return result;
    }

    private Map<String, Map<Integer, Set<String>>> collectLevelUps(Date from, Date to, ZoneId zone,
                                                                   int minLevel, int maxLevel,
                                                                   Set<String> platformUserIds) {
        Map<String, Map<Integer, Set<String>>> result = new HashMap<>();
        if (platformUserIds != null && platformUserIds.isEmpty()) {
            return result;
        }
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.LEVEL_UP)
                .and("serverTime").gte(from).lt(to));
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            if (userActivity.getUserId() == null || userActivity.getServerTime() == null
                    || (platformUserIds != null && !platformUserIds.contains(userActivity.getUserId()))) {
                continue;
            }
            Integer level = parseLevel(userActivity.getDetail());
            if (level == null || level < minLevel || level > maxLevel) {
                continue;
            }
            result.computeIfAbsent(toDay(userActivity.getServerTime(), zone), key -> new HashMap<>())
                    .computeIfAbsent(level, key -> new HashSet<>())
                    .add(userActivity.getUserId());
        }
        return result;
    }

    private String clickIdField(TrackingPlatform platform) {
        return platform == TrackingPlatform.REDDIT ? "rdtCid" : "twclid";
    }

    private String clickId(PageRequest pageRequest, TrackingPlatform platform) {
        return platform == TrackingPlatform.REDDIT ? pageRequest.getRdtCid() : pageRequest.getTwclid();
    }

    private Integer parseLevel(String detail) {
        if (detail == null) {
            return null;
        }
        try {
            return Integer.valueOf(detail.trim());
        } catch (NumberFormatException e) {
            logger.warn("Ignoring LEVEL_UP with non numeric detail: {}", detail);
            return null;
        }
    }

    private String toDay(Date date, ZoneId zone) {
        return date.toInstant().atZone(zone).toLocalDate().format(DAY_FORMAT);
    }
}
