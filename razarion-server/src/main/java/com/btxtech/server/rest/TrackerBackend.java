package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat on 29.05.2017.
 */
@Path(RestUrl.TRACKER_BACKEND_PATH)
public class TrackerBackend {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;

    @GET
    @Path("sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SessionTracker> sessions() {
        try {
            return trackerPersistence.readSessionTracking();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
