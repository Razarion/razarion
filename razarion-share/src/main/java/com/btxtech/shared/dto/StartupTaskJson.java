package com.btxtech.shared.dto;

import java.util.Date;

public class StartupTaskJson {
    private String gameSessionUuid;
    private String taskEnum;
    private Date startTime;
    private Date serverTime;
    private int duration;
    private String error;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public StartupTaskJson setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public String getTaskEnum() {
        return taskEnum;
    }

    public StartupTaskJson setTaskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public StartupTaskJson setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public StartupTaskJson setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getError() {
        return error;
    }

    public StartupTaskJson setError(String error) {
        this.error = error;
        return this;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    @Override
    public String toString() {
        return "StartupTaskJson{" +
                "gameSessionUuid='" + gameSessionUuid + '\'' +
                ", taskEnum='" + taskEnum + '\'' +
                ", startTime=" + startTime +
                ", serverTime=" + serverTime +
                ", duration=" + duration +
                ", error='" + error + '\'' +
                '}';
    }
}
