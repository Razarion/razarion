package com.btxtech.worker;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.client.Caller;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 01.06.2017.
 */

public class ClientWorkerTrackerHandler extends WorkerTrackerHandler {
    // private Logger logger = Logger.getLogger(WorkerTrackerHandler.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<TrackerProvider> trackingProvider;

    @Override
    protected void sendToServer(TrackingContainer tmpTrackingContainer) {
        trackingProvider.call(response -> {
        }, exceptionHandler.restErrorHandler("detailedTracking failed: ")).detailedTracking(tmpTrackingContainer);
    }
}
