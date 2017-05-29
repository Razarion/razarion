package com.btxtech.server.rest;

import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @POST
    @Path("sessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<SessionTracker> sessions(SearchConfig searchConfig) {
        try {
            return trackerPersistence.readSessionTracking(searchConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @GET
    @Path("sessiondetail/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SessionDetail sessiondetail(@PathParam("id") String id) {
        try {
            return trackerPersistence.readSessionDetail(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
