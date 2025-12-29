package com.btxtech.shared.dto;

import java.util.Date;

public class StartupTaskJson {
    private String gameSessionUuid;
    private String taskEnum;
    private Date startTime;
    private Date serverTime;
    private int duration;
    private String error;
    private String rdtCid;
    private String utmCampaign;
    private String utmSource;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
    }

    public String getTaskEnum() {
        return taskEnum;
    }

    public void setTaskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRdtCid() {
        return rdtCid;
    }

    public void setRdtCid(String rdtCid) {
        this.rdtCid = rdtCid;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public StartupTaskJson gameSessionUuid(String gameSessionUuid) {
        setGameSessionUuid(gameSessionUuid);
        return this;
    }

    public StartupTaskJson taskEnum(String taskEnum) {
        setTaskEnum(taskEnum);
        return this;
    }

    public StartupTaskJson startTime(Date startTime) {
        setStartTime(startTime);
        return this;
    }

    public StartupTaskJson serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public StartupTaskJson duration(int duration) {
        setDuration(duration);
        return this;
    }

    public StartupTaskJson error(String error) {
        setError(error);
        return this;
    }

    public StartupTaskJson rdtCid(String rdtCid) {
        setRdtCid(rdtCid);
        return this;
    }

    public StartupTaskJson utmCampaign(String utmCampaign) {
        setUtmCampaign(utmCampaign);
        return this;
    }

    public StartupTaskJson utmSource(String utmSource) {
        setUtmSource(utmSource);
        return this;
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
                ", rdtCid='" + rdtCid + '\'' +
                ", utmCampaign='" + utmCampaign + '\'' +
                ", utmSource='" + utmSource + '\'' +
                '}';
    }
}
