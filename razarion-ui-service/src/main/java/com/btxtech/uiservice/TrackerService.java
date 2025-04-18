package com.btxtech.uiservice;

import com.btxtech.uiservice.renderer.ViewField;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public interface TrackerService {
    void trackGameUiControl(Date startTimeStamp);

    void trackScene(Date startTimeStamp, String sceneInternalName);

    void startDetailedTracking(int planetId);

    void stopDetailedTracking();
}
