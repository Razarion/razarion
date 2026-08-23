package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.DailyProgress;
import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.model.tracking.TrackingDevice;
import com.btxtech.server.model.tracking.TrackingPlatform;
import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
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
 * Builds the per-day funnel shown in the backend's <em>Daily</em> tab, for one ad platform and one
 * device.
 * <p>
 * The chain that ties a level-up back to a visit is indirect: a page request carries the campaign
 * parameters and an http session, USER_CREATED ties that session to a user id, and everything after
 * that (base, levels) is recorded against the user id only. So attribution runs
 * visit -&gt; session -&gt; user, and the counts are then filtered to those users.
 * <p>
 * Attribution reads the whole page-request history, not just the reported window: a player who
 * arrived three weeks ago and levels up today still belongs to that platform. Only the counting is
 * windowed.
 * <p>
 * Which platform a visit came from is answered by {@link TrackingPlatforms} - the same three steps
 * the history and the funnel use, so the three tabs describe the same population. Asking for the
 * click id alone, as this did, meant every visitor from an organic post was missing: they are never
 * tagged with one.
 * <p>
 * Aggregated on the server on purpose - shipping the raw page requests and user activities to
 * the browser just to count them would be wasteful.
 */
@Service
public class DailyProgressService {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** Longest history the daily table will report. Past this it is a data export, not a trend. */
    private static final int MAX_DAYS = 90;
    private final Logger logger = LoggerFactory.getLogger(DailyProgressService.class);
    private final MongoTemplate mongoTemplate;
    private final StartupTrackingService startupTrackingService;

    public DailyProgressService(MongoTemplate mongoTemplate, StartupTrackingService startupTrackingService) {
        this.mongoTemplate = mongoTemplate;
        this.startupTrackingService = startupTrackingService;
    }

    /**
     * @param days     number of days to report, today included
     * @param minLevel lowest level number to report a column for
     * @param maxLevel highest level number to report a column for
     * @param platform ad platform to report, or null for every visitor, organic included
     * @param device   device to report, or null for every device
     * @return one entry per day, newest first; days without any traffic are included as zero rows
     */
    public List<DailyProgress> loadDailyProgress(int days, int minLevel, int maxLevel,
                                                 TrackingPlatform platform, TrackingDevice device) {
        int reportedDays = Math.max(1, Math.min(days, MAX_DAYS));
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate firstDay = today.minusDays(reportedDays - 1L);
        Date from = Date.from(firstDay.atStartOfDay(zone).toInstant());
        Date to = Date.from(today.plusDays(1).atStartOfDay(zone).toInstant());
        boolean filtered = platform != null || device != null;

        // Without a filter there is nothing to attribute, and the window is all that is read.
        List<PageRequest> pageRequests = mongoTemplate.find(filtered
                ? new Query()
                : new Query(Criteria.where("serverTime").gte(from).lt(to)), PageRequest.class,
                PageRequestService.PAGE_REQUEST);
        List<StartupTaskJson> startupTasks = startupTrackingService.loadStartupTaskJsons(from, to);
        List<StartupTerminatedJson> terminated = startupTrackingService.loadStartupTerminatedJson(from, to);

        Map<String, Visitor> sessionVisitors = new HashMap<>();
        pageRequests.forEach(pageRequest -> attribute(sessionVisitors, signal(pageRequest)));
        startupTasks.forEach(task -> attribute(sessionVisitors, signal(task)));
        terminated.forEach(terminatedJson -> attribute(sessionVisitors, signal(terminatedJson)));

        DailyCounts counts = countWindow(pageRequests, startupTasks, terminated, from, to, zone,
                sessionVisitors, platform, device);
        Set<String> attributedUserIds = filtered
                ? attributedUserIds(sessionVisitors, platform, device) : null;
        Map<String, Set<String>> initialBaseUsers = collectInitialBaseCreated(firstDay, zone, attributedUserIds);
        Map<String, Map<Integer, Set<String>>> levelUpUsers =
                collectLevelUps(from, to, zone, minLevel, maxLevel, attributedUserIds);

        List<DailyProgress> result = new ArrayList<>();
        for (int i = 0; i < reportedDays; i++) {
            LocalDate date = today.minusDays(i);
            String day = date.format(DAY_FORMAT);

            Map<Integer, Integer> levelUps = new TreeMap<>();
            Map<Integer, Set<String>> perLevel = levelUpUsers.getOrDefault(day, Map.of());
            for (int level = minLevel; level <= maxLevel; level++) {
                levelUps.put(level, perLevel.getOrDefault(level, Set.of()).size());
            }

            result.add(new DailyProgress()
                    .day(day)
                    .home(counts.home.getOrDefault(day, Set.of()).size())
                    .playClicked(counts.playClicked.getOrDefault(day, Set.of()).size())
                    .game(counts.game.getOrDefault(day, Set.of()).size())
                    .initialBaseCreated(initialBaseUsers.getOrDefault(day, Set.of()).size())
                    .levelUps(levelUps));
        }
        return result;
    }

    /** One day's visitors per stage, each counted once however many records they left. */
    private static class DailyCounts {
        private final Map<String, Set<String>> home = new HashMap<>();
        private final Map<String, Set<String>> playClicked = new HashMap<>();
        private final Map<String, Set<String>> game = new HashMap<>();

        private void add(Map<String, Set<String>> stage, String day, String visitorKey) {
            stage.computeIfAbsent(day, key -> new HashSet<>()).add(visitorKey);
        }
    }

    /**
     * The funnel stages of the reported window, one entry per visitor and day.
     * <p>
     * Counted per visitor rather than per http session, because the two are not the same thing for
     * a browser that keeps no cookies - see {@link VisitorGroups}.
     */
    private DailyCounts countWindow(List<PageRequest> pageRequests,
                                    List<StartupTaskJson> startupTasks,
                                    List<StartupTerminatedJson> terminated,
                                    Date from, Date to, ZoneId zone,
                                    Map<String, Visitor> sessionVisitors,
                                    TrackingPlatform platform, TrackingDevice device) {
        List<Signal> windowSignals = new ArrayList<>();
        for (PageRequest pageRequest : pageRequests) {
            // The landing page record is kept for its referer, not as a funnel step. It reaches
            // further than the pixel does - it does not need a campaign parameter or a loaded
            // image - so counting it here would move the funnel's population without anything
            // about the visitors having changed. See PageRequestType.LANDING.
            if (inWindow(pageRequest.getServerTime(), from, to)
                    && pageRequest.getPageRequestType() != PageRequestType.LANDING) {
                windowSignals.add(signal(pageRequest));
            }
        }
        startupTasks.forEach(task -> windowSignals.add(signal(task)));
        terminated.forEach(terminatedJson -> windowSignals.add(signal(terminatedJson)));

        VisitorGroups groups = new VisitorGroups();
        windowSignals.forEach(signal -> groups.join(signal.handles()));

        DailyCounts counts = new DailyCounts();
        for (Signal signal : windowSignals) {
            String visitorKey = groups.keyOf(signal.handles());
            if (visitorKey == null || signal.serverTime == null
                    || !matches(visitorOf(sessionVisitors, signal), platform, device)) {
                continue;
            }
            String day = toDay(signal.serverTime, zone);
            if (signal.pageRequestType == PageRequestType.HOME) {
                counts.add(counts.home, day, visitorKey);
            } else if (signal.pageRequestType == PageRequestType.HOME_PLAY_CLICKED) {
                counts.add(counts.playClicked, day, visitorKey);
            } else if (signal.pageRequestType == PageRequestType.GAME || signal.startup) {
                // A startup record proves the game was opened as surely as the page request does,
                // and it is the only proof for a visitor who arrived without a query string:
                // /game is recorded only when it carries one (RequestInfoLoggingFilter).
                counts.add(counts.game, day, visitorKey);
            }
            // HOME_EXIT carries the dwell time and is read per visit, not as a funnel step - a
            // session that left is not a session that got further.
        }
        return counts;
    }

    /**
     * The users behind the visits of this platform and device.
     * <p>
     * USER_CREATED is the only activity carrying the http session, so it is the sole bridge from a
     * visit to a user id.
     */
    private Set<String> attributedUserIds(Map<String, Visitor> sessionVisitors,
                                          TrackingPlatform platform, TrackingDevice device) {
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.USER_CREATED));
        Set<String> userIds = new HashSet<>();
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            Visitor visitor = sessionVisitors.get(userActivity.getHttpSessionId());
            if (userActivity.getUserId() != null && visitor != null && matches(visitor, platform, device)) {
                userIds.add(userActivity.getUserId());
            }
        }
        return userIds;
    }

    private boolean matches(Visitor visitor, TrackingPlatform platform, TrackingDevice device) {
        if (visitor == null) {
            // Nothing is known about this visit, so it belongs to no platform and no device. Only
            // the unfiltered report counts it - a filter that took it would be answering a question
            // the data cannot answer.
            return platform == null && device == null;
        }
        return (platform == null || visitor.platform() == platform)
                && (device == null || visitor.device() == device);
    }

    private Visitor visitorOf(Map<String, Visitor> sessionVisitors, Signal signal) {
        return signal.httpSessionId != null ? sessionVisitors.get(signal.httpSessionId) : null;
    }

    private void attribute(Map<String, Visitor> sessionVisitors, Signal signal) {
        if (signal.httpSessionId == null) {
            return;
        }
        sessionVisitors.computeIfAbsent(signal.httpSessionId, key -> new Visitor()).add(signal);
    }

    /**
     * What is known about the visitor behind one http session: where they came from and what they
     * were holding. Accumulated over every record of that session, because no single record has to
     * carry all of it - the referrer is only on the landing page request, the click id may be gone
     * by the time the game url is reached.
     */
    private static class Visitor {
        private TrackingPlatform clickIdPlatform;
        private TrackingPlatform utmPlatform;
        private TrackingPlatform originPlatform;
        private TrackingDevice device;

        private void add(Signal signal) {
            if (clickIdPlatform == null) {
                clickIdPlatform = TrackingPlatforms.ofClickIds(signal.rdtCid, signal.twclid);
            }
            if (utmPlatform == null) {
                utmPlatform = TrackingPlatforms.ofUtmSource(signal.utmSource);
            }
            if (originPlatform == null && signal.landingReferer != null) {
                originPlatform = TrackingPlatforms.ofOrigin(signal.landingReferer);
            }
            if (device == null || device == TrackingDevice.UNKNOWN) {
                // The later beacons of a cookie-less browser carry no user agent; the visit is
                // still the device that sent the first one.
                device = TrackingDevice.of(signal.userAgent);
            }
        }

        /** The three steps of TrackingPlatforms, in their order. Null means organic. */
        private TrackingPlatform platform() {
            if (clickIdPlatform != null) {
                return clickIdPlatform;
            }
            return utmPlatform != null ? utmPlatform : originPlatform;
        }

        private TrackingDevice device() {
            return device != null ? device : TrackingDevice.UNKNOWN;
        }
    }

    /**
     * What one record says about the visitor behind it, whichever collection it came from. The
     * three collections carry the same campaign fields, which is what lets one visitor be followed
     * from the landing page into the game.
     */
    private record Signal(String httpSessionId, String gameSessionUuid, String rdtCid, String twclid,
                          String utmSource, String landingReferer, String userAgent,
                          PageRequestType pageRequestType, boolean startup, Date serverTime) {
        private List<String> handles() {
            List<String> handles = new ArrayList<>(3);
            if (notEmpty(rdtCid)) {
                handles.add("rdtCid:" + rdtCid);
            }
            if (notEmpty(twclid)) {
                handles.add("twclid:" + twclid);
            }
            if (notEmpty(gameSessionUuid)) {
                handles.add("game:" + gameSessionUuid);
            }
            if (notEmpty(httpSessionId)) {
                handles.add("session:" + httpSessionId);
            }
            return handles;
        }

        private static boolean notEmpty(String value) {
            return value != null && !value.isEmpty();
        }
    }

    private static Signal signal(PageRequest pageRequest) {
        return new Signal(pageRequest.getHttpSessionId(), null, pageRequest.getRdtCid(),
                pageRequest.getTwclid(), pageRequest.getUtmSource(),
                // Only the landing page sees where the visitor came from: the pixel is a subresource
                // of it and "Play Now" is a navigation to /game, so every referrer after it is ours.
                pageRequest.getPageRequestType() == PageRequestType.LANDING ? pageRequest.getReferer() : null,
                pageRequest.getUserAgent(), pageRequest.getPageRequestType(), false,
                pageRequest.getServerTime());
    }

    private static Signal signal(StartupTaskJson task) {
        return new Signal(task.getHttpSessionId(), task.getGameSessionUuid(), task.getRdtCid(),
                task.getTwclid(), task.getUtmSource(), null, task.getUserAgent(), null, true,
                task.getServerTime());
    }

    /** The terminated record carries no user agent; its tasks do. */
    private static Signal signal(StartupTerminatedJson terminatedJson) {
        return new Signal(terminatedJson.getHttpSessionId(), terminatedJson.getGameSessionUuid(),
                terminatedJson.getRdtCid(), terminatedJson.getTwclid(), terminatedJson.getUtmSource(),
                null, null, null, true, terminatedJson.getServerTime());
    }

    private static boolean inWindow(Date serverTime, Date from, Date to) {
        return serverTime != null && !serverTime.before(from) && serverTime.before(to);
    }

    /**
     * A player who lost their base and built a new one must not be counted again, so the whole
     * BASE_CREATED history is scanned for each user's earliest event. Only those earliest events
     * that fall into the reported window end up in the result.
     */
    private Map<String, Set<String>> collectInitialBaseCreated(LocalDate firstDay, ZoneId zone,
                                                               Set<String> attributedUserIds) {
        Map<String, Set<String>> result = new HashMap<>();
        if (attributedUserIds != null && attributedUserIds.isEmpty()) {
            return result;
        }
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.BASE_CREATED));
        Map<String, Date> firstByUser = new HashMap<>();
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            if (userActivity.getUserId() == null || userActivity.getServerTime() == null
                    || (attributedUserIds != null && !attributedUserIds.contains(userActivity.getUserId()))) {
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
                                                                   Set<String> attributedUserIds) {
        Map<String, Map<Integer, Set<String>>> result = new HashMap<>();
        if (attributedUserIds != null && attributedUserIds.isEmpty()) {
            return result;
        }
        Query query = new Query(Criteria.where("userActivityType").is(UserActivityType.LEVEL_UP)
                .and("serverTime").gte(from).lt(to));
        for (UserActivity userActivity : mongoTemplate.find(query, UserActivity.class, UserActivityService.USER_ACTIVITY)) {
            if (userActivity.getUserId() == null || userActivity.getServerTime() == null
                    || (attributedUserIds != null && !attributedUserIds.contains(userActivity.getUserId()))) {
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
