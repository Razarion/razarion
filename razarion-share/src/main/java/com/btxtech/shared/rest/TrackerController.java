package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 03.03.2017.
 */
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
    void performanceTracker(List<PerfmonStatistic> perfmonStatistics);

    @POST
    @Path("terrainTileStatisticsTracker")
    @Consumes(MediaType.APPLICATION_JSON)
    void terrainTileStatisticsTracker(List<TerrainTileStatistic> terrainTileStatistics);

    @POST
    @Path("trackingstart")
    @Consumes(MediaType.APPLICATION_JSON)
    void trackingStart(TrackingStart trackingStart);

    @POST
    @Path("detailedTracking")
    @Consumes(MediaType.APPLICATION_JSON)
    void detailedTracking(TrackingContainer trackingContainer);
}
