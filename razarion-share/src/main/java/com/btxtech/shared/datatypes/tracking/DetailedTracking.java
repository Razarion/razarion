package com.btxtech.shared.datatypes.tracking;

import java.util.Date;

/**
 * Created by Beat
 * 26.05.2017.
 */
public abstract class DetailedTracking {
    private Date timeStamp;
    private String gameSessionUuid;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public DetailedTracking setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public DetailedTracking setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }
}
