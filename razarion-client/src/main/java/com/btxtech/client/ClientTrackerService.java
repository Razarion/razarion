package com.btxtech.client;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.rest.TrackerControllerFactory;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import elemental2.dom.DomGlobal;
import elemental2.dom.URLSearchParams;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
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
    private String rdtCid;
    private String utmCampaign;
    private String utmSource;


    @Inject
    public ClientTrackerService(Provider<Boot> boot) {
        this.boot = boot;
        try {
            String search = DomGlobal.location.search;
            URLSearchParams queryParams = new URLSearchParams(search);
            rdtCid = queryParams.get("rdt_cid");
            utmCampaign = queryParams.get("utm_campaign");
            utmSource = queryParams.get("utm_source");
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
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
        return new StartupTaskJson()
                .gameSessionUuid(boot.get().getGameSessionUuid())
                .startTime(new Date(task.getStartTime()))
                .duration((int) task.getDuration())
                .taskEnum(task.getTaskEnum().name())
                .error(error)
                .utmCampaign(utmCampaign)
                .rdtCid(rdtCid)
                .utmSource(utmSource);
    }

    private StartupTerminatedJson createStartupTerminatedJson(long totalTime, boolean success) {
        return new StartupTerminatedJson()
                .gameSessionUuid(boot.get().getGameSessionUuid())
                .successful(success)
                .totalTime((int) totalTime)
                .utmCampaign(utmCampaign)
                .rdtCid(rdtCid)
                .utmSource(utmSource);
    }
}
