package com.btxtech.worker;

import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.gameengine.WorkerTrackerHandler;
import com.btxtech.shared.rest.TrackerControllerFactory;
import com.btxtech.shared.system.SimpleExecutorService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 01.06.2017.
 */

public class ClientWorkerTrackerHandler extends WorkerTrackerHandler {
    private final Logger logger = Logger.getLogger(ClientWorkerTrackerHandler.class.getName());

    @Inject
    public ClientWorkerTrackerHandler(SimpleExecutorService simpleExecutorService) {
        super(simpleExecutorService);
    }

    @Override
    protected void sendToServer(TrackingContainer tmpTrackingContainer) {
        TrackerControllerFactory.INSTANCE.detailedTracking(tmpTrackingContainer).onFailed(fail -> {
            logger.log(Level.SEVERE, "detailedTracking failed: " + fail.getStatusText(), fail.getThrowable());
        });
    }
}
