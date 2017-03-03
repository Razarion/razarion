package com.btxtech.shared.dto;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class GameUiControlTrackerInfo {
    private String gameSessionUuid;
    private Date startTime;
    private int duration;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public GameUiControlTrackerInfo setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public GameUiControlTrackerInfo setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public GameUiControlTrackerInfo setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}
