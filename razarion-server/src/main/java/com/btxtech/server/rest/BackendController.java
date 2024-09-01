package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.BACKEND_PATH)
public class BackendController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadStartupTerminatedJson")
    @SecurityCheck
    public List<StartupTerminatedJson> loadStartupTerminatedJson() {
        try {
            return trackerPersistence.loadStartupTerminatedJson();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadStartupTaskJson/{gameSessionUuid}")
    @SecurityCheck
    public List<StartupTaskJson> loadStartupTaskJson(@PathParam("gameSessionUuid") String gameSessionUuid) {
        try {
            return trackerPersistence.loadStartupTaskJson(gameSessionUuid);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
