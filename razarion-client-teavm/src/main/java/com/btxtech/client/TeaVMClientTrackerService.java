package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsJson;
import com.btxtech.client.jso.JsObject;
import com.btxtech.client.jso.JsURLSearchParams;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.client.rest.TeaVMRestAccess;
import com.btxtech.client.system.boot.PageBootGlobals;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TeaVMClientTrackerService implements StartupProgressListener {
    private final Provider<Boot> boot;
    private String rdtCid;
    private String twclid;
    private String fbclid;
    private String utmCampaign;
    private String utmSource;
    /** Read once at construction: a later navigation inside the game would report itself. */
    private String referrer;

    @Inject
    public TeaVMClientTrackerService(Provider<Boot> boot) {
        this.boot = boot;
        try {
            String search = JsWindow.getLocationSearch();
            if (search != null && !search.isEmpty()) {
                JsURLSearchParams params = JsURLSearchParams.create(search);
                rdtCid = params.get("rdt_cid");
                twclid = params.get("twclid");
                fbclid = params.get("fbclid");
                utmCampaign = params.get("utm_campaign");
                utmSource = params.get("utm_source");
            }
            String documentReferrer = JsWindow.getDocumentReferrer();
            if (documentReferrer != null && !documentReferrer.isEmpty()) {
                referrer = documentReferrer;
            }
        } catch (Throwable t) {
            JsConsole.warn("TeaVMClientTrackerService: failed to read URL params: " + t.getMessage());
        }
    }

    /**
     * Published for the page's abort beacon: when the player leaves mid startup, the beacon
     * reports this as the task the startup died on.
     */
    @Override
    public void onNextTask(StartupTaskEnum taskEnum) {
        JsWindow.setString(PageBootGlobals.CURRENT_TASK, taskEnum.name());
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

    /**
     * The task is still running, so this is an extra record rather than the task's own one. It
     * makes a stuck startup visible even when it later completes - and when it does not, it is
     * the last record of the session and names the task it hung on.
     */
    @Override
    public void onTaskTimeout(AbstractStartupTask task, long waitedMillis) {
        JsConsole.warn("onTaskTimeout: " + task + " waited:" + waitedMillis + "ms");
        sendStartupTask(task, "TIMEOUT: still waiting after " + waitedMillis + "ms", (int) waitedMillis);
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
        sendStartupTask(task, error, (int) task.getDuration());
    }

    private void sendStartupTask(AbstractStartupTask task, String error, int duration) {
        try {
            JsObject jsObj = JsObject.create();
            jsObj.set("gameSessionUuid", boot.get().getGameSessionUuid());
            jsObj.set("startTime", (double) task.getStartTime());
            jsObj.set("duration", duration);
            jsObj.set("taskEnum", task.getTaskEnum().name());
            if (error != null) {
                jsObj.set("error", error);
            }
            if (rdtCid != null) {
                jsObj.set("rdtCid", rdtCid);
            }
            if (twclid != null) {
                jsObj.set("twclid", twclid);
            }
            if (fbclid != null) {
                jsObj.set("fbclid", fbclid);
            }
            if (utmCampaign != null) {
                jsObj.set("utmCampaign", utmCampaign);
            }
            if (utmSource != null) {
                jsObj.set("utmSource", utmSource);
            }
            if (referrer != null) {
                jsObj.set("referrer", referrer);
            }
            TeaVMRestAccess.post("/rest/tracker/startupTask", JsJson.stringify(jsObj), null, null);
        } catch (Throwable t) {
            JsConsole.error("sendStartupTask failed: " + t.getMessage());
        }
    }

    private void sendStartupTerminated(long totalTime, boolean success) {
        // The startup ended on its own, so leaving the page from here on is not an abandoned
        // startup - silence the page's abort beacon.
        JsWindow.setString(PageBootGlobals.TERMINATED, "true");
        try {
            JsObject jsObj = JsObject.create();
            jsObj.set("gameSessionUuid", boot.get().getGameSessionUuid());
            jsObj.set("successful", success);
            jsObj.set("aborted", false);
            jsObj.set("totalTime", (int) totalTime);
            if (rdtCid != null) {
                jsObj.set("rdtCid", rdtCid);
            }
            if (twclid != null) {
                jsObj.set("twclid", twclid);
            }
            if (fbclid != null) {
                jsObj.set("fbclid", fbclid);
            }
            if (utmCampaign != null) {
                jsObj.set("utmCampaign", utmCampaign);
            }
            if (utmSource != null) {
                jsObj.set("utmSource", utmSource);
            }
            if (referrer != null) {
                jsObj.set("referrer", referrer);
            }
            TeaVMRestAccess.post("/rest/tracker/startupTerminated", JsJson.stringify(jsObj), null, null);
        } catch (Throwable t) {
            JsConsole.error("sendStartupTerminated failed: " + t.getMessage());
        }
    }
}
