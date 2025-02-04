package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.rest.dto.QuestBackendInfo;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.UserBackendInfo;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.USER_MGMT_CONTROLLER_PATH)
public class UserMgmtController {
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private ServerUnlockService serverUnlockService;
    @Inject
    private UserService userService;

    @POST
    @Path("set-level/{userId}/{levelId}")
    @SecurityCheck
    public void setLevel(@PathParam("userId") int userId, @PathParam("levelId") int levelId) {
        serverLevelQuestService.setUserLevel(userId, levelId);
    }

    @POST
    @Path("set-crystals/{userId}/{crystals}")
    @SecurityCheck
    public void setCrystals(@PathParam("userId") int userId, @PathParam("crystals") int crystals) {
        userService.persistCrystals(userId, crystals);
    }

    @POST
    @Path("set-completed-quests/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public void setCompletedQuests(@PathParam("userId") int userId, List<Integer> completedQuestIds) {
        userService.setCompletedQuest(userId, completedQuestIds);
    }

    @POST
    @Path("set-unlocked/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public void setUnlocked(@PathParam("userId") int userId, List<Integer> unlockedIds) {
        serverUnlockService.updateUnlocked(userId, unlockedIds);
    }

    @POST
    @Path("get-user-id-for-email/{email}")
    @SecurityCheck
    public int getUserIdForEmail(@PathParam("email") String email) {
        return userService.getUserEntity4Email(email).getId();
    }

    @GET
    @Path("get-user-backend-infos")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public List<UserBackendInfo> getUserBackendInfos() {
        return userService.getUserBackendInfos();
    }

    @GET
    @Path("get-quest-backend-infos")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public List<QuestBackendInfo> getQuestBackendInfos() {
        return serverLevelQuestService.getQuestBackendInfos();
    }

    @POST
    @Path("activate-quest/{userId}/{questId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public void activateQuest(@PathParam("userId") int userId, @PathParam("questId") Integer questId) {
        serverLevelQuestService.activateQuestBackend(userId, questId);
    }

    @POST
    @Path("deactivate-quest/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck
    public void deactivateQuest(@PathParam("userId") int userId) {
        serverLevelQuestService.deactivateQuestBackend(userId);
    }


}
