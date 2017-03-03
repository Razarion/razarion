package com.btxtech.shared.rest;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Path(RestUrl.TRACKER_PATH)
public interface TrackerProvider {
    @POST
    @Path("startupTask")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTask(StartupTaskJson startupTaskJson);

    @POST
    @Path("startupTerminated")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTerminated(StartupTerminatedJson startupTerminatedJson);


}
