package com.btxtech.shared.dto;

import java.util.Date;

public class StartupTerminatedJson {
    private boolean successful;
    private int totalTime;
    private String gameSessionUuid;
    private Date serverTime;
    private String rdtCid;
    private String utmCampaign;
    private String utmSource;

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

    public StartupTerminatedJson rdtCid(String rdtCid) {
        setRdtCid(rdtCid);
        return this;
    }

    public StartupTerminatedJson utmCampaign(String utmCampaign) {
        setUtmCampaign(utmCampaign);
        return this;
    }

    public StartupTerminatedJson utmSource(String utmSource) {
        setUtmSource(utmSource);
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
