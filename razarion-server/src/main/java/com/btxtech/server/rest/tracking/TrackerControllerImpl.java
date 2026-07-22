package com.btxtech.server.rest.tracking;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.tracking.DailyProgress;
import com.btxtech.server.model.tracking.TrackingContainer;
import com.btxtech.server.model.tracking.TrackingPlatform;
import com.btxtech.server.model.tracking.TrackingRequest;
import com.btxtech.server.service.tracking.DailyProgressService;
import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.service.tracking.StartupTrackingService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerController;
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

    public TrackerControllerImpl(StartupTrackingService startupTrackingService,
                                 PageRequestService pageRequestService,
                                 UserActivityService userActivityService,
                                 DailyProgressService dailyProgressService) {
        this.startupTrackingService = startupTrackingService;
        this.pageRequestService = pageRequestService;
        this.userActivityService = userActivityService;
        this.dailyProgressService = dailyProgressService;
    }

    @Override
    @PostMapping(value = "startupTask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startupTask(@RequestBody StartupTaskJson startupTaskJson) {
        startupTrackingService.onStartupTask(startupTaskJson);
    }

    @Override
    @PostMapping(value = "startupTerminated", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startupTerminated(@RequestBody StartupTerminatedJson startupTerminatedJson) {
        startupTrackingService.onStartupTerminated(startupTerminatedJson);
    }

    @PostMapping(value = "loadTrackingContainer",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')") 
    public TrackingContainer loadTrackingContainer(@RequestBody TrackingRequest trackingRequest) {
        return new TrackingContainer()
                .pageRequests(pageRequestService.loadPageRequests(trackingRequest.getFromDate(), trackingRequest.getToDate()))
                .userActivities(userActivityService.loadUserActivities(trackingRequest.getFromDate(), trackingRequest.getToDate()))
                .startupTaskJsons(startupTrackingService.loadStartupTaskJsons(trackingRequest.getFromDate(), trackingRequest.getToDate()))
                .startupTerminatedJson(startupTrackingService.loadStartupTerminatedJson(trackingRequest.getFromDate(), trackingRequest.getToDate()));
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
