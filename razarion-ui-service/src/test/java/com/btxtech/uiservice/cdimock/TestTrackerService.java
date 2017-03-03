package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.TrackerService;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class TestTrackerService implements TrackerService {
    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trackScene(Date startTimeStamp, int sceneId) {
        throw new UnsupportedOperationException();
    }
}
