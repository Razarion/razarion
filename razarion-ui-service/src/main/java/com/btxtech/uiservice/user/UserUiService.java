package com.btxtech.uiservice.user;

import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.unlock.UnlockUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 21.02.2017.
 */
@ApplicationScoped
public class UserUiService {
    private static final long SET_NAME_TIME = 1000 * 60 * 5;
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
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ModalDialogManager modalDialogManager;
    private UserContext userContext;
    private Consumer<UserContext> userRegistrationCallback;
    private SimpleScheduledFuture setUserNameFuture;

    public void start() {
        if (!isRegistered()) {
        } else if (!isRegisteredAndNamed()) {
            if (gameUiControlInstance.get().getGameEngineMode() == GameEngineMode.SLAVE) {
                activateSetUserNameTimer();
            }
        }

    }

    public void stop() {
        clearSetUserNameTimer();
    }

    public void activateSetUserNameTimer() {
        clearSetUserNameTimer();
        setUserNameFuture = simpleExecutorService.schedule(SET_NAME_TIME, modalDialogManager::showSetUserNameDialog, SimpleExecutorService.Type.USER_SET_NAME);
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        if (userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
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

    public void onUnlockItemLimitChanged(UnlockedItemPacket unlockedItemLimit) {
        userContext.setUnlockedItemLimit(unlockedItemLimit.getUnlockedItemLimit());
        itemCockpitService.onStateChanged();
    }

    public void setUserRegistrationListener(Consumer<UserContext> userRegistrationCallback) {
        this.userRegistrationCallback = userRegistrationCallback;
        if (userContext != null && userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
    }

    public boolean isRegistered() {
        return userContext.checkRegistered();
    }

    public boolean isRegisteredAndNamed() {
        return userContext.checkName();
    }

    public void onUserNameSet(UserContext userContext) {
        this.userContext = userContext;
        if (userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
        clearSetUserNameTimer();
    }

    public void clearSetUserNameTimer() {
        if (setUserNameFuture != null) {
            setUserNameFuture.cancel();
            setUserNameFuture = null;
        }
    }
}
