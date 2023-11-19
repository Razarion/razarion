package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Path(CommonUrl.BACKEND_PATH)
public interface BackendController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadStartupTerminatedJson")
    List<StartupTerminatedJson> loadStartupTerminatedJson();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadStartupTaskJson/{gameSessionUuid}")
    List<StartupTaskJson> loadStartupTaskJson(@PathParam("gameSessionUuid") String gameSessionUuid);
}
