package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.ModalDialogManager;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.09.2016.
 */
public abstract class AbstractModalDialogManager implements ModalDialogManager {
    @Inject
    private QuestService questService;
    private Runnable levelUpCallback;
    private Runnable questPassedCallback;

    abstract protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener);

    abstract protected void showLevelUp(UserContext userContext, Runnable closeListener);

    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig) {
        showQuestPassed(questDescriptionConfig, () -> {
            if (questPassedCallback != null) {
                Runnable tmpQuestPassedCallback = questPassedCallback;
                questPassedCallback = null;
                tmpQuestPassedCallback.run();
            }
        });
    }

    public void onLevelPassed(UserContext userContext, LevelConfig oldLevel, LevelConfig newLevel) {
        showLevelUp(userContext, () -> {
            if (levelUpCallback != null) {
                Runnable tmpLevelUpCallback = levelUpCallback;
                levelUpCallback = null;
                tmpLevelUpCallback.run();
            }
        });
    }

    public void setLevelUpDialogCallback(Runnable callback) {
        levelUpCallback = callback;
    }

    public void setQuestPassedCallback(Runnable callback) {
        questPassedCallback = callback;
    }
}
