package com.btxtech.server.rest;

import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 05.09.2017.
 */
@Path(RestUrl.SERVER_MGMT_PROVIDER_PATH)
public class ServerMgmtProvider {
    @Inject
    private ServerMgmt serverMgmt;
    @Inject
    private ExceptionHandler exceptionHandler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadallonlines")
    public List<OnlineInfo> loadAllOnlines() {
        try {
            return serverMgmt.loadAllOnlines();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadbackenduserinfo/{playerId}")
    public UserBackendInfo loadBackendUserInfo(@PathParam("playerId") int playerId) {
        try {
            return serverMgmt.loadBackendUserInfo(playerId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removecompletedquest/{playerId}/{questId}")
    public UserBackendInfo removeCompletedQuest(@PathParam("playerId") int playerId, @PathParam("questId") int questId) {
        try {
            return serverMgmt.removeCompletedQuest(playerId, questId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
