package com.btxtech.server.rest;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat on 29.05.2017.
 */
public class SessionDetail {
    private Date time;
    private String id;
    private String fbAdRazTrack;
    private List<GameSessionDetail> gameSessionDetails;

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

    public List<GameSessionDetail> getGameSessionDetails() {
        return gameSessionDetails;
    }

    public SessionDetail setGameSessionDetails(List<GameSessionDetail> gameSessionDetails) {
        this.gameSessionDetails = gameSessionDetails;
        return this;
    }
}
