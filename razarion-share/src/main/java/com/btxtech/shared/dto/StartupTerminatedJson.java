package com.btxtech.shared.dto;

import java.util.Date;

public class StartupTerminatedJson {
    private boolean successful;
    private int totalTime;
    private String gameSessionUuid;
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
                ", serverTime=" + serverTime +
                '}';
    }
}
