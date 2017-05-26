package com.btxtech.shared.datatypes.tracking;

import java.util.Date;

/**
 * Created by Beat
 * 26.05.2017.
 */
public abstract class DetailedTracking {
    private Date timeStamp;
    private String startUuid;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public DetailedTracking setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getStartUuid() {
        return startUuid;
    }

    public DetailedTracking setStartUuid(String startUuid) {
        this.startUuid = startUuid;
        return this;
    }
}
