package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerController;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.shared.system.perfmon.TerrainTileStatistic;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class TrackerControllerImpl implements TrackerController {
    // private Logger logger = Logger.getLogger(TrackerControllerImpl.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private SessionHolder sessionHolder;

    @Override
    public void startupTask(StartupTaskJson startupTaskJson) {
        try {
            trackerPersistence.onStartupTask(startupTaskJson);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void startupTerminated(StartupTerminatedJson startupTerminatedJson) {
        try {
            trackerPersistence.onStartupTerminated(startupTerminatedJson);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void gameUiControlTrackerInfo(GameUiControlTrackerInfo gameUiControlTrackerInfo) {
        try {
            trackerPersistence.onGameUiControlTrackerInfo(gameUiControlTrackerInfo);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void sceneTrackerInfo(SceneTrackerInfo sceneTrackerInfo) {
        try {
            trackerPersistence.onSceneTrackerInfo(sceneTrackerInfo);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void performanceTracker(List<PerfmonStatistic> perfmonStatistics) {
        try {
            trackerPersistence.onPerformanceTracker(perfmonStatistics);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void terrainTileStatisticsTracker(List<TerrainTileStatistic> terrainTileStatistics) {
        try {
            trackerPersistence.onTerrainTileStatisticsTracker(terrainTileStatistics);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void trackingStart(TrackingStart trackingStart) {
        try {
            trackerPersistence.onTrackingStart(sessionHolder.getPlayerSession().getHttpSessionId(), trackingStart);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void detailedTracking(TrackingContainer trackingContainer) {
        try {
            trackerPersistence.onDetailedTracking(sessionHolder.getPlayerSession().getHttpSessionId(), trackingContainer);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}
