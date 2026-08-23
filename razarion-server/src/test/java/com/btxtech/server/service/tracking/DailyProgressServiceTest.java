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
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The daily table, against the three things that used to be missing from it: a visitor whose
 * platform is named by anything other than a click id, a game opened without a /game record, and
 * one boot arriving as a dozen http sessions.
 * <p>
 * The mongo template is faked rather than run, because what is worth pinning down here is the
 * counting - which visitor belongs to which platform, and how many of them there are.
 */
class DailyProgressServiceTest {
    private static final String PHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15";
    private static final String DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/153.0";

    private final List<PageRequest> pageRequests = new ArrayList<>();
    private final List<StartupTaskJson> startupTasks = new ArrayList<>();
    private final List<StartupTerminatedJson> terminated = new ArrayList<>();
    private final List<UserActivity> userActivities = new ArrayList<>();
    private DailyProgressService dailyProgressService;

    @BeforeEach
    void setUp() {
        MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);
        Mockito.when(mongoTemplate.find(ArgumentMatchers.any(Query.class),
                        ArgumentMatchers.eq(PageRequest.class), ArgumentMatchers.anyString()))
                .thenReturn(pageRequests);
        // The activity collectors rely on the query to say which type they want, so the fake has to
        // answer it - handing every activity to each of them would count a level up as a base.
        Mockito.when(mongoTemplate.find(ArgumentMatchers.any(Query.class),
                        ArgumentMatchers.eq(UserActivity.class), ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> ofType(invocation.getArgument(0, Query.class)));

        StartupTrackingService startupTrackingService = Mockito.mock(StartupTrackingService.class);
        Mockito.when(startupTrackingService.loadStartupTaskJsons(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(startupTasks);
        Mockito.when(startupTrackingService.loadStartupTerminatedJson(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(terminated);

        dailyProgressService = new DailyProgressService(mongoTemplate, startupTrackingService);
    }

    /** The activities the query asked for, in the window it asked for. */
    private List<UserActivity> ofType(Query query) {
        Document criteria = query.getQueryObject();
        String wanted = String.valueOf(criteria.get("userActivityType"));
        return userActivities.stream()
                .filter(userActivity -> userActivity.getUserActivityType().name().equals(wanted))
                .toList();
    }

    @Test
    void countsAVisitorNamedOnlyByTheirUtmSource() {
        PageRequest home = pageRequest(PageRequestType.HOME, "session-1", today());
        home.setUtmSource("twitter");
        home.setUserAgent(DESKTOP);
        PageRequest game = pageRequest(PageRequestType.GAME, "session-1", today());
        game.setUtmSource("twitter");
        pageRequests.add(home);
        pageRequests.add(game);

        // Not one click id anywhere, and the funnel used to ask for nothing else.
        assertEquals(1, todayOf(TrackingPlatform.X, null).getHome());
        assertEquals(1, todayOf(TrackingPlatform.X, null).getGame());
        assertEquals(0, todayOf(TrackingPlatform.REDDIT, null).getHome());
    }

    @Test
    void countsAVisitorNamedOnlyByTheSiteTheyCameFrom() {
        PageRequest landing = pageRequest(PageRequestType.LANDING, "session-1", today());
        landing.setReferer("https://t.co/6TzmtWLVdT");
        landing.setUserAgent(DESKTOP);
        pageRequests.add(landing);
        // No query string, so no pixel and no /game record: the startup is the only trace.
        startupTasks.add(task("session-1", "game-1", today(), DESKTOP));

        DailyProgress day = todayOf(TrackingPlatform.X, null);
        assertEquals(1, day.getGame());
        // The landing page is not a funnel step and the pixel never fired.
        assertEquals(0, day.getHome());
    }

    @Test
    void countsOneBootAsOneVisitorHoweverManySessionsItArrivedIn() {
        // A browser that keeps no cookies is handed a fresh http session per beacon.
        startupTasks.add(task("session-1", "game-1", today(), PHONE));
        startupTasks.add(task("session-2", "game-1", today(), null));
        startupTasks.add(task("session-3", "game-1", today(), null));

        assertEquals(1, todayOf(null, null).getGame());
    }

    @Test
    void splitsTheDayByDevice() {
        PageRequest phoneGame = pageRequest(PageRequestType.GAME, "session-1", today());
        phoneGame.setUserAgent(PHONE);
        PageRequest desktopGame = pageRequest(PageRequestType.GAME, "session-2", today());
        desktopGame.setUserAgent(DESKTOP);
        pageRequests.add(phoneGame);
        pageRequests.add(desktopGame);

        assertEquals(2, todayOf(null, null).getGame());
        assertEquals(1, todayOf(null, TrackingDevice.MOBILE).getGame());
        assertEquals(1, todayOf(null, TrackingDevice.DESKTOP).getGame());
        assertEquals(0, todayOf(null, TrackingDevice.TABLET).getGame());
    }

    /**
     * The base and the levels are recorded against a user id alone, so they are only that
     * platform's if the session the user was created in was.
     */
    @Test
    void attributesBaseAndLevelsThroughTheSessionTheUserWasCreatedIn() {
        PageRequest game = pageRequest(PageRequestType.GAME, "session-1", today());
        game.setUtmSource("twitter");
        game.setUserAgent(PHONE);
        pageRequests.add(game);
        userActivities.add(activity(UserActivityType.USER_CREATED, "u1", "session-1", null, today()));
        userActivities.add(activity(UserActivityType.BASE_CREATED, "u1", null, null, today()));
        userActivities.add(activity(UserActivityType.LEVEL_UP, "u1", null, "2", today()));

        DailyProgress asX = todayOf(TrackingPlatform.X, null);
        assertEquals(1, asX.getInitialBaseCreated());
        assertEquals(1, asX.getLevelUps().get(2));

        DailyProgress asReddit = todayOf(TrackingPlatform.REDDIT, null);
        assertEquals(0, asReddit.getInitialBaseCreated());
        assertEquals(0, asReddit.getLevelUps().get(2));
        // And the phone it was played on is not the desktops'.
        assertEquals(0, todayOf(TrackingPlatform.X, TrackingDevice.DESKTOP).getInitialBaseCreated());
        assertEquals(1, todayOf(TrackingPlatform.X, TrackingDevice.MOBILE).getInitialBaseCreated());
    }

    @Test
    void reportsAsManyDaysAsItWasAskedFor() {
        assertEquals(10, dailyProgressService.loadDailyProgress(10, 2, 5, null, null).size());
        assertEquals(30, dailyProgressService.loadDailyProgress(30, 2, 5, null, null).size());
        // Neither nothing nor a data export.
        assertEquals(1, dailyProgressService.loadDailyProgress(0, 2, 5, null, null).size());
        assertEquals(90, dailyProgressService.loadDailyProgress(1000, 2, 5, null, null).size());
    }

    /** Newest first, so today is the first row. */
    private DailyProgress todayOf(TrackingPlatform platform, TrackingDevice device) {
        return dailyProgressService.loadDailyProgress(10, 2, 5, platform, device).get(0);
    }

    private static Date today() {
        return Date.from(LocalDate.now(ZoneId.systemDefault()).atTime(LocalTime.NOON)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private static PageRequest pageRequest(PageRequestType pageRequestType, String httpSessionId, Date serverTime) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageRequestType(pageRequestType);
        pageRequest.setHttpSessionId(httpSessionId);
        pageRequest.setServerTime(serverTime);
        return pageRequest;
    }

    private static StartupTaskJson task(String httpSessionId, String gameSessionUuid, Date serverTime,
                                        String userAgent) {
        StartupTaskJson task = new StartupTaskJson();
        task.setHttpSessionId(httpSessionId);
        task.setGameSessionUuid(gameSessionUuid);
        task.setServerTime(serverTime);
        task.setUserAgent(userAgent);
        return task;
    }

    private static UserActivity activity(UserActivityType userActivityType, String userId,
                                         String httpSessionId, String detail, Date serverTime) {
        UserActivity userActivity = new UserActivity();
        userActivity.setUserActivityType(userActivityType);
        userActivity.setUserId(userId);
        userActivity.setHttpSessionId(httpSessionId);
        userActivity.setDetail(detail);
        userActivity.setServerTime(serverTime);
        return userActivity;
    }
}
