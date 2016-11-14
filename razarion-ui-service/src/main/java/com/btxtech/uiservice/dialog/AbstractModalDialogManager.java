package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.LevelServiceListener;
import com.btxtech.shared.gameengine.datatypes.ModalDialogManager;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.09.2016.
 */
public abstract class AbstractModalDialogManager implements ModalDialogManager, QuestListener, LevelServiceListener {
    @Inject
    private QuestService questService;
    @Inject
    private LevelService levelService;
    @Inject
    private ActivityService activityService;
    private Runnable levelUpCallback;
    private Runnable questPassedCallback;

    abstract protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, ApplyListener<QuestDescriptionConfig> applyListener);

    abstract protected void showLevelUp(UserContext userContext, ApplyListener<Void> applyListener);

    @PostConstruct
    public void init() {
        questService.addQuestListener(this);
        levelService.addLevelServiceListener(this);
        activityService.setModalDialogManager(this);
    }

    public void showQuestPassed(QuestDescriptionConfig questDescriptionConfig) {
        showQuestPassed(questDescriptionConfig, ignore -> {
            if (questPassedCallback != null) {
                Runnable tmpQuestPassedCallback = questPassedCallback;
                questPassedCallback = null;
                tmpQuestPassedCallback.run();
            }
        });
    }

    @Override
    public void onQuestPassed(UserContext examinee, QuestConfig questConfig) {
        showQuestPassed(questConfig);
    }

    @Override
    public void onLevelPassed(UserContext userContext, LevelConfig oldLevel, LevelConfig newLevel) {
        showLevelUp(userContext, aVoid -> {
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
