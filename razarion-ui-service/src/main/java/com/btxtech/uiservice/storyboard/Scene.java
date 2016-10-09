package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.condition.ConditionService;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.btxtech.uiservice.cockpit.StoryCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.renderer.task.startpoint.StartPointUiService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
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
    private StartPointUiService startPointUiService;
    @Inject
    private ActivityService activityService;
    @Inject
    private ConditionService conditionService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private LevelService levelService;
    @Inject
    private ResourceService resourceService;
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
            activityService.addSpanFinishedCallback(syncBaseItem -> {
                onComplete();
                return true;
            });
            hasCompletionCallback = true;
            completionCallbackCount++;
            botService.startBots(sceneConfig.getBotConfigs());
        }
        if (sceneConfig.getBotMoveCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotMoveCommandConfigs());
        }
        if (sceneConfig.getBotHarvestCommandConfigs() != null) {
            botService.executeCommands(sceneConfig.getBotHarvestCommandConfigs());
        }
        if (sceneConfig.getStartPointConfig() != null) {
            startPointUiService.activate(sceneConfig.getStartPointConfig());
        }
        if (sceneConfig.getQuestConfig() != null) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            conditionService.activateCondition(userContext, sceneConfig.getQuestConfig().getConditionConfig(), userContext1 -> {
                modalDialogManager.showQuestPassed(sceneConfig.getQuestConfig(), ignore -> {
                    levelService.increaseXp(userContext, sceneConfig.getQuestConfig().getXp());
                    onComplete();
                });
            });
            questVisualizer.showSideBar(sceneConfig.getQuestConfig());
        } else {
            questVisualizer.showSideBar(null);
        }
        setupCameraConfig(sceneConfig.getCameraConfig());
        if (sceneConfig.isWait4LevelUp() != null && sceneConfig.isWait4LevelUp()) {
            hasCompletionCallback = true;
            completionCallbackCount++;
            levelService.setLevelUpCallback(userContext1 -> modalDialogManager.showLevelUp(userContext1, ignore -> onComplete()));
        }
        if(sceneConfig.getResourceItemTypePositions() != null) {
            resourceService.createResources(sceneConfig.getResourceItemTypePositions());
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
        if (sceneConfig.getStartPointConfig() != null) {
            startPointUiService.deactivate();
        }
        if (sceneConfig.isWait4LevelUp() != null && sceneConfig.isWait4LevelUp()) {
            levelService.setLevelUpCallback(null);
        }
    }
}
