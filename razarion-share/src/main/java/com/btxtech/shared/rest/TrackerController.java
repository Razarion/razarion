package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.FirstInteractionJson;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.TabHiddenJson;
import com.btxtech.shared.dto.TipStallJson;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path(CommonUrl.TRACKER_PATH)
public interface TrackerController {
    @POST
    @Path("startupTask")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTask(StartupTaskJson startupTaskJson);

    @POST
    @Path("startupTerminated")
    @Consumes(MediaType.APPLICATION_JSON)
    void startupTerminated(StartupTerminatedJson startupTerminatedJson);

    @POST
    @Path("tipStall")
    @Consumes(MediaType.APPLICATION_JSON)
    void tipStall(TipStallJson tipStallJson);

    @POST
    @Path("tabHidden")
    @Consumes(MediaType.APPLICATION_JSON)
    void tabHidden(TabHiddenJson tabHiddenJson);

    @POST
    @Path("firstInteraction")
    @Consumes(MediaType.APPLICATION_JSON)
    void firstInteraction(FirstInteractionJson firstInteractionJson);
}
