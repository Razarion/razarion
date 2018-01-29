package com.btxtech.server.rest;

import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.item.ItemTrackerAccess;
import com.btxtech.server.persistence.item.ItemTracking;
import com.btxtech.server.persistence.item.ItemTrackingSearch;
import com.btxtech.server.persistence.tracker.SearchConfig;
import com.btxtech.server.persistence.tracker.SessionDetail;
import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.NewUser;
import com.btxtech.server.persistence.history.UserHistoryEntry;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.CommonUrl;
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
@Path(CommonUrl.TRACKER_BACKEND_PATH)
public class TrackerBackend {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private UserService userService;
    @Inject
    private HistoryPersistence historyPersistence;
    @Inject
    private ItemTrackerAccess itemTrackerAccess;

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

    @GET
    @Path("newusers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NewUser> newUsers() {
        try {
            return userService.findNewUsers();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @GET
    @Path("userhistory")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserHistoryEntry> userHistory() {
        try {
            return historyPersistence.readLoginHistory();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @POST
    @Path("itemhistory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemTracking> itemHistory(ItemTrackingSearch itemTrackingSearch) {
        try {
            return itemTrackerAccess.read(itemTrackingSearch);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
