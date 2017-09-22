package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.09.2016.
 */
public abstract class ModalDialogManager {
    @Inject
    private QuestService questService;
    private Runnable levelUpCallback;
    private Runnable questPassedCallback;
    private Runnable baseLostCallback;

    protected abstract void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener);

    protected abstract void showLevelUp(LevelUpPacket levelUpPacket, Runnable closeListener);

    public abstract void showBoxPicked(BoxContent boxContent);

    public abstract void showUseInventoryItemLimitExceeded(BaseItemType baseItemType);

    public abstract void showUseInventoryHouseSpaceExceeded();

    protected abstract void showBaseLost(Runnable closeListener);

    public abstract void showLeaveStartTutorial(Runnable closeListener);

    public abstract void showMessageImageDialog(String title, String message, Integer imageId);

    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig) {
        showQuestPassed(questDescriptionConfig, () -> {
            if (questPassedCallback != null) {
                Runnable tmpQuestPassedCallback = questPassedCallback;
                questPassedCallback = null;
                tmpQuestPassedCallback.run();
            }
        });
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
