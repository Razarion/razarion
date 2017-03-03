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

package com.btxtech.server.persistence.tracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * User: beat
 * Date: 12.01.2010
 * Time: 22:45:47
 */
@Entity
@Table(name = "TRACKER_STARTUP_TASK", indexes = { @Index(columnList = "sessionId"),  @Index(columnList = "gameSessionUuid") })
public class StartupTaskEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String sessionId;
    private String gameSessionUuid;
    private String taskEnum;
    @Column(nullable = false)
    private Date startTime;
    private Date clientStartTime;
    private int duration;
    @Column(length = 50000)
    private String error;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public void setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
    }

    public String getTaskEnum() {
        return taskEnum;
    }

    public void setTaskEnum(String taskEnum) {
        this.taskEnum = taskEnum;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getClientStartTime() {
        return clientStartTime;
    }

    public void setClientStartTime(Date clientStartTime) {
        this.clientStartTime = clientStartTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartupTaskEntity that = (StartupTaskEntity) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}