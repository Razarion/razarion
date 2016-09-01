/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.client.system.boot;

import com.btxtech.client.system.boot.StartupTaskEnum;

import java.io.Serializable;

/**
 * User: beat
 * Date: 05.12.2010
 * Time: 23:23:42
 */
public class StartupTaskInfo implements Serializable {
    private StartupTaskEnum taskEnum;
    private long startTime;
    private long duration;
    private String error;

    /**
     * Used by GWT
     */
    public StartupTaskInfo() {
    }

    public StartupTaskInfo(StartupTaskEnum taskEnum, long startTime, long duration) {
        this.taskEnum = taskEnum;
        this.startTime = startTime;
        this.duration = duration;
    }

    public void setErrorText(String error) {
        this.error = error;
    }

    public StartupTaskEnum getTaskEnum() {
        return taskEnum;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "taskEnum: " + taskEnum + " startTime: " + startTime + " duration: " + duration + (error != null ? error : "");
    }
}
