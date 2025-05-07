package com.btxtech.uiservice.user;

import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nConstants;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Singleton
public class UserUiService {
    private static final long SET_NAME_TIME = 1000 * 60 * 5;
    private static final long REGISTER_TIME = 1000 * 60 * 4;
    // private Logger logger = Logger.getLogger(UserUiService.class.getName());
    private final Provider<GameEngineControl> gameEngineControl;
    private final LevelService levelService;
    private final MainCockpitService cockpitService;
    private final ItemCockpitService itemCockpitService;
    private final ModalDialogManager dialogManager;
    private final Provider<GameUiControl> gameUiControlInstance;
    private final SimpleExecutorService simpleExecutorService;
    private final ModalDialogManager modalDialogManager;
    private UserContext userContext;
    private Consumer<UserContext> userRegistrationCallback;
    private SimpleScheduledFuture setUserNameFuture;
    private SimpleScheduledFuture registerFuture;

    @Inject
    public UserUiService(ModalDialogManager modalDialogManager,
                         SimpleExecutorService simpleExecutorService,
                         Provider<GameUiControl> gameUiControlInstance,
                         ModalDialogManager dialogManager,
                         ItemCockpitService itemCockpitService,
                         MainCockpitService cockpitService,
                         LevelService levelService,
                         Provider<GameEngineControl> gameEngineControl) {
        this.modalDialogManager = modalDialogManager;
        this.simpleExecutorService = simpleExecutorService;
        this.gameUiControlInstance = gameUiControlInstance;
        this.dialogManager = dialogManager;
        this.itemCockpitService = itemCockpitService;
        this.cockpitService = cockpitService;
        this.levelService = levelService;
        this.gameEngineControl = gameEngineControl;
    }

    public void init(UserContext userContext) {
        this.userContext = userContext;
        if (userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
    }

    public void start() {
        if (!isRegistered()) {
            activateRegisterTimer();
        } else if (!isRegisteredAndNamed()) {
            if (gameUiControlInstance.get().getGameEngineMode() == GameEngineMode.SLAVE) {
                activateSetUserNameTimer();
            }
        }
    }

    public void stop() {
        clearSetUserNameTimer();
        clearRegisterTimer();
    }

    public void activateSetUserNameTimer() {
        clearSetUserNameTimer();
        setUserNameFuture = simpleExecutorService.schedule(SET_NAME_TIME, modalDialogManager::showSetUserNameDialog, SimpleExecutorService.Type.USER_SET_NAME);
    }

    public void activateRegisterTimer() {
        clearRegisterTimer();
        registerFuture = simpleExecutorService.schedule(REGISTER_TIME, modalDialogManager::showRegisterDialog, SimpleExecutorService.Type.REGISTER);
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void increaseXp(int deltaXp) {
        int xp = userContext.getXp() + deltaXp;
        LevelConfig levelConfig = levelService.getLevel(userContext.getLevelId());
        if (xp >= levelConfig.getXp2LevelUp()) {
            LevelConfig newLevelConfig = levelService.getNextLevel(levelConfig);
            userContext.levelId(newLevelConfig.getId());
            userContext.xp(0);
            gameEngineControl.get().updateLevel(newLevelConfig.getId());
            cockpitService.updateLevelAndXp(userContext);
            itemCockpitService.onStateChanged();
            dialogManager.onLevelPassed(new LevelUpPacket().userContext(userContext));
            gameUiControlInstance.get().onLevelUpdate(newLevelConfig);
        } else {
            userContext.xp(xp);
            cockpitService.updateLevelAndXp(userContext);
        }
    }

    public void onServerLevelChange(LevelUpPacket levelUpPacket) {
        userContext = levelUpPacket.getUserContext();
        cockpitService.updateLevelAndXp(userContext);
        itemCockpitService.onStateChanged();
        cockpitService.blinkAvailableUnlock(levelUpPacket.isAvailableUnlocks());
        dialogManager.onLevelPassed(levelUpPacket);
    }

    public void onServerXpChange(Integer xp) {
        userContext.xp(xp);
        cockpitService.updateLevelAndXp(userContext);
    }

    public void onUnlockItemLimitChanged(UnlockedItemPacket unlockedItemLimit) {
        userContext.unlockedItemLimit(unlockedItemLimit.getUnlockedItemLimit());
        itemCockpitService.onStateChanged();
        cockpitService.blinkAvailableUnlock(unlockedItemLimit.isAvailableUnlocks());
    }

    public void setUserRegistrationListener(Consumer<UserContext> userRegistrationCallback) {
        this.userRegistrationCallback = userRegistrationCallback;
        if (userContext != null && userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
    }

    public boolean isRegistered() {
        return userContext.registered();
    }

    public boolean isEmailNotVerified() {
        return userContext.emailNotVerified();
    }

    public boolean isRegisteredAndNamed() {
        return userContext.checkName();
    }

    public void onUserRegistered(boolean emailNotVerified) {
        if (emailNotVerified) {
            userContext.registerState(UserContext.RegisterState.EMAIL_UNVERIFIED);
        } else {
            userContext.registerState(UserContext.RegisterState.EMAIL_VERIFIED);
        }
        clearRegisterTimer();
        if (gameUiControlInstance.get().getGameEngineMode() == GameEngineMode.SLAVE) {
            activateSetUserNameTimer();
        }
        if (userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
    }

    public void onUserNameSet(String userName) {
        userContext.name(userName);
        if (userRegistrationCallback != null) {
            userRegistrationCallback.accept(userContext);
        }
        clearSetUserNameTimer();
    }


    public void onEmailVerified() {
        userContext.registerState(UserContext.RegisterState.EMAIL_UNVERIFIED);
        modalDialogManager.showMessageDialog(I18nConstants.registerThanks(), I18nConstants.registerThanksLong());
    }

    public void clearSetUserNameTimer() {
        if (setUserNameFuture != null) {
            setUserNameFuture.cancel();
            setUserNameFuture = null;
        }
    }

    public void clearRegisterTimer() {
        if (registerFuture != null) {
            registerFuture.cancel();
            registerFuture = null;
        }
    }
}
