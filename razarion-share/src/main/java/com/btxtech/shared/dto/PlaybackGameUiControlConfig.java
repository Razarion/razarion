package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.tracking.CameraTracking;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class PlaybackGameUiControlConfig {
    private Date originTime;
    private List<CameraTracking> cameraTrackings;

    public List<CameraTracking> getCameraTrackings() {
        return cameraTrackings;
    }

    public PlaybackGameUiControlConfig setCameraTrackings(List<CameraTracking> cameraTrackings) {
        this.cameraTrackings = cameraTrackings;
        return this;
    }

    public Date getOriginTime() {
        return originTime;
    }

    public void setOriginTime(Date originTime) {
        this.originTime = originTime;
    }
}
