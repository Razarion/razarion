package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.TrackerService;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public class DevToolTrackerService implements TrackerService {
    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        System.out.println("DevToolTrackerService.trackGameUiControl() startTimeStamp: " + startTimeStamp);
    }

    @Override
    public void trackScene(Date startTimeStamp, String sceneInternalName) {
        System.out.println("DevToolTrackerService.trackScene() startTimeStamp: " + startTimeStamp + " sceneInternalName: " + sceneInternalName);
    }
}
