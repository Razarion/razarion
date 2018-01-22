package com.btxtech.server.rest;

import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.user.SecurityException;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
    // private Logger logger = Logger.getLogger(ServerMgmt.class.getName());
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
        } catch (SecurityException t) {
            exceptionHandler.handleException(t);
            throw new ForbiddenException(); // Unfortunately, resteasy log this exception
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setlevelnumber")
    public UserBackendInfo setLevelNumber(@FormParam("playerId") int playerId, @FormParam("levelNumber") int levelNumber) {
        try {
            return serverMgmt.setLevelNumber(playerId, levelNumber);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setxp")
    public UserBackendInfo setXp(@FormParam("playerId") int playerId, @FormParam("xp") int xp) {
        try {
            return serverMgmt.setXp(playerId, xp);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setcrystals")
    public UserBackendInfo setCrystals(@FormParam("playerId") int playerId, @FormParam("crystals") int crystals) {
        try {
            return serverMgmt.setCrystals(playerId, crystals);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeunlocked/{playerId}/{unlockedBackendInfoId}")
    public UserBackendInfo removeUnlocked(@PathParam("playerId") int playerId, @PathParam("unlockedBackendInfoId") int unlockedBackendInfoId) {
        try {
            return serverMgmt.removeUnlockedItem(playerId, unlockedBackendInfoId);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }


}
