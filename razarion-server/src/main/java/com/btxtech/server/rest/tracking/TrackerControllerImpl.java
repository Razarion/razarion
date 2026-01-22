package com.btxtech.server.rest.tracking;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.tracking.TrackingContainer;
import com.btxtech.server.model.tracking.TrackingRequest;
import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.service.tracking.StartupTrackingService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.btxtech.shared.CommonUrl.APPLICATION_PATH;
import static com.btxtech.shared.CommonUrl.TRACKER_PATH;

@RestController
@RequestMapping(APPLICATION_PATH + "/" + TRACKER_PATH)
public class TrackerControllerImpl implements TrackerController {
    private final StartupTrackingService startupTrackingService;
    private final PageRequestService pageRequestService;
    private final UserActivityService userActivityService;

    public TrackerControllerImpl(StartupTrackingService startupTrackingService,
                                 PageRequestService pageRequestService,
                                 UserActivityService userActivityService) {
        this.startupTrackingService = startupTrackingService;
        this.pageRequestService = pageRequestService;
        this.userActivityService = userActivityService;
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

}
