package com.btxtech.server.user;

import java.util.Date;

/**
 * Created by Beat
 * on 02.01.2018.
 */
public class NewUser {
    private Date date;
    private String name;
    private int id;
    private int playerId;
    private String sessionId;

    public Date getDate() {
        return date;
    }

    public NewUser setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getName() {
        return name;
    }

    public NewUser setName(String name) {
        this.name = name;
        return this;
    }

    public int getId() {
        return id;
    }

    public NewUser setId(int id) {
        this.id = id;
        return this;
    }

    public int getPlayerId() {
        return playerId;
    }

    public NewUser setPlayerId(int playerId) {
        this.playerId = playerId;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public NewUser setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
}
