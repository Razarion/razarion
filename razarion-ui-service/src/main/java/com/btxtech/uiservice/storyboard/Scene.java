package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.uiservice.DisplayService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

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
public class Scene {
    private Logger logger = Logger.getLogger(Scene.class.getName());
    @Inject
    private DisplayService displayService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private Storyboard storyboard;
    private SceneConfig sceneConfig;
    private Collection<SceneCompletionHandler> completionHandlers = new ArrayList<>();

    public void init(SceneConfig sceneConfig) {
        this.sceneConfig = sceneConfig;
    }

    public void run() {
        displayService.setIntroText(sceneConfig.getIntroText());
        setupCameraConfig(sceneConfig.getCameraConfig());
    }

    private void setupCameraConfig(CameraConfig cameraConfig) {
        if (cameraConfig == null) {
            return;
        }

        SceneCompletionHandler completionHandler = null;
        if (cameraConfig.isSmooth() && cameraConfig.getToPosition() != null) {
            completionHandler = registerSceneCompletionHandler();
        }
        terrainScrollHandler.executeCameraConfig(cameraConfig, completionHandler);
    }

    private SceneCompletionHandler registerSceneCompletionHandler() {
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
            storyboard.onSceneCompleted();
        }
    }
}
