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
    private String twclid;
    /** Meta's click id, on links from Facebook and Instagram. */
    private String fbclid;
    private String utmCampaign;
    private String utmSource;
    /**
     * Only sent with the page's first task. The startups that never reach the engine are the
     * ones worth naming a browser for - WebAssembly GC needs Chrome 119+, Firefox 120+ or
     * Safari 18.2+, and anything older dies before a single engine task runs.
     */
    private String userAgent;
    /**
     * What the browser can do, as {@code name=value} pairs - wasm, wasmgc, webgl2, coi, sab, mem,
     * cores. Sent with PAGE_LOADED only, from the inline script in index.html, so it is there even
     * for the sessions that never load anything else. See that script for why it is always
     * reported rather than only when something is missing.
     */
    private String capabilities;
    /**
     * Where the browser says it came from, straight from document.referrer.
     * <p>
     * Worth something only for a visitor who opened the game directly. "Play Now" is a navigation
     * from the landing page to /game, so for everyone who came that way this reads razarion.com -
     * the page before, not the origin. The origin of those sessions is taken from the landing page
     * request instead; see PageRequestType.LANDING.
     * <p>
     * Empty for a direct open - typed url, bookmark, or a referrer the browser withholds.
     */
    private String referrer;
    /**
     * Stamped by the server, not sent by the client - see StartupTerminatedJson#userId.
     */
    private String userId;
    private String httpSessionId;

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public StartupTaskJson referrer(String referrer) {
        setReferrer(referrer);
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

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

    public String getTwclid() {
        return twclid;
    }

    public void setTwclid(String twclid) {
        this.twclid = twclid;
    }

    public String getFbclid() {
        return fbclid;
    }

    public void setFbclid(String fbclid) {
        this.fbclid = fbclid;
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

    public StartupTaskJson twclid(String twclid) {
        setTwclid(twclid);
        return this;
    }

    public StartupTaskJson fbclid(String fbclid) {
        setFbclid(fbclid);
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

    public StartupTaskJson userAgent(String userAgent) {
        setUserAgent(userAgent);
        return this;
    }

    public StartupTaskJson capabilities(String capabilities) {
        setCapabilities(capabilities);
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
                ", twclid='" + twclid + '\'' +
                ", utmCampaign='" + utmCampaign + '\'' +
                ", utmSource='" + utmSource + '\'' +
                '}';
    }
}
