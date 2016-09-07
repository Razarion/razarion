package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.btxtech.uiservice.cockpit.StoryCover;
import com.btxtech.uiservice.renderer.task.startpoint.StartPointUiService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.utils.CompletionListener;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
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
    private SceneConfig sceneConfig;
    private Collection<SceneCompletionHandler> completionHandlers = new ArrayList<>();

    public void init(SceneConfig sceneConfig) {
        this.sceneConfig = sceneConfig;
    }

    public void run() {
        if (sceneConfig.getIntroText() != null) {
            storyCover.show(sceneConfig.getIntroText());
        }
        if (sceneConfig.getBotConfigs() != null) {
            botService.startBots(sceneConfig.getBotConfigs());
        }
        if (sceneConfig.getStartPointConfig() != null) {
         // TODO   startPointUiService.activate(sceneConfig.getStartPointConfig());
        }
        questVisualizer.showSideBar(sceneConfig.isShowQuestSideBar());
        setupCameraConfig(sceneConfig.getCameraConfig());
    }

    private void setupCameraConfig(CameraConfig cameraConfig) {
        if (cameraConfig == null) {
            return;
        }

        CompletionListener completionListener = null;
        if (cameraConfig.isSmooth() && cameraConfig.getToPosition() != null) {
            completionListener = registerSceneCompletionListener();
        }
        terrainScrollHandler.executeCameraConfig(cameraConfig, completionListener);
    }

    private CompletionListener registerSceneCompletionListener() {
        SceneCompletionHandler completionHandler = new SceneCompletionHandler(this);
        completionHandlers.add(completionHandler);
        return completionHandler;
    }

    public void onComplete(SceneCompletionHandler sceneCompletionHandler) {
        if (completionHandlers.isEmpty()) {
            logger.severe("completionHandlers is already empty");
        }
        boolean removed = completionHandlers.remove(sceneCompletionHandler);
        if (!removed) {
            logger.severe("SceneCompletionHandler not removed");
        }
        if (completionHandlers.isEmpty()) {
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
    }
}
