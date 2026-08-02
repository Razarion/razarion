package com.btxtech.server.model.tracking;

import java.util.Date;

/**
 * Something the player did in the window - a base, a level, a quest - for the expanded row.
 * <p>
 * Deliberately the user activity trail and not the game history: the history is a Mongo query per
 * player and belongs behind the lazy call the user table already uses. This is what came with the
 * range anyway.
 */
public class PlayerSessionEvent {
    private Date serverTime;
    private UserActivityType type;
    /** Base id, level number or quest id, depending on the type. */
    private String detail;

    public Date getServerTime() {
        return serverTime;
    }

    public PlayerSessionEvent serverTime(Date serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public UserActivityType getType() {
        return type;
    }

    public PlayerSessionEvent type(UserActivityType type) {
        this.type = type;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public PlayerSessionEvent detail(String detail) {
        this.detail = detail;
        return this;
    }
}
