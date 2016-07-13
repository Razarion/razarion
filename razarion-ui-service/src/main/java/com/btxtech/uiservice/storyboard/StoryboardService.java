package com.btxtech.uiservice.storyboard;

import com.btxtech.uiservice.Planet;
import com.btxtech.shared.dto.StoryboardConfig;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton
public class StoryboardService {
    @Inject
    private Planet planet;
    @Inject
    private Instance<Scene> sceneInstance;
    private StoryboardConfig storyboardConfig;
    private int nextSceneNumber;
    private Scene currentScene;

    public void init(StoryboardConfig storyboardConfig) {
        this.storyboardConfig = storyboardConfig;
        planet.init(storyboardConfig.getPlanetConfig());
    }

    public void setup() {
        planet.setup();
    }

    public void start() {
        nextSceneNumber = 0;
        runScene();
    }

    private void runScene() {
        if(currentScene != null) {
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
            if(currentScene != null) {
                currentScene.cleanup();
                currentScene = null;
            }
        }
    }
}
