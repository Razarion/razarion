package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.tracking.TrackingStart;

import java.util.Date;

/**
 * Created by Beat
 * on 02.06.2017.
 */
public class ServerTrackerStart {
    private TrackingStart trackingStart;
    private Date timeStamp;
    private String sessionId;

    public TrackingStart getTrackingStart() {
        return trackingStart;
    }

    public ServerTrackerStart setTrackingStart(TrackingStart trackingStart) {
        this.trackingStart = trackingStart;
        return this;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public ServerTrackerStart setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ServerTrackerStart setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }
}
