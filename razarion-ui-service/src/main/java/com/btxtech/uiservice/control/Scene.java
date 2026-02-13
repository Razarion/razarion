package com.btxtech.uiservice.control;

import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.ServerQuestProvider;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.user.UserUiService;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_GAME_UI_CONTEXT;

/**
 * Created by Beat
 * 05.07.2016.
 */

// Better name: something with game-control
public class Scene {
    private final Logger logger = Logger.getLogger(Scene.class.getName());
    private final ScreenCover screenCover;
    private final BabylonRendererService threeJsRendererService;
    private final GameUiControl gameUiControl;
    private final QuestCockpitService questCockpitService;
    private final BaseItemPlacerService baseItemPlacerService;
    private final ModalDialogManager modalDialogManager;
    private final SimpleExecutorService simpleExecutorService;
    private final GameEngineControl gameEngineControl;
    private final UserUiService userUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private final ServerQuestProvider serverQuestProvider;
    private SceneConfig sceneConfig;
    private int completionCallbackCount;
    private boolean hasCompletionCallback;
    private boolean scrollBouncePrevention = true;

    @Inject
    public Scene(ServerQuestProvider serverQuestProvider, InGameQuestVisualizationService inGameQuestVisualizationService, UserUiService userUiService, GameEngineControl gameEngineControl, SimpleExecutorService simpleExecutorService, ModalDialogManager modalDialogManager, BaseItemPlacerService baseItemPlacerService, QuestCockpitService questCockpitService, GameUiControl gameUiControl, BabylonRendererService threeJsRendererService, ScreenCover screenCover) {
        this.serverQuestProvider = serverQuestProvider;
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.userUiService = userUiService;
        this.gameEngineControl = gameEngineControl;
        this.simpleExecutorService = simpleExecutorService;
        this.modalDialogManager = modalDialogManager;
        this.baseItemPlacerService = baseItemPlacerService;
        this.questCockpitService = questCockpitService;
        this.gameUiControl = gameUiControl;
        this.threeJsRendererService = threeJsRendererService;
        this.screenCover = screenCover;
    }

    public void init(SceneConfig sceneConfig) {
        this.sceneConfig = sceneConfig;
        completionCallbackCount = 0;
    }

    public void run() {
        setupViewFieldConfig(sceneConfig.getViewFieldConfig());
        if (sceneConfig.getIntroText() != null && !sceneConfig.getIntroText().trim().isEmpty()) {
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
            sceneConfig.getStartPointPlacerConfig().baseItemCount(1);
            AlarmRaiser.onNull(gameUiControl.getPlanetConfig().getStartBaseItemTypeId(),
                    INVALID_GAME_UI_CONTEXT,
                    "No Start base item type in planet",
                    gameUiControl.getPlanetConfig().getId());
            sceneConfig.getStartPointPlacerConfig().baseItemTypeId(gameUiControl.getPlanetConfig().getStartBaseItemTypeId());
            baseItemPlacerService.activate(sceneConfig.getStartPointPlacerConfig(), false, decimalPositions -> {
                if (decimalPositions.size() != 1) {
                    throw new IllegalArgumentException("To create a new human base, only one base item is allowed. Given: " + decimalPositions.size());
                }
                gameEngineControl.createHumanBaseWithBaseItem(CollectionUtils.getFirst(decimalPositions));
            });
        }
        if (sceneConfig.getQuestConfig() != null) {
            gameEngineControl.activateQuest(sceneConfig.getQuestConfig());
            questCockpitService.showQuestSideBar(sceneConfig.getQuestConfig(), false);
            inGameQuestVisualizationService.onQuestActivated(sceneConfig.getQuestConfig());
        }
        if (sceneConfig.getWait4LevelUpDialog() != null && sceneConfig.getWait4LevelUpDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setLevelUpDialogCallback(this::onComplete);
        }
        if (sceneConfig.getWait4QuestPassedDialog() != null && sceneConfig.getWait4QuestPassedDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setQuestPassedCallback(this::onComplete);
        }
        if (sceneConfig.getWaitForBaseLostDialog() != null && sceneConfig.getWaitForBaseLostDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            modalDialogManager.setBaseLostCallback(this::onComplete);
        }
        if (sceneConfig.getQuestConfig() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
        }
        if (sceneConfig.getWaitForBaseCreated() != null && sceneConfig.getWaitForBaseCreated()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            questCockpitService.showQuestSideBar(new QuestDescriptionConfig(), false);
        }
        if (sceneConfig.getDuration() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            simpleExecutorService.schedule(sceneConfig.getDuration(), this::onComplete, SimpleExecutorService.Type.SCENE_WAIT);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            scrollBouncePrevention = false;
            questCockpitService.showQuestSideBar(sceneConfig.getScrollUiQuest(), false);
            // TODO viewService.addViewFieldListeners(this);
        }
        if (sceneConfig.getBoxItemPositions() != null) {
            gameEngineControl.dropBoxes(sceneConfig.getBoxItemPositions());
        }
        if (sceneConfig.getProcessServerQuests() != null && sceneConfig.getProcessServerQuests()) {
            if (!gameUiControl.hasActiveServerQuest()) {
                serverQuestProvider.activateNextPossibleQuest();
            }
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
        threeJsRendererService.executeViewFieldConfig(viewFieldConfig, Optional.of(this::onComplete));
    }

    // TODO @Override
    // TODO public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
    // TODO     if (!scrollBouncePrevention && viewField.isInside(sceneConfig.getScrollUiQuest().getScrollTargetRectangle())) {
    // TODO         scrollBouncePrevention = true;
    // TODO         onQuestPassed();
    // TODO     }
    // TODO }

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
        if (sceneConfig.getWaitForBaseCreated() != null && sceneConfig.getWaitForBaseCreated()) {
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
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            // TODO viewService.removeViewFieldListeners(this);
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getWaitForBaseCreated() != null && sceneConfig.getWaitForBaseCreated()) {
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getProcessServerQuests() != null && sceneConfig.getProcessServerQuests()) {
            questCockpitService.showQuestSideBar(null, false);
        }
        inGameQuestVisualizationService.stop();
    }

    void onQuestPassed() {
        if (sceneConfig.getQuestConfig() != null) {
            questCockpitService.showQuestSideBar(null, false);
            onComplete();
            userUiService.increaseXp(sceneConfig.getQuestConfig().getXp());
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questCockpitService.showQuestSideBar(null, false);
            onComplete();
            userUiService.increaseXp(sceneConfig.getScrollUiQuest().getXp());
        }
        inGameQuestVisualizationService.stop();
    }

    public SceneConfig getSceneConfig() {
        return sceneConfig;
    }

    private void setupQuestVisualizer4Server() {
        questCockpitService.showQuestSideBar(gameUiControl.getServerQuest(), true);
        inGameQuestVisualizationService.onQuestActivated(gameUiControl.getServerQuest());
        if (gameUiControl.getServerQuestProgress() != null) {
            questCockpitService.onQuestProgress(gameUiControl.getServerQuestProgress());
            inGameQuestVisualizationService.onQuestProgress(gameUiControl.getServerQuestProgress());
        }
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        questCockpitService.onQuestProgress(questProgressInfo);
        inGameQuestVisualizationService.onQuestProgress(questProgressInfo);
    }

    public void onQuestActivatedServer(QuestConfig quest) {
        if (sceneConfig.getProcessServerQuests() != null && sceneConfig.getProcessServerQuests()) {
            questCockpitService.showQuestSideBar(quest, true);
            if (quest != null) {
                inGameQuestVisualizationService.onQuestActivated(quest);
            } else {
                inGameQuestVisualizationService.stop();
            }
        }
    }

    public void onQuestPassedServer(QuestConfig quest) {
        if (sceneConfig.getProcessServerQuests() != null && sceneConfig.getProcessServerQuests()) {
            questCockpitService.showQuestSideBar(null, true);
            modalDialogManager.showQuestPassed(quest);
            inGameQuestVisualizationService.stop();
        } else {
            logger.severe("Scene.onQuestPassedServer() but not sceneConfig.isProcessServerQuests()");
        }
    }
}
