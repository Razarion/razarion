package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.renderer.ViewField;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class DevToolTrackerService implements TrackerService {
    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        // System.out.println("DevToolTrackerService.trackGameUiControl() startTimeStamp: " + startTimeStamp);
    }

    @Override
    public void trackScene(Date startTimeStamp, String sceneInternalName) {
        // System.out.println("DevToolTrackerService.trackScene() startTimeStamp: " + startTimeStamp + " sceneInternalName: " + sceneInternalName);
    }

    @Override
    public void startDetailedTracking(int planetId) {
        // System.out.println("DevToolTrackerService.startDetailedTracking()");
    }

    @Override
    public void stopDetailedTracking() {
        // System.out.println("DevToolTrackerService.stopDetailedTracking()");
    }

    @Override
    public void onViewChanged(ViewField currentViewField) {
        // System.out.println("DevToolTrackerService.onViewChanged()");
    }
}
