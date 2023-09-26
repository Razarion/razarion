package com.btxtech.shared.dto;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class StartupTerminatedJson {
    private boolean successful;
    private int totalTime;
    private String gameSessionUuid;
    private String httpSessionId;
    private Date serverTime;


    public boolean isSuccessful() {
        return successful;
    }

    public StartupTerminatedJson setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public StartupTerminatedJson setTotalTime(int totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public StartupTerminatedJson setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }
}
