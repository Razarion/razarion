package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class ServerTrackingContainer {
    private TrackingStart trackingStart;
    private Date time;
    private List<TrackingContainer> trackingContainers = new ArrayList<>();

    public ServerTrackingContainer(TrackingStart trackingStart) {
        this.trackingStart = trackingStart;
        time = new Date();
    }

    public String getGameSessionUuid() {
        return trackingStart.getGameSessionUuid();
    }

    public int getPlanetId() {
        return trackingStart.getPlanetId();
    }

    public TrackingStart getTrackingStart() {
        return trackingStart;
    }

    public Date getTime() {
        return time;
    }

    public void addTrackingContainer(TrackingContainer trackingContainer) {
        trackingContainers.add(trackingContainer);
    }

    public TrackingContainer generateTrackingContainer() {
        TrackingContainer result = new TrackingContainer();
        for (TrackingContainer trackingContainer : trackingContainers) {
            if (trackingContainer.getCameraTrackings() != null) {
                result.getCameraTrackings().addAll(trackingContainer.getCameraTrackings());
            }
            if (trackingContainer.getBrowserWindowTrackings() != null) {
                result.getBrowserWindowTrackings().addAll(trackingContainer.getBrowserWindowTrackings());
            }
            if (trackingContainer.getMouseMoveTrackings() != null) {
                result.getMouseMoveTrackings().addAll(trackingContainer.getMouseMoveTrackings());
            }
            if (trackingContainer.getMouseButtonTrackings() != null) {
                result.getMouseButtonTrackings().addAll(trackingContainer.getMouseButtonTrackings());
            }
            if (trackingContainer.getPlayerBaseTrackings() != null) {
                result.getPlayerBaseTrackings().addAll(trackingContainer.getPlayerBaseTrackings());
            }
            if (trackingContainer.getSyncBaseItemTrackings() != null) {
                result.getSyncBaseItemTrackings().addAll(trackingContainer.getSyncBaseItemTrackings());
            }
        }
        result.getCameraTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getBrowserWindowTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getMouseMoveTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getMouseButtonTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getPlayerBaseTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        result.getSyncBaseItemTrackings().sort(Comparator.comparing(DetailedTracking::getTimeStamp));
        return result;
    }
}
