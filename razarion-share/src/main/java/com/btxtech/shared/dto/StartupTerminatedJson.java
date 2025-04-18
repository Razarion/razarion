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

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
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


    public StartupTerminatedJson successful(boolean successful) {
        setSuccessful(successful);
        return this;
    }

    public StartupTerminatedJson totalTime(int totalTime) {
        setTotalTime(totalTime);
        return this;
    }

    public StartupTerminatedJson gameSessionUuid(String gameSessionUuid) {
        setGameSessionUuid(gameSessionUuid);
        return this;
    }

    public StartupTerminatedJson httpSessionId(String httpSessionId) {
        setHttpSessionId(httpSessionId);
        return this;
    }

    public StartupTerminatedJson serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    @Override
    public String toString() {
        return "StartupTerminatedJson{" +
                "successful=" + successful +
                ", totalTime=" + totalTime +
                ", gameSessionUuid='" + gameSessionUuid + '\'' +
                ", httpSessionId='" + httpSessionId + '\'' +
                ", serverTime=" + serverTime +
                '}';
    }
}
