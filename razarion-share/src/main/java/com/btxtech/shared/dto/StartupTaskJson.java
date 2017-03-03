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

package com.btxtech.shared.dto;

import java.util.Date;

/**
 * User: beat
 * Date: 05.12.2010
 * Time: 23:23:42
 */
public class StartupTaskJson {
    private String gameSessionUuid;
    private String taskEnum;
    private Date startTime;
    private int duration;
    private String error;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public StartupTaskJson setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public String getTaskEnum() {
        return taskEnum;
    }

    public StartupTaskJson setTaskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public StartupTaskJson setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public StartupTaskJson setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getError() {
        return error;
    }

    public StartupTaskJson setError(String error) {
        this.error = error;
        return this;
    }
}
