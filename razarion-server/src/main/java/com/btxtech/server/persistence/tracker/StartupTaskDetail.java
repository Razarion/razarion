package com.btxtech.server.persistence.tracker;

import javax.persistence.Column;
import java.util.Date;

/**
 * Created by Beat
 * on 25.07.2017.
 */
public class StartupTaskDetail {
    private String taskEnum;
    private Date clientStartTime;
    private int duration;
    private String error;

    public String getTaskEnum() {
        return taskEnum;
    }

    public StartupTaskDetail setTaskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
        return this;
    }

    public Date getClientStartTime() {
        return clientStartTime;
    }

    public StartupTaskDetail setClientStartTime(Date clientStartTime) {
        this.clientStartTime = clientStartTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public StartupTaskDetail setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getError() {
        return error;
    }

    public StartupTaskDetail setError(String error) {
        this.error = error;
        return this;
    }
}
