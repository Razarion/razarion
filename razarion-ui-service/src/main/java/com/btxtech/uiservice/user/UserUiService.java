package com.btxtech.uiservice.user;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FacebookUserLoginInfo;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.02.2017.
 */
@ApplicationScoped
public class UserUiService {
    private static final String FACEBOOK_STATUS_CONNECTED = "connected";
    private Logger logger = Logger.getLogger(UserUiService.class.getName());
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private LevelService levelService;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private ModalDialogManager dialogManager;
    @Inject
    private Instance<GameUiControl> gameUiControlInstance;
    private FacebookUserLoginInfo facebookUserLoginInfo;
    private UserContext userContext;

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public void facebookLoginState(String status, String accessToken, Integer expiresIn, String signedRequest, String userId) {
        if (FACEBOOK_STATUS_CONNECTED.equalsIgnoreCase(status)) {
            facebookUserLoginInfo = new FacebookUserLoginInfo();
            facebookUserLoginInfo.setAccessToken(accessToken).setExpiresIn(expiresIn).setSignedRequest(signedRequest).setUserId(userId);
        } else {
            facebookUserLoginInfo = null;
        }
    }

    public void facebookLoginState(String status) {
        facebookUserLoginInfo = null;
        logger.warning("Facebook login unclear: " + status);
    }

    public void facebookLoginStateFailed() {
        facebookUserLoginInfo = null;
    }

    public FacebookUserLoginInfo getFacebookUserLoginInfo() {
        return facebookUserLoginInfo;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public boolean isAdmin() {
        return userContext.isAdmin();
    }

    public void increaseXp(int deltaXp) {
        int xp = userContext.getXp() + deltaXp;
        LevelConfig levelConfig = levelService.getLevel(userContext.getLevelId());
        if (xp >= levelConfig.getXp2LevelUp()) {
            LevelConfig newLevelConfig = levelService.getNextLevel(levelConfig);
            userContext.setLevelId(newLevelConfig.getLevelId());
            userContext.setXp(0);
            gameEngineControl.updateLevel(newLevelConfig.getLevelId());
            cockpitService.updateLevelAndXp(userContext);
            itemCockpitService.onStateChanged();
            dialogManager.onLevelPassed(newLevelConfig);
            gameUiControlInstance.get().onLevelUpdate(newLevelConfig);
        } else {
            userContext.setXp(xp);
            cockpitService.updateLevelAndXp(userContext);
        }
    }

    public void onOnBoxPicked(BoxContent boxContent) {
        for (InventoryItem inventoryItem : boxContent.getInventoryItems()) {
            userContext.addInventoryItem(inventoryItem.getId());
        }
        dialogManager.showBoxPicked(boxContent);
    }
}
