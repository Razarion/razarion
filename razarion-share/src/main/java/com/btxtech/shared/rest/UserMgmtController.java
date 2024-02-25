package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.UserBackendInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.USER_MGMT_CONTROLLER_PATH)
public interface UserMgmtController {
    @POST
    @Path("set-level/{userId}/{levelId}")
    void setLevel(@PathParam("userId") int userId, @PathParam("levelId") int levelId);

    @POST
    @Path("set-crystals/{userId}/{crystals}")
    void setCrystals(@PathParam("userId") int userId, @PathParam("crystals") int crystals);

    @POST
    @Path("set-completed-quests/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void setCompletedQuests(@PathParam("userId") int userId, List<Integer> completedQuestIds);

    @POST
    @Path("set-unlocked/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void setUnlocked(@PathParam("userId") int userId, List<Integer> unlockedIds);

    @POST
    @Path("get-user-id-for-email/{email}")
    int getUserIdForEmail(@PathParam("email") String email);

    @GET
    @Path("get-user-backend-infos")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserBackendInfo> getUserBackendInfos();
}
