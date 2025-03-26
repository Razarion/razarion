package com.btxtech.server.rest;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerController;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.btxtech.shared.CommonUrl.APPLICATION_PATH;
import static com.btxtech.shared.CommonUrl.TRACKER_PATH;

@RestController
@RequestMapping(APPLICATION_PATH + "/" + TRACKER_PATH)
public class TrackerControllerImpl implements TrackerController {
    private final Logger logger = LoggerFactory.getLogger(TrackerControllerImpl.class);

    @Override
    @PostMapping(value = "startupTask", consumes = MediaType.APPLICATION_JSON)
    public void startupTask(StartupTaskJson startupTaskJson) {
        logger.info(startupTaskJson.toString());
    }

    @Override
    @PostMapping(value = "startupTerminated", consumes = MediaType.APPLICATION_JSON)
    public void startupTerminated(StartupTerminatedJson startupTerminatedJson) {
        logger.info(startupTerminatedJson.toString());
    }

    @Override
    @PostMapping(value = "gameUiControlTrackerInfo", consumes = MediaType.APPLICATION_JSON)
    public void gameUiControlTrackerInfo(GameUiControlTrackerInfo gameUiControlTrackerInfo) {
        logger.info(gameUiControlTrackerInfo.toString());
    }

    @Override
    @PostMapping(value = "sceneTrackerInfo", consumes = MediaType.APPLICATION_JSON)
    public void sceneTrackerInfo(SceneTrackerInfo sceneTrackerInfo) {
        logger.info(sceneTrackerInfo.toString());
    }

    @Override
    @PostMapping(value = "performanceTracker", consumes = MediaType.APPLICATION_JSON)
    public void performanceTracker(List<PerfmonStatistic> perfmonStatistics) {
        logger.info("PerfmonStatistic ... not implemented");
    }

    @Override
    @PostMapping(value = "terrainTileStatisticsTracker", consumes = MediaType.APPLICATION_JSON)
    public void terrainTileStatisticsTracker(List<TerrainTileStatistic> terrainTileStatistics) {
        logger.info("TerrainTileStatistic ... not implemented");
    }

    @Override
    @PostMapping(value = "trackingstart", consumes = MediaType.APPLICATION_JSON)
    public void trackingStart(TrackingStart trackingStart) {
        logger.info(trackingStart.toString());
    }

    @Override
    @PostMapping(value = "detailedTracking", consumes = MediaType.APPLICATION_JSON)
    public void detailedTracking(TrackingContainer trackingContainer) {
        logger.info(trackingContainer.toString());
    }
}
