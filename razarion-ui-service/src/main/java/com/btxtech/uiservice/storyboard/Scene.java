package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.btxtech.uiservice.cockpit.StoryCover;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

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
public class Scene {
    private Logger logger = Logger.getLogger(Scene.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private StoryCover storyCover;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private StoryboardService storyboardService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private QuestVisualizer questVisualizer;
    @Inject
    private BotService botService;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private ActivityService activityService;
    @Inject
    private QuestService questService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AbstractModalDialogManager abstractModalDialogManager;
    @Inject
    private LevelService levelService;
    @Inject
    private ResourceService resourceService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private BoxService boxService;
    @Inject
    private BaseItemService baseItemService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ItemTypeService itemTypeService;
    private UserContext userContext;
    private SceneConfig sceneConfig;
    private int completionCallbackCount;
    private boolean hasCompletionCallback;

    public void init(UserContext userContext, SceneConfig sceneConfig) {
        this.userContext = userContext;
        this.sceneConfig = sceneConfig;
        completionCallbackCount = 0;
    }

    public void run() {
        if (sceneConfig.getIntroText() != null) {
            storyCover.show(sceneConfig.getIntroText());
        }
        if (sceneConfig.getBotConfigs() != null) {
            botService.startBots(sceneConfig.getBotConfigs());
        }
        if (sceneConfig.getResourceItemTypePositions() != null) {
            resourceService.createResources(sceneConfig.getResourceItemTypePositions());
        }
        if (sceneConfig.getBotMoveCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotMoveCommandConfigs());
        }
        if (sceneConfig.getBotHarvestCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotHarvestCommandConfigs());
        }
        if (sceneConfig.getBotAttackCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotAttackCommandConfigs());
        }
        if (sceneConfig.getBotKillOtherBotCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotKillOtherBotCommandConfigs());
        }
        if (sceneConfig.getBotKillHumanCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotKillHumanCommandConfigs());
        }
        if (sceneConfig.getStartPointPlacerConfig() != null) {
            baseItemPlacerService.activate(sceneConfig.getStartPointPlacerConfig(), decimalPositions -> {
                PlayerBase playerBase = baseItemService.createHumanBase(storyboardService.getUserContext());
                try {
                    for (DecimalPosition position : decimalPositions) {
                        baseItemService.spawnSyncBaseItem(itemTypeService.getBaseItemType(sceneConfig.getStartPointPlacerConfig().getBaseItemTypeId()), position, playerBase, false);
                    }
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }
            });
        }
        if (sceneConfig.getQuestConfig() != null) {
            questService.activateCondition(userContext, sceneConfig.getQuestConfig());
            questVisualizer.showSideBar(sceneConfig.getQuestConfig());
        } else {
            questVisualizer.showSideBar(null);
        }
        setupCameraConfig(sceneConfig.getCameraConfig());
        if (sceneConfig.isWait4LevelUpDialog() != null && sceneConfig.isWait4LevelUpDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            abstractModalDialogManager.setLevelUpDialogCallback(this::onComplete);
        }
        if (sceneConfig.isWait4QuestPassedDialog() != null && sceneConfig.isWait4QuestPassedDialog()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            abstractModalDialogManager.setQuestPassedCallback(this::onComplete);
        }
        if (sceneConfig.getDuration() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            simpleExecutorService.schedule(sceneConfig.getDuration(), this::onComplete, SimpleExecutorService.Type.UNSPECIFIED);
        }
        if (sceneConfig.getScrollUiQuest() != null) {
            questVisualizer.showSideBar(sceneConfig.getScrollUiQuest());
            terrainScrollHandler.setPositionListener(sceneConfig.getScrollUiQuest().getScrollTargetRectangle(), () -> {
                abstractModalDialogManager.showQuestPassed(sceneConfig.getScrollUiQuest());
                levelService.increaseXp(userContext, sceneConfig.getScrollUiQuest().getXp());
            });
        }
        if (sceneConfig.getBoxItemPositions() != null) {
            boxService.dropBoxes(sceneConfig.getBoxItemPositions());
        }

        if (!hasCompletionCallback) {
            storyboardService.onSceneCompleted();
        }
    }

    private void setupCameraConfig(CameraConfig cameraConfig) {
        if (cameraConfig == null) {
            return;
        }

        if (cameraConfig.getSpeed() != null && cameraConfig.getToPosition() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
        }
        terrainScrollHandler.executeCameraConfig(cameraConfig, Optional.of(this::onComplete));
    }

    public void onComplete() {
        if (completionCallbackCount == 0) {
            logger.severe("completionCallbackCount is already zero");
        }
        completionCallbackCount--;
        if (completionCallbackCount == 0) {
            storyboardService.onSceneCompleted();
        }
    }

    public void cleanup() {
        if (sceneConfig.getIntroText() != null) {
            storyCover.hide();
        }
        if (sceneConfig.getStartPointPlacerConfig() != null) {
            baseItemPlacerService.deactivate();
        }
    }
}
