package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class ServerTrackingContainer {
    private String sessionId;
    private Date timeStamp;
    private TrackingContainer trackingContainer;

    public String getSessionId() {
        return sessionId;
    }

    public ServerTrackingContainer setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public ServerTrackingContainer setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public TrackingContainer getTrackingContainer() {
        return trackingContainer;
    }

    public ServerTrackingContainer setTrackingContainer(TrackingContainer trackingContainer) {
        this.trackingContainer = trackingContainer;
        return this;
    }
}
