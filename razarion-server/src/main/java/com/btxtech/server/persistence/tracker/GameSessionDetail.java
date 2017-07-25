package com.btxtech.server.persistence.tracker;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 29.05.2017.
 */
public class GameSessionDetail {
    private Date time;
    private String id;
    private String sessionId;
    private List<StartupTaskDetail> startupTaskDetails;
    private StartupTerminatedDetail startupTerminatedDetail;
    private boolean inGameTracking;

    public Date getTime() {
        return time;
    }

    public GameSessionDetail setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public GameSessionDetail setId(String id) {
        this.id = id;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameSessionDetail setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public List<StartupTaskDetail> getStartupTaskDetails() {
        return startupTaskDetails;
    }

    public GameSessionDetail setStartupTaskDetails(List<StartupTaskDetail> startupTaskDetails) {
        this.startupTaskDetails = startupTaskDetails;
        return this;
    }

    public StartupTerminatedDetail getStartupTerminatedDetail() {
        return startupTerminatedDetail;
    }

    public GameSessionDetail setStartupTerminatedDetail(StartupTerminatedDetail startupTerminatedDetail) {
        this.startupTerminatedDetail = startupTerminatedDetail;
        return this;
    }

    public boolean isInGameTracking() {
        return inGameTracking;
    }

    public GameSessionDetail setInGameTracking(boolean inGameTracking) {
        this.inGameTracking = inGameTracking;
        return this;
    }
}
