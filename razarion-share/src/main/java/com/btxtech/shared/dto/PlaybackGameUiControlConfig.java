package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.tracking.ViewFieldTracking;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class PlaybackGameUiControlConfig {
    private List<ViewFieldTracking> viewFieldTrackings;

    public List<ViewFieldTracking> getViewFieldTrackings() {
        return viewFieldTrackings;
    }

    public PlaybackGameUiControlConfig setViewFieldTrackings(List<ViewFieldTracking> viewFieldTrackings) {
        this.viewFieldTrackings = viewFieldTrackings;
        return this;
    }
}
