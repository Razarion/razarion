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
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_GAME_UI_CONTEXT;

/**
 * Created by Beat
 * 05.07.2016.
 */

// Better name: something with game-control
public class Scene {
    public static final long FADE_DURATION = 2000; // Edit in razarion.css
    private Logger logger = Logger.getLogger(Scene.class.getName());

    private ScreenCover screenCover;

    private BabylonRendererService threeJsRendererService;

    private GameUiControl gameUiControl;

    private QuestCockpitService questCockpitService;

    private BaseItemPlacerService baseItemPlacerService;

    private ModalDialogManager modalDialogManager;

    private SimpleExecutorService simpleExecutorService;

    private AudioService audioService;

    private GameEngineControl gameEngineControl;

    private UserUiService userUiService;

    private InGameQuestVisualizationService inGameQuestVisualizationService;

    private ServerQuestProvider serverQuestProvider;
    private SceneConfig sceneConfig;
    private int completionCallbackCount;
    private boolean hasCompletionCallback;
    private boolean scrollBouncePrevention = true;

    @Inject
    public Scene(ServerQuestProvider serverQuestProvider, InGameQuestVisualizationService inGameQuestVisualizationService, UserUiService userUiService, GameEngineControl gameEngineControl, AudioService audioService, SimpleExecutorService simpleExecutorService, ModalDialogManager modalDialogManager, BaseItemPlacerService baseItemPlacerService, QuestCockpitService questCockpitService, GameUiControl gameUiControl, BabylonRendererService threeJsRendererService, ScreenCover screenCover) {
        this.serverQuestProvider = serverQuestProvider;
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.userUiService = userUiService;
        this.gameEngineControl = gameEngineControl;
        this.audioService = audioService;
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
        if (sceneConfig.isRemoveLoadingCover()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            screenCover.fadeOutLoadingCover();
            simpleExecutorService.schedule(FADE_DURATION, () -> {
                screenCover.removeLoadingCover();
                onComplete();
            }, SimpleExecutorService.Type.SCENE_RUNNER);
        }
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
            sceneConfig.getStartPointPlacerConfig().setBaseItemCount(1);
            AlarmRaiser.onNull(gameUiControl.getPlanetConfig().getStartBaseItemTypeId(),
                    INVALID_GAME_UI_CONTEXT,
                    "No Start base item type in planet",
                    gameUiControl.getPlanetConfig().getId());
            sceneConfig.getStartPointPlacerConfig().setBaseItemTypeId(gameUiControl.getPlanetConfig().getStartBaseItemTypeId());
            baseItemPlacerService.activate(sceneConfig.getStartPointPlacerConfig(), false, decimalPositions -> {
                if (decimalPositions.size() != 1) {
                    throw new IllegalArgumentException("To create a new human base, only one base item is allowed. Given: " + decimalPositions.size());
                }
                gameEngineControl.createHumanBaseWithBaseItem(CollectionUtils.getFirst(decimalPositions));
            });
        }
        if (sceneConfig.getQuestConfig() != null) {
            gameEngineControl.activateQuest(sceneConfig.getQuestConfig());
            audioService.onQuestActivated();
            questCockpitService.showQuestSideBar(sceneConfig.getQuestConfig(), false);
            inGameQuestVisualizationService.onQuestActivated(sceneConfig.getQuestConfig());
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
            questCockpitService.showQuestSideBar(new QuestDescriptionConfig().title(I18nHelper.getConstants().placeStartItemTitle()).description(I18nHelper.getConstants().placeStartItemDescription()).hidePassedDialog(true), false);
        }
        if (sceneConfig.getDuration() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            simpleExecutorService.schedule(sceneConfig.getDuration(), this::onComplete, SimpleExecutorService.Type.SCENE_WAIT);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            scrollBouncePrevention = false;
            questCockpitService.showQuestSideBar(sceneConfig.getScrollUiQuest(), false);
            audioService.onQuestActivated();
            // TODO viewService.addViewFieldListeners(this);
        }
        if (sceneConfig.getBoxItemPositions() != null) {
            gameEngineControl.dropBoxes(sceneConfig.getBoxItemPositions());
        }
        if (sceneConfig.getGameTipConfig() != null) {
            inGameQuestVisualizationService.setSuppressed(true);
            // TODO gameTipService.start(sceneConfig.getGameTipConfig());
        }
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            if(!gameUiControl.hasActiveServerQuest()) {
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
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.getGameTipConfig() != null) {
            inGameQuestVisualizationService.setSuppressed(false);
            // TODO gameTipService.stop();
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            // TODO viewService.removeViewFieldListeners(this);
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.isWaitForBaseCreated() != null && sceneConfig.isWaitForBaseCreated()) {
            questCockpitService.showQuestSideBar(null, false);
        }
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            questCockpitService.showQuestSideBar(null, false);
        }
        inGameQuestVisualizationService.stop();
    }

    void onQuestPassed() {
        if (sceneConfig.getQuestConfig() != null) {
            questCockpitService.showQuestSideBar(null, false);
            if (sceneConfig.getQuestConfig().isHidePassedDialog()) {
                onComplete();
            } else {
                modalDialogManager.showQuestPassed(sceneConfig.getQuestConfig());
                if (sceneConfig.getGameTipConfig() != null) {
                    inGameQuestVisualizationService.setSuppressed(false);
                    // TODO gameTipService.stop();
                }
            }
            userUiService.increaseXp(sceneConfig.getQuestConfig().getXp());
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questCockpitService.showQuestSideBar(null, false);
            if (sceneConfig.getScrollUiQuest().isHidePassedDialog()) {
                onComplete();
            } else {
                modalDialogManager.showQuestPassed(sceneConfig.getScrollUiQuest());
                if (sceneConfig.getGameTipConfig() != null) {
                    inGameQuestVisualizationService.setSuppressed(false);
                    // TODO gameTipService.stop();
                }
            }
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
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            questCockpitService.showQuestSideBar(quest, true);
            if (quest != null) {
                inGameQuestVisualizationService.onQuestActivated(quest);
            } else {
                inGameQuestVisualizationService.stop();
            }
        }
    }

    public void onQuestPassedServer(QuestConfig quest) {
        if (sceneConfig.isProcessServerQuests() != null && sceneConfig.isProcessServerQuests()) {
            questCockpitService.showQuestSideBar(null, true);
            modalDialogManager.showQuestPassed(quest);
            inGameQuestVisualizationService.stop();
        } else {
            logger.severe("Scene.onQuestPassedServer() but not sceneConfig.isProcessServerQuests()");
        }
    }
}
