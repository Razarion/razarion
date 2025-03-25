package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class PlaybackGameUiControlConfig {
    private TrackingStart trackingStart;
    private TrackingContainer trackingContainer;

    public TrackingContainer getTrackingContainer() {
        return trackingContainer;
    }

    public PlaybackGameUiControlConfig setTrackingContainer(TrackingContainer trackingContainer) {
        this.trackingContainer = trackingContainer;
        return this;
    }

    public TrackingStart getTrackingStart() {
        return trackingStart;
    }

    public PlaybackGameUiControlConfig setTrackingStart(TrackingStart trackingStart) {
        this.trackingStart = trackingStart;
        return this;
    }
}
