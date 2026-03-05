package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsJson;
import com.btxtech.client.jso.JsObject;
import com.btxtech.client.jso.JsURLSearchParams;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.rest.TeaVMRestAccess;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TeaVMClientTrackerService implements StartupProgressListener {
    private final Provider<Boot> boot;
    private String rdtCid;
    private String utmCampaign;
    private String utmSource;

    @Inject
    public TeaVMClientTrackerService(Provider<Boot> boot) {
        this.boot = boot;
        try {
            String search = JsWindow.getLocationSearch();
            if (search != null && !search.isEmpty()) {
                JsURLSearchParams params = JsURLSearchParams.create(search);
                rdtCid = params.get("rdt_cid");
                utmCampaign = params.get("utm_campaign");
                utmSource = params.get("utm_source");
            }
        } catch (Throwable t) {
            JsConsole.warn("TeaVMClientTrackerService: failed to read URL params: " + t.getMessage());
        }
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        sendStartupTask(task, null);
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        JsConsole.error("onTaskFailed: " + task + " error:" + error);
        sendStartupTask(task, error);
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        sendStartupTerminated(totalTime, true);
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        sendStartupTerminated(totalTime, false);
        JsConsole.error("onStartupFailed: totalTime:" + totalTime);
    }

    private void sendStartupTask(AbstractStartupTask task, String error) {
        try {
            JsObject jsObj = JsObject.create();
            jsObj.set("gameSessionUuid", boot.get().getGameSessionUuid());
            jsObj.set("startTime", (double) task.getStartTime());
            jsObj.set("duration", (int) task.getDuration());
            jsObj.set("taskEnum", task.getTaskEnum().name());
            if (error != null) {
                jsObj.set("error", error);
            }
            if (rdtCid != null) {
                jsObj.set("rdtCid", rdtCid);
            }
            if (utmCampaign != null) {
                jsObj.set("utmCampaign", utmCampaign);
            }
            if (utmSource != null) {
                jsObj.set("utmSource", utmSource);
            }
            TeaVMRestAccess.post("/rest/tracker/startupTask", JsJson.stringify(jsObj), null, null);
        } catch (Throwable t) {
            JsConsole.error("sendStartupTask failed: " + t.getMessage());
        }
    }

    private void sendStartupTerminated(long totalTime, boolean success) {
        try {
            JsObject jsObj = JsObject.create();
            jsObj.set("gameSessionUuid", boot.get().getGameSessionUuid());
            jsObj.set("successful", success);
            jsObj.set("totalTime", (int) totalTime);
            if (rdtCid != null) {
                jsObj.set("rdtCid", rdtCid);
            }
            if (utmCampaign != null) {
                jsObj.set("utmCampaign", utmCampaign);
            }
            if (utmSource != null) {
                jsObj.set("utmSource", utmSource);
            }
            TeaVMRestAccess.post("/rest/tracker/startupTerminated", JsJson.stringify(jsObj), null, null);
        } catch (Throwable t) {
            JsConsole.error("sendStartupTerminated failed: " + t.getMessage());
        }
    }
}
