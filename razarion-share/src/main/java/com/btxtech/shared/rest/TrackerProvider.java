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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Path(CommonUrl.TRACKER_PATH)
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

    @GET
    @Path("webpagenoscript/{p}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    Response webPageNoScript(@PathParam("p") String page);

    @GET
    @Path("webpagescript/{p}")
    @Produces(MediaType.TEXT_PLAIN)
    String webPageScript(@PathParam("p") String page);

    @GET
    @Path("webpagecookie/{p}/{e}")
    @Produces(MediaType.TEXT_PLAIN)
    public String webPageCookie(@PathParam("p") String page, @PathParam("e") Boolean enabled);

}
