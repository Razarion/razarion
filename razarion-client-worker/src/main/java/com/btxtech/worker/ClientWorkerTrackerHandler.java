package com.btxtech.worker;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
import com.btxtech.shared.rest.TrackerProvider;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 01.06.2017.
 */
@Dependent
public class ClientWorkerTrackerHandler extends WorkerTrackerHandler {
    private Logger logger = Logger.getLogger(WorkerTrackerHandler.class.getName());
    @Inject
    private Caller<TrackerProvider> trackingProvider;

    @Override
    protected void sendToServer(TrackingContainer tmpTrackingContainer) {
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "detailedTracking failed: " + message, throwable);
            return false;
        }).detailedTracking(tmpTrackingContainer);
    }
}