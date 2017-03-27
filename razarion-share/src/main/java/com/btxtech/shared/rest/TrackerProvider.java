package com.btxtech.shared.rest;

import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;

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

    @POST
    @Path("gameUiControlTrackerInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    void gameUiControlTrackerInfo(GameUiControlTrackerInfo gameUiControlTrackerInfo);

    @POST
    @Path("sceneTrackerInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    void sceneTrackerInfo(SceneTrackerInfo sceneTrackerInfo);

    @POST
    @Path("performanceTracker")
    @Consumes(MediaType.APPLICATION_JSON)
    void performanceTracker(PerfmonStatistic perfmonStatistic);
}
