package com.btxtech.server.model.tracking;

import java.util.Date;

/**
 * One startup task of an attempt, for the expanded row. Slimmed down from the stored record: the
 * click ids and the user agent are already on the row itself.
 */
public class PlayerSessionTask {
    private String taskEnum;
    private Date serverTime;
    /** Milliseconds the task took. */
    private int duration;
    /** Null unless the task failed. */
    private String error;

    public String getTaskEnum() {
        return taskEnum;
    }

    public PlayerSessionTask taskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
        return this;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public PlayerSessionTask serverTime(Date serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public PlayerSessionTask duration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getError() {
        return error;
    }

    public PlayerSessionTask error(String error) {
        this.error = error;
        return this;
    }
}
