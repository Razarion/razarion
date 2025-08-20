package com.btxtech.server.rest.tracking;

import com.btxtech.server.model.Roles;
import com.btxtech.server.service.tracking.StartupTrackingService;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerController;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.btxtech.shared.CommonUrl.APPLICATION_PATH;
import static com.btxtech.shared.CommonUrl.TRACKER_PATH;

@RestController
@RequestMapping(APPLICATION_PATH + "/" + TRACKER_PATH)
public class TrackerControllerImpl implements TrackerController {
    private final StartupTrackingService startupTrackingService;

    public TrackerControllerImpl(StartupTrackingService startupTrackingService) {
        this.startupTrackingService = startupTrackingService;
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

    @GetMapping(value = "loadStartupTerminatedJson", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public List<StartupTerminatedJson> loadStartupTerminatedJson() {
        return startupTrackingService.loadStartupTerminatedJson();
    }

    @GetMapping(value = "loadStartupTaskJson/{gameSessionUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(Roles.ADMIN)
    public List<StartupTaskJson> loadStartupTaskJson(@PathVariable("gameSessionUuid") String gameSessionUuid) {
        return startupTrackingService.loadStartupTaskJson(gameSessionUuid);
    }

}
