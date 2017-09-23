package com.btxtech.uiservice.user;

import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.unlock.UnlockUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Map;

/**
 * Created by Beat
 * 21.02.2017.
 */
@ApplicationScoped
public class UserUiService {
    // private Logger logger = Logger.getLogger(UserUiService.class.getName());
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
    @Inject
    private UnlockUiService unlockUiService;
    private UserContext userContext;

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
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
            dialogManager.onLevelPassed(new LevelUpPacket().setUserContext(userContext));
            gameUiControlInstance.get().onLevelUpdate(newLevelConfig);
        } else {
            userContext.setXp(xp);
            cockpitService.updateLevelAndXp(userContext);
        }
    }

    public void onServerLevelChange(LevelUpPacket levelUpPacket) {
        userContext = levelUpPacket.getUserContext();
        cockpitService.updateLevelAndXp(userContext);
        itemCockpitService.onStateChanged();
        unlockUiService.setLevelUnlockConfigs(levelUpPacket.getLevelUnlockConfigs());
        dialogManager.onLevelPassed(levelUpPacket);
    }

    public void onServerXpChange(Integer xp) {
        userContext.setXp(xp);
        cockpitService.updateLevelAndXp(userContext);
    }

    public void onUnlockItemLimitChanged(Map<Integer, Integer> unlockedItemLimit) {
        userContext.setUnlockedItemLimit(unlockedItemLimit);
        itemCockpitService.onStateChanged();
    }

}
