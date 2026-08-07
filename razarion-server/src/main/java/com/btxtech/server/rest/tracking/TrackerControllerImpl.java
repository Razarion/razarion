package com.btxtech.server.rest.tracking;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.tracking.DailyProgress;
import com.btxtech.server.model.tracking.PlayerSessionInfo;
import com.btxtech.server.model.tracking.TrackingContainer;
import com.btxtech.server.model.tracking.TrackingPlatform;
import com.btxtech.server.model.tracking.TrackingRequest;
import com.btxtech.server.service.tracking.DailyProgressService;
import com.btxtech.server.service.tracking.FirstInteractionService;
import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.service.tracking.PlayerSessionService;
import com.btxtech.server.service.tracking.StartupTrackingService;
import com.btxtech.server.service.tracking.TipStallService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.FirstInteractionJson;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.TabHiddenJson;
import com.btxtech.shared.dto.TipStallJson;
import com.btxtech.shared.rest.TrackerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.btxtech.shared.CommonUrl.APPLICATION_PATH;
import static com.btxtech.shared.CommonUrl.TRACKER_PATH;

@RestController
@RequestMapping(APPLICATION_PATH + "/" + TRACKER_PATH)
public class TrackerControllerImpl implements TrackerController {
    private final Logger logger = LoggerFactory.getLogger(TrackerControllerImpl.class);
    /**
     * Days shown in the daily funnel table.
     */
    private static final int DAILY_PROGRESS_DAYS = 10;
    /**
     * Lowest level with its own column. Level 1 comes free with the first base and never emits a
     * LEVEL_UP, so a column for it would read zero forever - "Initial Base created" is the
     * level 1 number.
     */
    private static final int DAILY_PROGRESS_MIN_LEVEL = 2;
    /**
     * Highest level with its own column in the daily funnel table.
     */
    private static final int DAILY_PROGRESS_MAX_LEVEL = 5;
    private final StartupTrackingService startupTrackingService;
    private final PageRequestService pageRequestService;
    private final UserActivityService userActivityService;
    private final DailyProgressService dailyProgressService;
    private final TipStallService tipStallService;
    private final FirstInteractionService firstInteractionService;
    private final UserService userService;
    private final PlayerSessionService playerSessionService;

    public TrackerControllerImpl(StartupTrackingService startupTrackingService,
                                 PageRequestService pageRequestService,
                                 UserActivityService userActivityService,
                                 DailyProgressService dailyProgressService,
                                 TipStallService tipStallService,
                                 FirstInteractionService firstInteractionService,
                                 UserService userService,
                                 PlayerSessionService playerSessionService) {
        this.playerSessionService = playerSessionService;
        this.startupTrackingService = startupTrackingService;
        this.pageRequestService = pageRequestService;
        this.userActivityService = userActivityService;
        this.dailyProgressService = dailyProgressService;
        this.tipStallService = tipStallService;
        this.firstInteractionService = firstInteractionService;
        this.userService = userService;
    }

    @Override
    @PostMapping(value = "startupTask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startupTask(@RequestBody StartupTaskJson startupTaskJson) {
        // The session first: it is what every later join runs on, and it must exist even for the
        // very first task, which arrives before anything else has asked for one.
        startupTaskJson.setHttpSessionId(userService.getOrCreateHttpSessionId());
        startupTaskJson.setUserId(currentUserId());
        startupTrackingService.onStartupTask(startupTaskJson);
    }

    @Override
    @PostMapping(value = "startupTerminated", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startupTerminated(@RequestBody StartupTerminatedJson startupTerminatedJson) {
        startupTerminatedJson.setHttpSessionId(userService.getOrCreateHttpSessionId());
        startupTerminatedJson.setUserId(currentUserId());
        startupTrackingService.onStartupTerminated(startupTerminatedJson);
    }

    /**
     * Who is starting up here, as far as it is already known. Never a player created for the
     * occasion: tracking watches a startup, it does not begin one.
     * <p>
     * The first task is reported by the page itself, before the Angular app has attached the login
     * token, so a request from a logged-in player is indistinguishable from a first-time visitor at
     * that moment. Answering it with a new anonymous user wrote a throwaway player into the
     * database on every start and made the history show the attempt under that id - unnamed, level
     * one, baseless. Null instead: the tasks that follow carry whoever it really is.
     * <p>
     * Never let it break the tracking call: a startup record without a user is still worth having,
     * and a failed POST would be reported as a broken startup.
     */
    private String currentUserId() {
        try {
            return userService.getUserIdFromContextOrNull();
        } catch (Exception e) {
            logger.warn("Could not determine the user of a startup record: {}", e.getMessage());
            return null;
        }
    }

    /**
     * The running game went to the background. Sent by the page's beacon, so it arrives without
     * a session context and carries only what the page knows.
     */
    @Override
    @PostMapping(value = "tabHidden", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void tabHidden(@RequestBody TabHiddenJson tabHiddenJson) {
        startupTrackingService.onTabHidden(tabHiddenJson);
    }

    /**
     * A tip that stopped guiding the player. Comes from the running game, so the user is known -
     * that is what makes a stall comparable with the quest it belongs to.
     */
    @Override
    @PostMapping(value = "tipStall", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void tipStall(@RequestBody TipStallJson tipStallJson) {
        tipStallService.onTipStall(tipStallJson, userService.getOrCreateUserIdFromContext());
    }

    /**
     * The player used one of the controls for the first time this session. Comes from the running
     * game like tipStall does, so the user is known.
     */
    @Override
    @PostMapping(value = "firstInteraction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void firstInteraction(@RequestBody FirstInteractionJson firstInteractionJson) {
        firstInteractionService.onFirstInteraction(firstInteractionJson,
                userService.getOrCreateUserIdFromContext());
    }

    @PostMapping(value = "loadTrackingContainer",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')") 
    public TrackingContainer loadTrackingContainer(@RequestBody TrackingRequest trackingRequest) {
        List<StartupTaskJson> startupTaskJsons = startupTrackingService
                .loadStartupTaskJsons(trackingRequest.getFromDate(), trackingRequest.getToDate());
        // Startups that never reported an end have no record of their own - resolveTerminated
        // adds them so the list shows the abandoned sessions, not only the ones that finished.
        List<StartupTerminatedJson> startupTerminatedJsons = startupTrackingService.resolveTerminated(
                startupTaskJsons,
                startupTrackingService.loadStartupTerminatedJson(trackingRequest.getFromDate(), trackingRequest.getToDate()));

        return new TrackingContainer()
                .pageRequests(pageRequestService.loadPageRequests(trackingRequest.getFromDate(), trackingRequest.getToDate()))
                .userActivities(userActivityService.loadUserActivities(trackingRequest.getFromDate(), trackingRequest.getToDate()))
                .startupTaskJsons(startupTaskJsons)
                .startupTerminatedJson(startupTerminatedJsons)
                .tabHiddenJsons(startupTrackingService.loadTabHiddenJsons(trackingRequest.getFromDate(), trackingRequest.getToDate()));
    }

    /**
     * The history view: one row per attempt to start the game in the range, newest first.
     * <p>
     * Assembled on the server because the join needs the running planet and the user table. The
     * per-player game history is deliberately not in here - that is one Mongo query per player and
     * has its own lazy call.
     */
    @PostMapping(value = "loadPlayerSessions",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PlayerSessionInfo> loadPlayerSessions(@RequestBody TrackingRequest trackingRequest) {
        return playerSessionService.load(trackingRequest.getFromDate(), trackingRequest.getToDate());
    }

    /**
     * Per-day funnel, independent of the date range picker above it. The UI always uses the
     * default window; the days parameter exists to look further back by hand.
     *
     * @param platform restrict to visitors carrying this platform's click id. Optional on
     *                 purpose: without it every visitor counts, including organic. That also
     *                 keeps a stale cached frontend working instead of failing the request.
     */
    @GetMapping(value = "loadDailyProgress", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<DailyProgress> loadDailyProgress(
            @RequestParam(value = "platform", required = false) TrackingPlatform platform,
            @RequestParam(value = "days", defaultValue = "" + DAILY_PROGRESS_DAYS) int days) {
        return dailyProgressService.loadDailyProgress(days, DAILY_PROGRESS_MIN_LEVEL,
                DAILY_PROGRESS_MAX_LEVEL, platform);
    }

}
