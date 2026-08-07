package com.btxtech.server.service.tracking;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.model.tracking.PlayerSessionEvent;
import com.btxtech.server.model.tracking.PlayerSessionInfo;
import com.btxtech.server.model.tracking.PlayerSessionOutcome;
import com.btxtech.server.model.tracking.PlayerSessionTask;
import com.btxtech.server.model.tracking.TrackingPlatform;
import com.btxtech.server.model.tracking.UserActivity;
import com.btxtech.server.model.tracking.UserActivityType;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.TabHiddenJson;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the history view: one row per attempt to start the game in a time range.
 * <p>
 * Everything is assembled here rather than in the browser. The join needs the running planet (does
 * this player still own a base?) and the user table, neither of which the frontend can reach, and
 * the alternative - shipping five raw collections and correlating them by click id - is what the
 * funnel view does, where organic visitors all collapse into one bucket because they share the
 * absent click id.
 */
@Service
public class PlayerSessionService {
    private final StartupTrackingService startupTrackingService;
    private final UserActivityService userActivityService;
    private final PageRequestService pageRequestService;
    private final UserService userService;
    private final BaseItemService baseItemService;

    public PlayerSessionService(StartupTrackingService startupTrackingService,
                                UserActivityService userActivityService,
                                PageRequestService pageRequestService,
                                UserService userService,
                                BaseItemService baseItemService) {
        this.startupTrackingService = startupTrackingService;
        this.userActivityService = userActivityService;
        this.pageRequestService = pageRequestService;
        this.userService = userService;
        this.baseItemService = baseItemService;
    }

    public List<PlayerSessionInfo> load(Date fromDate, Date toDate) {
        List<StartupTaskJson> startupTasks = startupTrackingService.loadStartupTaskJsons(fromDate, toDate);
        List<StartupTerminatedJson> terminated = startupTrackingService.resolveTerminated(
                startupTasks, startupTrackingService.loadStartupTerminatedJson(fromDate, toDate));
        List<TabHiddenJson> tabHiddenJsons = startupTrackingService.loadTabHiddenJsons(fromDate, toDate);
        List<UserActivity> userActivities = userActivityService.loadUserActivities(fromDate, toDate);
        List<PageRequest> pageRequests = pageRequestService.loadPageRequests(fromDate, toDate);

        Map<String, List<StartupTaskJson>> tasksPerSession = tasksPerSession(startupTasks);
        Map<String, StartupTerminatedJson> terminatedPerSession = terminatedPerSession(terminated);
        Set<String> tabbedAwaySessions = new HashSet<>();
        tabHiddenJsons.forEach(tabHiddenJson -> tabbedAwaySessions.add(tabHiddenJson.getGameSessionUuid()));

        List<PlayerSessionInfo> rows = new ArrayList<>();
        Set<String> sessionUuids = new LinkedHashSet<>(tasksPerSession.keySet());
        sessionUuids.addAll(terminatedPerSession.keySet());
        for (String sessionUuid : sessionUuids) {
            rows.add(startupRow(sessionUuid,
                    tasksPerSession.getOrDefault(sessionUuid, List.of()),
                    terminatedPerSession.get(sessionUuid),
                    tabbedAwaySessions.contains(sessionUuid),
                    userActivities,
                    pageRequests));
        }
        rows.addAll(noStartupRows(pageRequests, tasksPerSession, terminatedPerSession, userActivities));

        // Newest first, then the per-player enrichment, which needs the whole set at once.
        rows.sort(Comparator.comparing(PlayerSessionInfo::getStartTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        enrichPlayers(rows, userActivities);
        return rows;
    }

    private PlayerSessionInfo startupRow(String sessionUuid,
                                         List<StartupTaskJson> tasks,
                                         StartupTerminatedJson terminatedJson,
                                         boolean tabbedAway,
                                         List<UserActivity> userActivities,
                                         List<PageRequest> pageRequests) {
        String userId = userIdOfAttempt(tasks, terminatedJson, sessionUuid, userActivities);
        String httpSessionId = firstHttpSessionId(tasks, terminatedJson);
        PlayerSessionInfo row = new PlayerSessionInfo()
                .rowId(sessionUuid)
                .gameSessionUuid(sessionUuid)
                .startTime(startTime(tasks, terminatedJson))
                .userId(userId)
                .userAgent(userAgent(tasks, httpSessionId, pageRequests))
                .source(source(tasks, terminatedJson, httpSessionId, pageRequests))
                .origin(origin(tasks, terminatedJson, httpSessionId, pageRequests))
                .outcome(outcome(terminatedJson))
                .tasks(tasks.stream()
                        .map(task -> new PlayerSessionTask()
                                .taskEnum(task.getTaskEnum())
                                .serverTime(task.getServerTime())
                                .duration(task.getDuration())
                                .error(task.getError()))
                        .toList());
        if (terminatedJson != null) {
            row.lastTaskEnum(terminatedJson.getLastTaskEnum())
                    .totalTime(terminatedJson.getTotalTime())
                    // The beacon knows which of the two happened, a server-derived abort does not.
                    .tabbedAway(tabbedAway || Boolean.TRUE.equals(terminatedJson.getHidden()));
        } else {
            row.tabbedAway(tabbedAway);
        }
        return row;
    }

    /**
     * Game page visits that never produced a single startup task - a browser that died before the
     * first task could report, which no other view shows at all.
     * <p>
     * Only built when the startup records of this range carry a session id. Before the server
     * started stamping it, no startup could be matched to its page visit, and every visit in the
     * range would be reported as a dead one.
     */
    private List<PlayerSessionInfo> noStartupRows(List<PageRequest> pageRequests,
                                                  Map<String, List<StartupTaskJson>> tasksPerSession,
                                                  Map<String, StartupTerminatedJson> terminatedPerSession,
                                                  List<UserActivity> userActivities) {
        Set<String> startedSessions = new HashSet<>();
        tasksPerSession.values().forEach(tasks -> tasks.forEach(task -> add(startedSessions, task.getHttpSessionId())));
        terminatedPerSession.values().forEach(terminatedJson -> add(startedSessions, terminatedJson.getHttpSessionId()));
        if (startedSessions.isEmpty()) {
            return List.of();
        }

        Map<String, String> usersPerHttpSession = usersPerHttpSession(userActivities);
        List<PlayerSessionInfo> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (PageRequest pageRequest : pageRequests) {
            String httpSessionId = pageRequest.getHttpSessionId();
            if (pageRequest.getPageRequestType() != PageRequestType.GAME
                    || httpSessionId == null
                    || startedSessions.contains(httpSessionId)
                    || !seen.add(httpSessionId)) {
                continue;
            }
            rows.add(new PlayerSessionInfo()
                    .rowId("no-startup-" + httpSessionId)
                    .startTime(pageRequest.getServerTime())
                    .userId(usersPerHttpSession.get(httpSessionId))
                    .userAgent(pageRequest.getUserAgent())
                    .source(platform(pageRequest.getRdtCid(), pageRequest.getTwclid()))
                    .outcome(PlayerSessionOutcome.NO_STARTUP)
                    .tasks(List.of()));
        }
        return rows;
    }

    /**
     * Everything that belongs to the player rather than to the single attempt: name, level, base,
     * quests, and which attempt of theirs this was.
     * <p>
     * The activities are cut along the attempts: what a player did after their second start is
     * shown on that row, not on all three. Rows arrive newest first, so a player's attempts are
     * numbered backwards through the list.
     */
    private void enrichPlayers(List<PlayerSessionInfo> rows, List<UserActivity> userActivities) {
        Set<String> userIds = new LinkedHashSet<>();
        rows.forEach(row -> add(userIds, row.getUserId()));
        Map<String, String> names = userService.getUserNames(userIds);
        Map<String, Integer> currentLevels = userService.getUserLevelNumbers(userIds);
        Map<String, List<UserActivity>> activitiesPerUser = activitiesPerUser(userActivities);

        Map<String, Integer> attemptCount = new HashMap<>();
        Map<String, Date> nextAttemptStart = new HashMap<>();
        for (PlayerSessionInfo row : rows) {
            String userId = row.getUserId();
            if (userId == null) {
                continue;
            }
            row.name(names.get(userId));
            row.baseAlive(baseItemService.getPlayerBase4UserId(userId) != null);
            row.attemptNumber(attemptCount.merge(userId, 1, Integer::sum));

            List<PlayerSessionEvent> events = new ArrayList<>();
            Integer level = null;
            boolean baseCreated = false;
            int questsPassed = 0;
            Date until = nextAttemptStart.get(userId);
            for (UserActivity userActivity : activitiesPerUser.getOrDefault(userId, List.of())) {
                if (!belongsToAttempt(userActivity, row.getStartTime(), until)) {
                    continue;
                }
                events.add(new PlayerSessionEvent()
                        .serverTime(userActivity.getServerTime())
                        .type(userActivity.getUserActivityType())
                        .detail(userActivity.getDetail()));
                switch (userActivity.getUserActivityType()) {
                    case LEVEL_UP -> {
                        Integer levelNumber = parseInt(userActivity.getDetail());
                        if (levelNumber != null && (level == null || levelNumber > level)) {
                            level = levelNumber;
                        }
                    }
                    case BASE_CREATED -> baseCreated = true;
                    case QUEST_PASSED -> questsPassed++;
                    default -> {
                    }
                }
            }
            row.events(events)
                    .baseCreated(baseCreated)
                    .questsPassed(questsPassed)
                    // A player who was already level 5 and did not level up in the window has no
                    // LEVEL_UP to read - then the user table answers, if the user is still there.
                    .levelNumber(level != null ? level : currentLevels.get(userId));
            nextAttemptStart.put(userId, row.getStartTime());
        }
    }

    /**
     * An activity belongs to the attempt that was running when it happened: after this attempt
     * started and before the player's next one did.
     */
    private boolean belongsToAttempt(UserActivity userActivity, Date attemptStart, Date nextAttemptStart) {
        Date time = userActivity.getServerTime();
        if (time == null) {
            return false;
        }
        if (attemptStart != null && time.before(attemptStart)) {
            return false;
        }
        return nextAttemptStart == null || time.before(nextAttemptStart);
    }

    private PlayerSessionOutcome outcome(StartupTerminatedJson terminatedJson) {
        if (terminatedJson == null) {
            // resolveTerminated() invents a record for every abandoned session it is sure about
            // and leaves the young ones alone - so what is left here is still booting.
            return PlayerSessionOutcome.RUNNING;
        }
        if (terminatedJson.isAborted()) {
            return PlayerSessionOutcome.ABORTED;
        }
        return terminatedJson.isSuccessful() ? PlayerSessionOutcome.SUCCESSFUL : PlayerSessionOutcome.FAILED;
    }

    /**
     * Where this attempt came from. The click ids sit on the startup records themselves, so no
     * join is needed for the common case; the page visit is only asked when they are absent, which
     * covers a startup whose click id never reached the game url.
     */
    private TrackingPlatform source(List<StartupTaskJson> tasks,
                                    StartupTerminatedJson terminatedJson,
                                    String httpSessionId,
                                    List<PageRequest> pageRequests) {
        TrackingPlatform clickIdPlatform = clickIdPlatform(tasks, terminatedJson, httpSessionId, pageRequests);
        if (clickIdPlatform != null) {
            return clickIdPlatform;
        }
        // Only when no click id was found anywhere: the campaign the visit names itself.
        TrackingPlatform utmPlatform = platformOfUtmSource(utmSource(tasks, terminatedJson, httpSessionId, pageRequests));
        if (utmPlatform != null) {
            return utmPlatform;
        }
        // And last the site the visitor came from, which needs no parameter to have survived at all.
        return platformOfOrigin(origin(tasks, terminatedJson, httpSessionId, pageRequests));
    }

    /**
     * The platform a click id names. Kept apart from the rest of {@link #source} because the origin
     * needs it too, and the origin cannot ask for the source itself - the source asks the origin.
     */
    private TrackingPlatform clickIdPlatform(List<StartupTaskJson> tasks,
                                             StartupTerminatedJson terminatedJson,
                                             String httpSessionId,
                                             List<PageRequest> pageRequests) {
        for (StartupTaskJson task : tasks) {
            TrackingPlatform platform = platform(task.getRdtCid(), task.getTwclid());
            if (platform != null) {
                return platform;
            }
        }
        if (terminatedJson != null) {
            TrackingPlatform platform = platform(terminatedJson.getRdtCid(), terminatedJson.getTwclid());
            if (platform != null) {
                return platform;
            }
        }
        if (httpSessionId != null) {
            for (PageRequest pageRequest : pageRequests) {
                if (httpSessionId.equals(pageRequest.getHttpSessionId())) {
                    TrackingPlatform platform = platform(pageRequest.getRdtCid(), pageRequest.getTwclid());
                    if (platform != null) {
                        return platform;
                    }
                }
            }
        }
        return null;
    }

    private TrackingPlatform platform(String rdtCid, String twclid) {
        if (rdtCid != null && !rdtCid.isEmpty()) {
            return TrackingPlatform.REDDIT;
        }
        if (twclid != null && !twclid.isEmpty()) {
            return TrackingPlatform.X;
        }
        return null;
    }

    /**
     * The campaign a visitor names, for the ones whose click id did not survive the trip.
     * <p>
     * A click id is lost easily - a reload without it, a link passed on, an in-app browser that
     * strips the parameter - and every one of those used to count as organic although the visit
     * still says where it came from. Over two weeks that was 155 sessions, more than a third of
     * everything the history called organic.
     */
    private static TrackingPlatform platformOfUtmSource(String utmSource) {
        if (utmSource == null) {
            return null;
        }
        String normalized = utmSource.toLowerCase();
        if (normalized.contains("reddit")) {
            return TrackingPlatform.REDDIT;
        }
        if (normalized.contains("twitter") || normalized.equals("x")) {
            return TrackingPlatform.X;
        }
        return null;
    }

    /**
     * The platform a referring site belongs to, for a visit that carries no parameter at all.
     * <p>
     * A link shared onward keeps nothing but its referrer: t.co is X's own shortener, and a session
     * arriving from it is X traffic that the history called organic because it looked for click ids
     * and campaign names and found neither.
     * <p>
     * Only the two platforms that are advertised on are mapped. A visit from a search engine is
     * genuinely organic, and saying so while the origin column names the search engine is the
     * honest answer - not a third platform invented to fill the cell.
     */
    static TrackingPlatform platformOfOrigin(String origin) {
        String host = host(origin != null ? origin : "");
        if (isHost(host, "t.co") || isHost(host, "x.com") || isHost(host, "twitter.com")) {
            return TrackingPlatform.X;
        }
        if (isHost(host, "reddit.com") || isHost(host, "redd.it")) {
            return TrackingPlatform.REDDIT;
        }
        return null;
    }

    /** The domain itself or a subdomain of it, never a host that merely ends in the same letters. */
    private static boolean isHost(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    /**
     * Where the visit came from, and only ever a place that is not us: the referrer of the landing
     * page request, then what the client itself read, then the utm source. Shown next to the
     * platform, and the only thing there is to show for a session no platform can be derived for.
     * <p>
     * The landing page comes first because it is the only request that sees the truth. Pressing
     * "Play Now" is a navigation to /game, so from there on every referrer the browser reports is
     * razarion.com - which is how this column came to answer "razarion.com" for 130 of 139
     * sessions, its own site, for visitors who had all come from X.
     */
    private String origin(List<StartupTaskJson> tasks,
                          StartupTerminatedJson terminatedJson,
                          String httpSessionId,
                          List<PageRequest> pageRequests) {
        String landingReferer = landingReferer(httpSessionId, pageRequests);
        if (landingReferer != null) {
            return landingReferer;
        }
        // No landing page in this session: the game was opened directly, and then what the client
        // read is a real origin rather than the page before it.
        for (StartupTaskJson task : tasks) {
            if (isForeign(task.getReferrer())) {
                return task.getReferrer();
            }
        }
        if (terminatedJson != null && isForeign(terminatedJson.getReferrer())) {
            return terminatedJson.getReferrer();
        }
        if (httpSessionId != null) {
            for (PageRequest pageRequest : pageRequests) {
                if (httpSessionId.equals(pageRequest.getHttpSessionId()) && isForeign(pageRequest.getReferer())) {
                    return pageRequest.getReferer();
                }
            }
        }
        String utmSource = utmSource(tasks, terminatedJson, httpSessionId, pageRequests);
        if (saysNothingBeyondThePlatform(utmSource,
                clickIdPlatform(tasks, terminatedJson, httpSessionId, pageRequests))) {
            return null;
        }
        return utmSource;
    }

    /**
     * Whether naming the utm source would only repeat the platform column next to it.
     * <p>
     * "X · twitter" says one thing twice. The utm source is worth showing when it carries something
     * the platform does not - a campaign named "newsletter" - and worth dropping when it is just
     * the platform's own name written differently.
     * <p>
     * A source that contradicts the click id is kept: two answers that disagree is information, and
     * quietly hiding one of them would leave the row looking settled when it is not.
     */
    static boolean saysNothingBeyondThePlatform(String utmSource, TrackingPlatform clickIdPlatform) {
        TrackingPlatform utmPlatform = platformOfUtmSource(utmSource);
        return utmPlatform != null && (clickIdPlatform == null || clickIdPlatform == utmPlatform);
    }

    private String landingReferer(String httpSessionId, List<PageRequest> pageRequests) {
        if (httpSessionId == null) {
            return null;
        }
        for (PageRequest pageRequest : pageRequests) {
            if (pageRequest.getPageRequestType() == PageRequestType.LANDING
                    && httpSessionId.equals(pageRequest.getHttpSessionId())
                    && isForeign(pageRequest.getReferer())) {
                return pageRequest.getReferer();
            }
        }
        return null;
    }

    /**
     * Whether a referrer names somewhere other than this site. A page of our own is not an origin:
     * it is the step before, and reporting it as the origin hides the one thing the column is for.
     * <p>
     * Anything that does not parse counts as foreign. It is not a page of ours - ours are written
     * by us - and showing an odd value beats silently dropping it.
     */
    static boolean isForeign(String referrer) {
        if (!notEmpty(referrer)) {
            return false;
        }
        String host = host(referrer);
        return !isOwnHost(host) && !host.equals("localhost");
    }

    /** The site itself and any subdomain of it - but not a host that merely ends in the same name. */
    private static boolean isOwnHost(String host) {
        return host.equals("razarion.com") || host.endsWith(".razarion.com");
    }

    private static String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String utmSource(List<StartupTaskJson> tasks,
                             StartupTerminatedJson terminatedJson,
                             String httpSessionId,
                             List<PageRequest> pageRequests) {
        for (StartupTaskJson task : tasks) {
            if (notEmpty(task.getUtmSource())) {
                return task.getUtmSource();
            }
        }
        if (terminatedJson != null && notEmpty(terminatedJson.getUtmSource())) {
            return terminatedJson.getUtmSource();
        }
        if (httpSessionId != null) {
            for (PageRequest pageRequest : pageRequests) {
                if (httpSessionId.equals(pageRequest.getHttpSessionId()) && notEmpty(pageRequest.getUtmSource())) {
                    return pageRequest.getUtmSource();
                }
            }
        }
        return null;
    }

    private static boolean notEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    /** Only the first task carries it; a page visit answers for everything that has no task at all. */
    private String userAgent(List<StartupTaskJson> tasks, String httpSessionId, List<PageRequest> pageRequests) {
        for (StartupTaskJson task : tasks) {
            if (task.getUserAgent() != null) {
                return task.getUserAgent();
            }
        }
        if (httpSessionId == null) {
            return null;
        }
        for (PageRequest pageRequest : pageRequests) {
            if (httpSessionId.equals(pageRequest.getHttpSessionId()) && pageRequest.getUserAgent() != null) {
                return pageRequest.getUserAgent();
            }
        }
        return null;
    }

    /**
     * The player behind the attempt.
     * <p>
     * GAME_SESSION_STARTED is asked first because the game itself writes it, when the session
     * opens and the player is beyond doubt. The ids stamped on the startup records answer for
     * everything that never got that far - as a vote rather than as a first hit: the first task is
     * reported before the page has attached its login token, and the server used to answer that
     * request with a fresh anonymous user. Taking the first id then named that throwaway player,
     * and the row showed level 1 without a base for someone who had been playing for weeks. The
     * vote also repairs the records already written that way.
     */
    static String userIdOfAttempt(List<StartupTaskJson> tasks,
                                  StartupTerminatedJson terminatedJson,
                                  String sessionUuid,
                                  List<UserActivity> userActivities) {
        for (UserActivity userActivity : userActivities) {
            if (userActivity.getUserActivityType() == UserActivityType.GAME_SESSION_STARTED
                    && sessionUuid.equals(userActivity.getGameSessionUuid())
                    && userActivity.getUserId() != null) {
                return userActivity.getUserId();
            }
        }
        Map<String, Integer> votes = new LinkedHashMap<>();
        tasks.forEach(task -> countUserId(votes, task.getUserId()));
        if (terminatedJson != null) {
            countUserId(votes, terminatedJson.getUserId());
        }
        // Ties go to the earliest, which is the order the ids were seen in.
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static void countUserId(Map<String, Integer> votes, String userId) {
        if (userId != null) {
            votes.merge(userId, 1, Integer::sum);
        }
    }

    private String firstHttpSessionId(List<StartupTaskJson> tasks, StartupTerminatedJson terminatedJson) {
        for (StartupTaskJson task : tasks) {
            if (task.getHttpSessionId() != null) {
                return task.getHttpSessionId();
            }
        }
        return terminatedJson != null ? terminatedJson.getHttpSessionId() : null;
    }

    private Date startTime(List<StartupTaskJson> tasks, StartupTerminatedJson terminatedJson) {
        for (StartupTaskJson task : tasks) {
            if (task.getServerTime() != null) {
                return task.getServerTime();
            }
        }
        return terminatedJson != null ? terminatedJson.getServerTime() : null;
    }

    private Map<String, List<StartupTaskJson>> tasksPerSession(List<StartupTaskJson> startupTasks) {
        Map<String, List<StartupTaskJson>> tasksPerSession = new LinkedHashMap<>();
        startupTasks.stream()
                .filter(task -> task.getGameSessionUuid() != null)
                .sorted(Comparator.comparing(StartupTaskJson::getServerTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(task -> tasksPerSession
                        .computeIfAbsent(task.getGameSessionUuid(), uuid -> new ArrayList<>())
                        .add(task));
        return tasksPerSession;
    }

    /** One record per session; the resolved list holds at most one anyway, the last one wins. */
    private Map<String, StartupTerminatedJson> terminatedPerSession(List<StartupTerminatedJson> terminated) {
        Map<String, StartupTerminatedJson> perSession = new LinkedHashMap<>();
        terminated.stream()
                .filter(terminatedJson -> terminatedJson.getGameSessionUuid() != null)
                .forEach(terminatedJson -> perSession.put(terminatedJson.getGameSessionUuid(), terminatedJson));
        return perSession;
    }

    private Map<String, List<UserActivity>> activitiesPerUser(List<UserActivity> userActivities) {
        Map<String, List<UserActivity>> perUser = new HashMap<>();
        userActivities.stream()
                .filter(userActivity -> userActivity.getUserId() != null)
                .sorted(Comparator.comparing(UserActivity::getServerTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(userActivity -> perUser
                        .computeIfAbsent(userActivity.getUserId(), userId -> new ArrayList<>())
                        .add(userActivity));
        return perUser;
    }

    private Map<String, String> usersPerHttpSession(List<UserActivity> userActivities) {
        Map<String, String> perHttpSession = new HashMap<>();
        userActivities.stream()
                .filter(userActivity -> userActivity.getUserActivityType() == UserActivityType.USER_CREATED
                        && userActivity.getHttpSessionId() != null
                        && userActivity.getUserId() != null)
                .forEach(userActivity -> perHttpSession.put(userActivity.getHttpSessionId(), userActivity.getUserId()));
        return perHttpSession;
    }

    private void add(Set<String> target, String value) {
        if (value != null) {
            target.add(value);
        }
    }

    private Integer parseInt(String value) {
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
