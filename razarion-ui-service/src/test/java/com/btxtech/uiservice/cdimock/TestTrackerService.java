package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.renderer.ViewField;

import javax.inject.Singleton;
import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Singleton
public class TestTrackerService implements TrackerService {
    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trackScene(Date startTimeStamp, String sceneInternalName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startDetailedTracking(int planetId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopDetailedTracking() {
        throw new UnsupportedOperationException();
    }
}
