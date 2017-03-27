package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;

import javax.inject.Inject;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class TrackerProviderImpl implements TrackerProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;

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
    public void performanceTracker(PerfmonStatistic perfmonStatistic) {
        try {
            trackerPersistence.onPerformanceTracker(perfmonStatistic);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
