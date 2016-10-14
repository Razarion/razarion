package com.btxtech.uiservice.clip;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ClipConfig;

/**
 * Created by Beat
 * 14.10.2016.
 */
public class PlayingClip {
    private final Vertex position;
    private final Vertex norm;
    private final ClipConfig clipConfig;
    private long endTimeStamp;

    public PlayingClip(Vertex position, Vertex norm, ClipConfig clipConfig, long timeStamp) {
        this.position = position;
        this.norm = norm;
        this.clipConfig = clipConfig;
        endTimeStamp = timeStamp + clipConfig.getDurationMillis();
    }

    public ClipConfig getClipConfig() {
        return clipConfig;
    }

    public ModelMatrices provideModelMatrices(long timeStamp) {
        int delta = (int) (endTimeStamp - timeStamp);
        if (delta < 0) {
            return null;
        }
        return ModelMatrices.createFromPositionAndDirection(position, norm).setProgress((double) delta / (double) clipConfig.getDurationMillis());
    }
}
