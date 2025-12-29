package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path(CommonUrl.TRACKER_PATH)
@RequestFactory
public interface TrackerController {
    @POST
    @Path("startupTask")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTask(StartupTaskJson startupTaskJson);

    @POST
    @Path("startupTerminated")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTerminated(StartupTerminatedJson startupTerminatedJson);
}
