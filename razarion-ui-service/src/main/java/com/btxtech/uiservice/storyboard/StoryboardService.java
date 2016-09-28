package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.VisualUiService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @ApplicationScoped lead to crashes with errai
// Better name: something with game-control, client control
public class StoryboardService {
    // private Logger logger = Logger.getLogger(StoryboardService.class.getName());
    @Inject
    private GameEngine gameEngine;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private Instance<Scene> sceneInstance;
    @Inject
    private SyncItemContainerService syncItemContainerService;
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

    public boolean isMyOwnProperty(SyncBaseItem syncBaseItem) {
        return syncBaseItem.getBase().getUserContext() != null && syncBaseItem.getBase().getUserContext().equals(userContext);

    }

    private void runScene() {
        if (currentScene != null) {
            currentScene.cleanup();
        }
        currentScene = sceneInstance.get();
        currentScene.init(userContext, storyboardConfig.getSceneConfigs().get(nextSceneNumber));
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

    public Collection<SyncBaseItem> getMyItemsInRegion(Rectangle2D rectangle) {
        Collection<SyncBaseItem> result = new ArrayList<>();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (isMyOwnProperty(syncBaseItem)) {
                return null;
            }
            if (syncBaseItem.getSyncPhysicalArea().overlap(rectangle)) {
                result.add(syncBaseItem);
            }
            return null;
        });
        return result;
    }
}
