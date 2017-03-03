package com.btxtech.client.system.boot;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Dependent
public class StartupProgressListenerImpl implements StartupProgressListener {
    private Logger logger = Logger.getLogger(StartupProgressListenerImpl.class.getName());
    @Inject
    private Caller<TrackerProvider> providerCaller;
    @Inject
    private ClientRunner clientRunner;

    @Override
    public void onStart(StartupSeq startupSeq) {
        // Ignore
    }

    @Override
    public void onNextTask(StartupTaskEnum taskEnum) {
        // Ignore
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        providerCaller.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, null));
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
        providerCaller.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, error));
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        providerCaller.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTerminated(createStartupTerminatedJson(totalTime, true));
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
        providerCaller.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTerminated(createStartupTerminatedJson(totalTime, false));
    }

    private StartupTaskJson createStartupTaskJson(AbstractStartupTask task, String error) {
        StartupTaskJson startupTaskJson = new StartupTaskJson();
        startupTaskJson.setGameSessionUuid(clientRunner.getGameSessionUuid());
        startupTaskJson.setStartTime(new Date(task.getStartTime())).setDuration((int) task.getDuration());
        startupTaskJson.setTaskEnum(task.getTaskEnum().name()).setError(error);
        return startupTaskJson;
    }

    private StartupTerminatedJson createStartupTerminatedJson(long totalTime, boolean success) {
        StartupTerminatedJson startupTerminatedJson = new StartupTerminatedJson();
        startupTerminatedJson.setGameSessionUuid(clientRunner.getGameSessionUuid());
        startupTerminatedJson.setSuccessful(success).setTotalTime((int) totalTime);
        return startupTerminatedJson;
    }

}
