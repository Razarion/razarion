package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Dependent
// Better name: something with game-control
public class Scene implements ViewService.ViewFieldListener {
    private Logger logger = Logger.getLogger(Scene.class.getName());
    @Inject
    private ScreenCover screenCover;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ViewService viewService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private QuestVisualizer questVisualizer;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private AudioService audioService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private UserUiService userUiService;
    private SceneConfig sceneConfig;
    private int completionCallbackCount;
    private boolean hasCompletionCallback;
    private boolean scrollBouncePrevention = true;

    public void init(SceneConfig sceneConfig) {
        this.sceneConfig = sceneConfig;
        completionCallbackCount = 0;
    }

    public void run() {
        setupViewFieldConfig(sceneConfig.getViewFieldConfig());
        if (sceneConfig.isRemoveLoadingCover()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            screenCover.fadeOutLoadingCover();
            simpleExecutorService.schedule(ScreenCover.FADE_DURATION, () -> {
                screenCover.removeLoadingCover();
                onComplete();
            }, SimpleExecutorService.Type.UNSPECIFIED);
        }
        if (sceneConfig.getIntroText() != null) {
            screenCover.showStoryCover(sceneConfig.getIntroText());
        }
        if (sceneConfig.getBotConfigs() != null) {
            gameEngineControl.startBots(sceneConfig.getBotConfigs());
        }
        if (sceneConfig.getResourceItemTypePositions() != null) {
            gameEngineControl.createResources(sceneConfig.getResourceItemTypePositions());
        }
        if (sceneConfig.getBotMoveCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotMoveCommandConfigs());
        }
        if (sceneConfig.getBotHarvestCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotHarvestCommandConfigs());
        }
        if (sceneConfig.getBotAttackCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotAttackCommandConfigs());
        }
        if (sceneConfig.getBotKillOtherBotCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotKillOtherBotCommandConfigs());
        }
        if (sceneConfig.getBotKillHumanCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotKillHumanCommandConfigs());
        }
        if (sceneConfig.getBotRemoveOwnItemCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getBotRemoveOwnItemCommandConfigs());
        }
        if (sceneConfig.getKillBotCommandConfigs() != null) {
            gameEngineControl.executeBotCommands(sceneConfig.getKillBotCommandConfigs());
        }
        if (sceneConfig.getStartPointPlacerConfig() != null) {
            sceneConfig.getStartPointPlacerConfig().setBaseItemCount(1);
            sceneConfig.getStartPointPlacerConfig().setBaseItemTypeId(gameUiControl.getPlanetConfig().getStartBaseItemTypeId());
            baseItemPlacerService.activate(sceneConfig.getStartPointPlacerConfig(), decimalPositions -> {
                if (decimalPositions.size() != 1) {
                    throw new IllegalArgumentException("To create a new human base, only one base item is allowed. Given: " + decimalPositions.size());
                }
                gameEngineControl.createHumanBaseWithBaseItem(CollectionUtils.getFirst(decimalPositions));
            });
        }
        if (sceneConfig.getQuestConfig() != null) {
            gameEngineControl.activateQuest(sceneConfig.getQuestConfig());
            audioService.onQuestActivated();
            questVisualizer.showSideBar(sceneConfig.getQuestConfig(), null);
        }
        if (sceneConfig.isWait4LevelUpDialog() != null && sceneConfig.isWait4LevelUpDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setLevelUpDialogCallback(this::onComplete);
        }
        if (sceneConfig.isWait4QuestPassedDialog() != null && sceneConfig.isWait4QuestPassedDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setQuestPassedCallback(this::onComplete);
        }
        if (sceneConfig.isWaitForBaseLostDialog() != null && sceneConfig.isWaitForBaseLostDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setBaseLostCallback(this::onComplete);
        }
        if (sceneConfig.getQuestConfig() != null && sceneConfig.getQuestConfig().isHidePassedDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
        }
        if (sceneConfig.isWaitForBaseCreated() != null && sceneConfig.isWaitForBaseCreated()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            questVisualizer.showSideBar(new QuestDescriptionConfig().setTitle(I18nHelper.getConstants().placeStartItemTitle()).setDescription(I18nHelper.getConstants().placeStartItemDescription()).setHidePassedDialog(true), null);
        }
        if (sceneConfig.getDuration() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            simpleExecutorService.schedule(sceneConfig.getDuration(), this::onComplete, SimpleExecutorService.Type.UNSPECIFIED);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            scrollBouncePrevention = false;
            questVisualizer.showSideBar(sceneConfig.getScrollUiQuest(), null);
            audioService.onQuestActivated();
            viewService.addViewFieldListeners(this);
        }
        if (sceneConfig.getBoxItemPositions() != null) {
            gameEngineControl.dropBoxes(sceneConfig.getBoxItemPositions());
        }
        if (sceneConfig.getGameTipConfig() != null) {
            gameTipService.start(sceneConfig.getGameTipConfig());
        }
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            setupQuestVisualizer4Server();
        }
        if (!hasCompletionCallback) {
            gameUiControl.onSceneCompleted();
        }
    }

    private void setupViewFieldConfig(ViewFieldConfig viewFieldConfig) {
        if (viewFieldConfig == null) {
            return;
        }

        if (viewFieldConfig.getSpeed() != null && viewFieldConfig.getToPosition() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
        }
        terrainScrollHandler.executeViewFieldConfig(viewFieldConfig, Optional.of(this::onComplete));
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        if (!scrollBouncePrevention && viewField.isInside(sceneConfig.getScrollUiQuest().getScrollTargetRectangle())) {
            scrollBouncePrevention = true;
            onQuestPassed();
        }
    }

    private void onComplete() {
        if (completionCallbackCount == 0) {
            logger.severe("completionCallbackCount is already zero");
        }
        completionCallbackCount--;
        if (completionCallbackCount == 0) {
            gameUiControl.onSceneCompleted();
        }
    }

    public void onOwnBaseCreated() {
        if (sceneConfig.isWaitForBaseCreated() != null && sceneConfig.isWaitForBaseCreated()) {
            onComplete();
        }
    }

    public void cleanup() {
        if (sceneConfig.getIntroText() != null) {
            screenCover.hideStoryCover();
        }
        if (sceneConfig.getStartPointPlacerConfig() != null) {
            baseItemPlacerService.deactivate();
        }
        if (sceneConfig.getQuestConfig() != null) {
            questVisualizer.showSideBar(null, null);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questVisualizer.showSideBar(null, null);
        }
        if (sceneConfig.getGameTipConfig() != null) {
            gameTipService.stop();
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            viewService.removeViewFieldListeners(this);
            questVisualizer.showSideBar(null, null);
        }
        if (sceneConfig.isWaitForBaseCreated() != null && sceneConfig.isWaitForBaseCreated()) {
            questVisualizer.showSideBar(null, null);
        }
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            questVisualizer.showSideBar(null, null);
        }
    }

    void onQuestPassed() {
        if (sceneConfig.getQuestConfig() != null) {
            questVisualizer.showSideBar(null, null);
            if (sceneConfig.getQuestConfig().isHidePassedDialog()) {
                onComplete();
            } else {
                modalDialogManager.showQuestPassed(sceneConfig.getQuestConfig());
                if (sceneConfig.getGameTipConfig() != null) {
                    gameTipService.stop();
                }
            }
            userUiService.increaseXp(sceneConfig.getQuestConfig().getXp());
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questVisualizer.showSideBar(null, null);
            if (sceneConfig.getScrollUiQuest().isHidePassedDialog()) {
                onComplete();
            } else {
                modalDialogManager.showQuestPassed(sceneConfig.getScrollUiQuest());
                if (sceneConfig.getGameTipConfig() != null) {
                    gameTipService.stop();
                }
            }
            userUiService.increaseXp(sceneConfig.getScrollUiQuest().getXp());
        }
    }

    public SceneConfig getSceneConfig() {
        return sceneConfig;
    }

    private void setupQuestVisualizer4Server() {
        questVisualizer.showSideBar(gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getSlavePlanetConfig().getActiveQuest(),
                gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getSlavePlanetConfig().getQuestProgressInfo());
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        questVisualizer.onQuestProgress(questProgressInfo);
    }
}
