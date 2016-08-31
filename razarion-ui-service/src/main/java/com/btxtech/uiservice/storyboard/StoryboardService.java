package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.uiservice.StartPointUiService;
import com.btxtech.uiservice.VisualUiService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton
// Better name: something with game-control, client control
public class StoryboardService {
    @Inject
    private GameEngine gameEngine;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private Instance<Scene> sceneInstance;
    private StoryboardConfig storyboardConfig;
    private int nextSceneNumber;
    private Scene currentScene;
    private UserContext userContext;

    public void init(StoryboardConfig storyboardConfig) {
        this.storyboardConfig = storyboardConfig;
        gameEngine.initialise(storyboardConfig.getGameEngineConfig());
        visualUiService.initialise(storyboardConfig.getVisualConfig());
        this.userContext = storyboardConfig.getUserContext();
    }

    public void start() {
        gameEngine.start();
        nextSceneNumber = 0;
        runScene();
    }

    public UserContext getUserContext() {
        return userContext;
    }

    private void runScene() {
        if (currentScene != null) {
            currentScene.cleanup();
        }
        currentScene = sceneInstance.get();
        currentScene.init(storyboardConfig.getSceneConfigs().get(nextSceneNumber));
        currentScene.run();
    }

    void onSceneCompleted() {
        if (nextSceneNumber + 1 < storyboardConfig.getSceneConfigs().size()) {
            nextSceneNumber++;
            runScene();
        } else {
            if (currentScene != null) {
                currentScene.cleanup();
                currentScene = null;
            }
        }
    }
}
