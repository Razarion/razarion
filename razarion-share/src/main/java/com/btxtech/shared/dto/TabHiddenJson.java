package com.btxtech.shared.dto;

import java.util.Date;

/**
 * The player put the running game into the background - switched tabs, or left the browser.
 * <p>
 * Deliberately not a {@link StartupTerminatedJson}: the startup ended successfully, and writing
 * it there would either produce a second terminated record per session or make an abandoned
 * startup and a backgrounded game look alike. It is its own thing, and it answers a question the
 * startup tracking cannot: a startup that finished while nobody was looking is not a player.
 * <p>
 * Reported once per session, on the first time the page goes hidden after the engine started.
 */
public class TabHiddenJson {
    private String gameSessionUuid;
    private Date serverTime;
    /**
     * Milliseconds between the page load and the moment the tab went to the background. Says
     * whether the player watched the game at all or was already somewhere else.
     */
    private Integer millisSincePageLoad;
    private String rdtCid;
    private String twclid;
    private String utmCampaign;
    private String utmSource;

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

    public Integer getMillisSincePageLoad() {
        return millisSincePageLoad;
    }

    public void setMillisSincePageLoad(Integer millisSincePageLoad) {
        this.millisSincePageLoad = millisSincePageLoad;
    }

    public String getRdtCid() {
        return rdtCid;
    }

    public void setRdtCid(String rdtCid) {
        this.rdtCid = rdtCid;
    }

    public String getTwclid() {
        return twclid;
    }

    public void setTwclid(String twclid) {
        this.twclid = twclid;
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

    public TabHiddenJson gameSessionUuid(String gameSessionUuid) {
        setGameSessionUuid(gameSessionUuid);
        return this;
    }

    public TabHiddenJson serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public TabHiddenJson millisSincePageLoad(Integer millisSincePageLoad) {
        setMillisSincePageLoad(millisSincePageLoad);
        return this;
    }

    @Override
    public String toString() {
        return "TabHiddenJson{" +
                "gameSessionUuid='" + gameSessionUuid + '\'' +
                ", millisSincePageLoad=" + millisSincePageLoad +
                ", serverTime=" + serverTime +
                '}';
    }
}
