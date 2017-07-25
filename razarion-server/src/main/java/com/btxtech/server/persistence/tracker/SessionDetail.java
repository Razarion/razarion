package com.btxtech.server.persistence.tracker;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 29.05.2017.
 */
public class SessionDetail {
    private Date time;
    private String id;
    private String fbAdRazTrack;
    private String userAgent;
    private String remoteHost;
    private String remoteAddr;
    private List<GameSessionDetail> gameSessionDetails;
    private List<PageDetail> pageDetails;

    public Date getTime() {
        return time;
    }

    public SessionDetail setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public SessionDetail setId(String id) {
        this.id = id;
        return this;
    }

    public String getFbAdRazTrack() {
        return fbAdRazTrack;
    }

    public SessionDetail setFbAdRazTrack(String fbAdRazTrack) {
        this.fbAdRazTrack = fbAdRazTrack;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public SessionDetail setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public SessionDetail setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
        return this;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public SessionDetail setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
        return this;
    }

    public List<GameSessionDetail> getGameSessionDetails() {
        return gameSessionDetails;
    }

    public SessionDetail setGameSessionDetails(List<GameSessionDetail> gameSessionDetails) {
        this.gameSessionDetails = gameSessionDetails;
        return this;
    }

    public List<PageDetail> getPageDetails() {
        return pageDetails;
    }

    public SessionDetail setPageDetails(List<PageDetail> pageDetails) {
        this.pageDetails = pageDetails;
        return this;
    }
}
