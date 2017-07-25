package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat on 29.05.2017.
 */
public class SessionTracker {
    private Date time;
    private String id;
    private int gameAttempts;
    private int successGameAttempts;
    private String remoteHost;
    private String fbAdRazTrack;
    private String userAgent;

    public Date getTime() {
        return time;
    }

    public SessionTracker setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public int getGameAttempts() {
        return gameAttempts;
    }

    public SessionTracker setGameAttempts(int gameAttempts) {
        this.gameAttempts = gameAttempts;
        return this;
    }

    public int getSuccessGameAttempts() {
        return successGameAttempts;
    }

    public SessionTracker setSuccessGameAttempts(int successGameAttempts) {
        this.successGameAttempts = successGameAttempts;
        return this;
    }

    public SessionTracker setId(String id) {
        this.id = id;
        return this;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public SessionTracker setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
        return this;
    }

    public String getFbAdRazTrack() {
        return fbAdRazTrack;
    }

    public SessionTracker setFbAdRazTrack(String fbAdRazTrack) {
        this.fbAdRazTrack = fbAdRazTrack;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public SessionTracker setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}
