package com.btxtech.server.persistence.history;

import java.util.Date;

/**
 * Created by Beat
 * on 03.01.2018.
 */
public class UserHistoryEntry {
    private String name;
    private int id;
    private int playerId;
    private Date login;
    private Date logout;
    private String sessionId;

    public String getName() {
        return name;
    }

    public UserHistoryEntry setName(String name) {
        this.name = name;
        return this;
    }

    public int getId() {
        return id;
    }

    public UserHistoryEntry setId(int id) {
        this.id = id;
        return this;
    }

    public int getPlayerId() {
        return playerId;
    }

    public UserHistoryEntry setPlayerId(int playerId) {
        this.playerId = playerId;
        return this;
    }

    public Date getLogin() {
        return login;
    }

    public UserHistoryEntry setLogin(Date login) {
        this.login = login;
        return this;
    }

    public Date getLogout() {
        return logout;
    }

    public UserHistoryEntry setLogout(Date logout) {
        this.logout = logout;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserHistoryEntry setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
}
