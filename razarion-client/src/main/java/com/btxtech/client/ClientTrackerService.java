package com.btxtech.client;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerControllerFactory;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Singleton
public class ClientTrackerService implements StartupProgressListener {
    private final Logger logger = Logger.getLogger(ClientTrackerService.class.getName());
    private final Provider<Boot> boot;

    @Inject
    public ClientTrackerService(Provider<Boot> boot) {
        this.boot = boot;
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        TrackerControllerFactory.INSTANCE
                .startupTask(createStartupTaskJson(task, null))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFinished: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
        TrackerControllerFactory.INSTANCE
                .startupTask(createStartupTaskJson(task, error))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFailed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        TrackerControllerFactory.INSTANCE
                .startupTerminated(createStartupTerminatedJson(totalTime, true))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFinished: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        TrackerControllerFactory.INSTANCE
                .startupTerminated(createStartupTerminatedJson(totalTime, false))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFailed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
        logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
    }

    private StartupTaskJson createStartupTaskJson(AbstractStartupTask task, String error) {
        StartupTaskJson startupTaskJson = new StartupTaskJson();
        startupTaskJson.setGameSessionUuid(boot.get().getGameSessionUuid());
        startupTaskJson.setStartTime(new Date(task.getStartTime())).setDuration((int) task.getDuration());
        startupTaskJson.setTaskEnum(task.getTaskEnum().name()).setError(error);
        return startupTaskJson;
    }

    private StartupTerminatedJson createStartupTerminatedJson(long totalTime, boolean success) {
        StartupTerminatedJson startupTerminatedJson = new StartupTerminatedJson();
        startupTerminatedJson.setGameSessionUuid(boot.get().getGameSessionUuid());
        startupTerminatedJson.successful(success).totalTime((int) totalTime);
        return startupTerminatedJson;
    }
}
