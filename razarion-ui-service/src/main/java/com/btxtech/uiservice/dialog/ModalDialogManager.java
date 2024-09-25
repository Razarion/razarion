package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Singleton
public class ModalDialogManager {
    private Runnable levelUpCallback;
    private Runnable questPassedCallback;
    private Runnable baseLostCallback;
    private ModelDialogPresenter modelDialogPresenter;

    @Inject
    public ModalDialogManager() {
    }

    public void init(ModelDialogPresenter modelDialogPresenter) {
        this.modelDialogPresenter = modelDialogPresenter;
    }

    protected void showLevelUp(LevelUpPacket levelUpPacket, Runnable closeListener) {
        if (modelDialogPresenter != null) {
            // TODO closeListener not working, levelUpPacket not used
            modelDialogPresenter.showLevelUp();
        }
    }

    public void showBoxPicked(BoxContent boxContent) {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showBoxPicked(boxContent);
        }
    }

    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showUseInventoryItemLimitExceeded(baseItemType);
        }
    }

    public void showUseInventoryHouseSpaceExceeded() {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showUseInventoryHouseSpaceExceeded();
        }
    }

    protected void showBaseLost(Runnable closeListener) {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showBaseLost();
        }
    }

    public void showLeaveStartTutorial(Runnable closeListener) {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showLeaveStartTutorial(closeListener);
        }
    }

    public void showMessageDialog(String title, String message) {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showMessageDialog(title, message);
        }
    }

    public void showRegisterDialog() {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showRegisterDialog();
        }
    }

    public void showSetUserNameDialog() {
        if (modelDialogPresenter != null) {
            modelDialogPresenter.showSetUserNameDialog();
        }
    }

    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig) {
        showQuestPassed(questDescriptionConfig, () -> {
            if (questPassedCallback != null) {
                Runnable tmpQuestPassedCallback = questPassedCallback;
                questPassedCallback = null;
                tmpQuestPassedCallback.run();
            }
        });
    }

    private void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        if (modelDialogPresenter != null) {
            // TODO closeListener not working, questDescriptionConfig not used
            modelDialogPresenter.showQuestPassed();
        }
    }

    public void onLevelPassed(LevelUpPacket levelUpPacket) {
        showLevelUp(levelUpPacket, () -> {
            if (levelUpCallback != null) {
                Runnable tmpLevelUpCallback = levelUpCallback;
                levelUpCallback = null;
                tmpLevelUpCallback.run();
            }
        });
    }

    public void onShowBaseLost() {
        showBaseLost(() -> {
            if (baseLostCallback != null) {
                Runnable tmpBaseLostCallback = baseLostCallback;
                baseLostCallback = null;
                tmpBaseLostCallback.run();
            }
        });
    }

    public void setLevelUpDialogCallback(Runnable callback) {
        levelUpCallback = callback;
    }

    public void setQuestPassedCallback(Runnable callback) {
        questPassedCallback = callback;
    }

    public void setBaseLostCallback(Runnable baseLostCallback) {
        this.baseLostCallback = baseLostCallback;
    }

}
