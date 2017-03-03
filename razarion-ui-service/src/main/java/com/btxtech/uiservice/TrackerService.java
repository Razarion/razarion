package com.btxtech.uiservice;

import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;

import java.util.Date;

/**
 * Created by Beat
 * 03.03.2017.
 */
public interface TrackerService {
    void trackGameUiControl(Date startTimeStamp);

    void trackScene(Date startTimeStamp, int sceneId);
}
