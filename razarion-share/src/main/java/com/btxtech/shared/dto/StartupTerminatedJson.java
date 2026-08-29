package com.btxtech.shared.dto;

import java.util.Date;

public class StartupTerminatedJson {
    private boolean successful;
    /**
     * Null for an aborted startup: nobody ever reported a finish, so there is no duration to
     * show. Only a startup that ran to its own end - successfully or with an error - has one.
     */
    private Integer totalTime;
    private String gameSessionUuid;
    private Date serverTime;
    private String rdtCid;
    private String twclid;
    /** Meta's click id, on links from Facebook and Instagram. */
    private String fbclid;
    private String utmCampaign;
    private String utmSource;
    /**
     * The startup never terminated by itself: the player left, or a task waited forever for a
     * callback that never came. Such a session used to be invisible - it simply had no record
     * at all - which is why the startup list looked almost failure-free while a third of the
     * players never reached the game.
     * <p>
     * Set either by the client's pagehide beacon or, for everything the beacon cannot catch
     * (browser killed, network gone), derived on the server from startup tasks without a
     * matching terminated record.
     */
    private boolean aborted;
    /**
     * Last startup task seen for an aborted session - where it got stuck. Null otherwise.
     */
    private String lastTaskEnum;
    /**
     * The page went to the background rather than away: the player switched tabs while the game
     * was still loading. Without this, tabbing away and giving up are the same record, and they
     * call for opposite answers - one player is still reachable, the other is gone. Null for
     * aborts the server derived itself, which cannot know which of the two happened.
     */
    private Boolean hidden;
    /** Where the browser says it came from; see StartupTaskJson#referrer. */
    private String referrer;
    /**
     * Who was starting up. Stamped by the server from the request, never sent by the client.
     * <p>
     * Startup tracking is keyed on the game session uuid, everything a player then does on the
     * user id, and the only bridge between them - GAME_SESSION_STARTED - is written when the
     * system connection opens, which is the seventh of nine boot tasks. Every startup that died
     * before that was unattributable, and those are exactly the ones worth looking at. The
     * request carries the session, so the server can simply say who it was.
     */
    private String userId;
    /** The browser session, which is how a startup finds its landing page visit and its click id. */
    private String httpSessionId;

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

    public StartupTerminatedJson userId(String userId) {
        this.userId = userId;
        return this;
    }

    public StartupTerminatedJson httpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
        return this;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public String getLastTaskEnum() {
        return lastTaskEnum;
    }

    public void setLastTaskEnum(String lastTaskEnum) {
        this.lastTaskEnum = lastTaskEnum;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
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

    public StartupTerminatedJson successful(boolean successful) {
        setSuccessful(successful);
        return this;
    }

    public StartupTerminatedJson totalTime(Integer totalTime) {
        setTotalTime(totalTime);
        return this;
    }

    public StartupTerminatedJson aborted(boolean aborted) {
        setAborted(aborted);
        return this;
    }

    public StartupTerminatedJson lastTaskEnum(String lastTaskEnum) {
        setLastTaskEnum(lastTaskEnum);
        return this;
    }

    public StartupTerminatedJson hidden(Boolean hidden) {
        setHidden(hidden);
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

    public StartupTerminatedJson twclid(String twclid) {
        setTwclid(twclid);
        return this;
    }

    public StartupTerminatedJson fbclid(String fbclid) {
        setFbclid(fbclid);
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

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public StartupTerminatedJson referrer(String referrer) {
        setReferrer(referrer);
        return this;
    }

    @Override
    public String toString() {
        return "StartupTerminatedJson{" +
                "successful=" + successful +
                ", aborted=" + aborted +
                ", lastTaskEnum='" + lastTaskEnum + '\'' +
                ", totalTime=" + totalTime +
                ", gameSessionUuid='" + gameSessionUuid + '\'' +
                ", serverTime=" + serverTime +
                '}';
    }
}
