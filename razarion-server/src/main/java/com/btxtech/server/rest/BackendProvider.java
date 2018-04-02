package com.btxtech.server.rest;

import com.btxtech.server.mgmt.OnlineInfo;
import com.btxtech.server.mgmt.UserBackendInfo;
import com.btxtech.server.persistence.history.UserHistoryEntry;
import com.btxtech.server.persistence.item.ItemTracking;
import com.btxtech.server.persistence.item.ItemTrackingSearch;
import com.btxtech.server.persistence.tracker.SearchConfig;
import com.btxtech.server.persistence.tracker.SessionDetail;
import com.btxtech.server.persistence.tracker.SessionTracker;
import com.btxtech.server.user.NewUser;
import com.btxtech.shared.CommonUrl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 * on 08.02.2018.
 */
@Path(CommonUrl.BACKEND_PATH)
public interface BackendProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadallonlines")
    List<OnlineInfo> loadAllOnlines();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadbackenduserinfo/{playerId}")
    UserBackendInfo loadBackendUserInfo(@PathParam("playerId") int playerId);

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removecompletedquest/{playerId}/{questId}")
    UserBackendInfo removeCompletedQuest(@PathParam("playerId") int playerId, @PathParam("questId") int questId);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addcompletedquest")
    UserBackendInfo addCompletedQuest(@FormParam("playerId") int playerId, @FormParam("questId") int questId);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setlevelnumber")
    UserBackendInfo setLevelNumber(@FormParam("playerId") int playerId, @FormParam("levelNumber") int levelNumber);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setxp")
    UserBackendInfo setXp(@FormParam("playerId") int playerId, @FormParam("xp") int xp);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setcrystals")
    UserBackendInfo setCrystals(@FormParam("playerId") int playerId, @FormParam("crystals") int crystals);

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeunlocked/{playerId}/{unlockedBackendInfoId}")
    UserBackendInfo removeUnlocked(@PathParam("playerId") int playerId, @PathParam("unlockedBackendInfoId") int unlockedBackendInfoId);

    @POST
    @Path("sessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<SessionTracker> sessions(SearchConfig searchConfig);

    @GET
    @Path("sessiondetail/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    SessionDetail sessiondetail(@PathParam("id") String id);

    @GET
    @Path("newusers")
    @Produces(MediaType.APPLICATION_JSON)
    List<NewUser> newUsers();

    @GET
    @Path("userhistory")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserHistoryEntry> userHistory();

    @POST
    @Path("itemhistory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<ItemTracking> itemHistory(ItemTrackingSearch itemTrackingSearch);

    @POST
    @Path("sendrestartlifecycle")
    void sendRestartLifecycle();
}
