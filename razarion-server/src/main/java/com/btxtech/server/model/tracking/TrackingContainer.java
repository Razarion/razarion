package com.btxtech.server.model.tracking;

import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.dto.TabHiddenJson;

import java.util.List;

public class TrackingContainer {
    private List<StartupTerminatedJson> startupTerminatedJson;
    private List<StartupTaskJson> startupTaskJsons;
    private List<UserActivity> userActivities;
    private List<PageRequest> pageRequests;
    /**
     * Games that went to the background after they had started. Their own list rather than a flag
     * on the startup records: one startup can be backgrounded more than once, and the two are
     * counted against different populations.
     */
    private List<TabHiddenJson> tabHiddenJsons;

    public List<StartupTerminatedJson> getStartupTerminatedJson() {
        return startupTerminatedJson;
    }

    public void setStartupTerminatedJson(List<StartupTerminatedJson> startupTerminatedJson) {
        this.startupTerminatedJson = startupTerminatedJson;
    }

    public List<StartupTaskJson> getStartupTaskJsons() {
        return startupTaskJsons;
    }

    public void setStartupTaskJsons(List<StartupTaskJson> startupTaskJsons) {
        this.startupTaskJsons = startupTaskJsons;
    }

    public List<UserActivity> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(List<UserActivity> userActivities) {
        this.userActivities = userActivities;
    }

    public List<PageRequest> getPageRequests() {
        return pageRequests;
    }

    public void setPageRequests(List<PageRequest> pageRequests) {
        this.pageRequests = pageRequests;
    }

    public List<TabHiddenJson> getTabHiddenJsons() {
        return tabHiddenJsons;
    }

    public void setTabHiddenJsons(List<TabHiddenJson> tabHiddenJsons) {
        this.tabHiddenJsons = tabHiddenJsons;
    }

    public TrackingContainer tabHiddenJsons(List<TabHiddenJson> tabHiddenJsons) {
        this.tabHiddenJsons = tabHiddenJsons;
        return this;
    }

    public TrackingContainer startupTerminatedJson(List<StartupTerminatedJson> startupTerminatedJson) {
        this.startupTerminatedJson = startupTerminatedJson;
        return this;
    }

    public TrackingContainer startupTaskJsons(List<StartupTaskJson> startupTaskJsons) {
        this.startupTaskJsons = startupTaskJsons;
        return this;
    }

    public TrackingContainer userActivities(List<UserActivity> userActivityList) {
        this.userActivities = userActivityList;
        return this;
    }

    public TrackingContainer pageRequests(List<PageRequest> pageRequestList) {
        this.pageRequests = pageRequestList;
        return this;
    }

}
