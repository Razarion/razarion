package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.rest.UserMgmtController;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class UserMgmtControllerImpl implements UserMgmtController {
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
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
    @Transactional
    public int getUserIdForEmail(String email) {
        return userService.getUserEntity4Email(email).getId();
    }
}
