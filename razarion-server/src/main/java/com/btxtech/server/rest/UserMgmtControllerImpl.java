package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.UserBackendInfo;
import com.btxtech.shared.rest.UserMgmtController;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

public class UserMgmtControllerImpl implements UserMgmtController {
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private ServerUnlockService serverUnlockService;
    @Inject
    private UserService userService;

    @Override
    @SecurityCheck
    public void setLevel(int userId, int levelId) {
        serverLevelQuestService.setUserLevel(userId, levelId);
    }

    @Override
    @SecurityCheck
    public void setCrystals(int userId, int crystals) {
        userService.persistCrystals(userId, crystals);
    }

    @Override
    @SecurityCheck
    public void setCompletedQuests(int userId, List<Integer> completedQuestIds) {
        userService.setCompletedQuest(userId, completedQuestIds);

    }

    @Override
    @SecurityCheck
    public void setUnlocked(int userId, List<Integer> unlockedIds) {
        serverUnlockService.updateUnlocked(userId, unlockedIds);

    }

    @Override
    @SecurityCheck
    @Transactional
    public int getUserIdForEmail(String email) {
        return userService.getUserEntity4Email(email).getId();
    }

    @Override
    @SecurityCheck
    public List<UserBackendInfo> getUserBackendInfos() {
        return userService.getUserBackendInfos();
    }
}
