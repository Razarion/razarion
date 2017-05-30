package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.tracking.CameraTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class PlaybackGameUiControlConfig {
    private Date originTime;
    private TrackingContainer trackingContainer;

    public TrackingContainer getTrackingContainer() {
        return trackingContainer;
    }

    public PlaybackGameUiControlConfig setTrackingContainer(TrackingContainer trackingContainer) {
        this.trackingContainer = trackingContainer;
        return this;
    }

    public Date getOriginTime() {
        return originTime;
    }

    public PlaybackGameUiControlConfig setOriginTime(Date originTime) {
        this.originTime = originTime;
        return this;
    }
}
