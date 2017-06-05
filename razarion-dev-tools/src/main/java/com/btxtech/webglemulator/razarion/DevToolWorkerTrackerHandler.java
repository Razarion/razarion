package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;

/**
 * Created by Beat
 * on 04.06.2017.
 */
public class DevToolWorkerTrackerHandler extends WorkerTrackerHandler {
    @Override
    protected void sendToServer(TrackingContainer tmpTrackingContainer) {
        System.out.println("**** send tracking to server");
    }
}
