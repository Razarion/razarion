package com.btxtech.server.rest.editor;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.engine.quest.QuestBackendInfo;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.UserBackendInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/editor/user-mgmt")
public class UserMgmtController {
    private final ServerLevelQuestService serverLevelQuestService;
    private final ServerUnlockService serverUnlockService;
    private final UserService userService;

    public UserMgmtController(ServerLevelQuestService serverLevelQuestService, ServerUnlockService serverUnlockService, UserService userService) {
        this.serverLevelQuestService = serverLevelQuestService;
        this.serverUnlockService = serverUnlockService;
        this.userService = userService;
    }

    @SecurityCheck
    @PostMapping(value = "set-level/{userId}/{levelId}")
    public void setLevel(@PathVariable("userId") int userId, @PathVariable("levelId") int levelId) {
        serverLevelQuestService.setUserLevel(userId, levelId);
    }

    @SecurityCheck
    @PostMapping(value = "set-crystals/{userId}/{crystals}")
    public void setCrystals(@PathVariable("userId") int userId, @PathVariable("crystals") int crystals) {
        userService.persistCrystals(userId, crystals);
    }

    @SecurityCheck
    @PostMapping(value = "set-completed-quests/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setCompletedQuests(@PathVariable("userId") int userId, @RequestBody List<Integer> completedQuestIds) {
        userService.setCompletedQuest(userId, completedQuestIds);
    }

    @SecurityCheck
    @PostMapping(value = "set-unlocked/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setUnlocked(@PathVariable("userId") int userId, @RequestBody List<Integer> unlockedIds) {
        serverUnlockService.updateUnlocked(userId, unlockedIds);
    }

    @SecurityCheck
    @GetMapping(value = "get-user-backend-infos", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserBackendInfo> getUserBackendInfos() {
        return userService.getUserBackendInfos();
    }

    @SecurityCheck
    @GetMapping(value = "get-quest-backend-infos", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuestBackendInfo> getQuestBackendInfos() {
        return serverLevelQuestService.getQuestBackendInfos();
    }

    @SecurityCheck
    @PostMapping(value = "activate-quest/{userId}/{questId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void activateQuest(@PathVariable("userId") int userId, @PathVariable("questId") Integer questId) {
        serverLevelQuestService.activateQuestBackend(userId, questId);
    }

    @SecurityCheck
    @PostMapping(value = "deactivate-quest/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deactivateQuest(@PathVariable("userId") int userId) {
        serverLevelQuestService.deactivateQuestBackend(userId);
    }


}
