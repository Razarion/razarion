package com.btxtech.server.rest;

import java.util.Date;

/**
 * Created by Beat on 29.05.2017.
 */
public class GameSessionDetail {
    private Date time;
    private String id;
    private String sessionId;

    public Date getTime() {
        return time;
    }

    public GameSessionDetail setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public GameSessionDetail setId(String id) {
        this.id = id;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameSessionDetail setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
}
