package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @ApplicationScoped lead to crashes with errai CDI
public class GameUiControl { // Equivalent worker class is PlanetService
    private Logger logger = Logger.getLogger(GameUiControl.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private VisualUiService visualUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AudioService audioService;
    @Inject
    private Instance<Scene> sceneInstance;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private LevelService levelService;
    @Inject
    private InventoryService inventoryService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AbstractModalDialogManager dialogManager;
    @Inject
    private Event<GameUiControlInitEvent> gameUiControlInitEvent;
    private GameUiControlConfig gameUiControlConfig;
    private int nextSceneNumber;
    private Scene currentScene;
    private UserContext userContext;

    public void setGameUiControlConfig(GameUiControlConfig gameUiControlConfig) {
        this.gameUiControlConfig = gameUiControlConfig;
    }

    public void init() {
        itemTypeService.init(gameUiControlConfig.getGameEngineConfig());
        terrainTypeService.init(gameUiControlConfig.getGameEngineConfig());
        levelService.init(gameUiControlConfig.getGameEngineConfig());
        inventoryService.init(gameUiControlConfig.getGameEngineConfig());
        userContext = gameUiControlConfig.getUserContext();
        gameEngineControl.init(gameUiControlConfig.getGameEngineConfig(), userContext);
        gameUiControlInitEvent.fire(new GameUiControlInitEvent(gameUiControlConfig));
    }

    public void start() {
        cockpitService.show();
        gameEngineControl.start();
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
        logger.warning("Run Scene: " + currentScene);
        currentScene.init(userContext, gameUiControlConfig.getSceneConfigs().get(nextSceneNumber));
        currentScene.run();
    }

    void onSceneCompleted() {
        if (nextSceneNumber + 1 < gameUiControlConfig.getSceneConfigs().size()) {
            nextSceneNumber++;
            runScene();
        } else {
            if (currentScene != null) {
                currentScene.cleanup();
                currentScene = null;
            }
        }
    }

    public GameUiControlConfig getGameUiControlConfig() {
        return gameUiControlConfig;
    }

    public PlanetConfig getPlanetConfig() {
        return gameUiControlConfig.getGameEngineConfig().getPlanetConfig();
    }

    public void onQuestPassed() {
        if (currentScene != null) {
            currentScene.onQuestPassed();
        }
    }

    public void setGameInfo(GameInfo gameInfo) {
        baseItemUiService.updateGameInfo(gameInfo);
        if (gameInfo.getXpFromKills() > 0) {
            increaseXp(gameInfo.getXpFromKills());
        }
    }

    public void increaseXp(int deltaXp) {
        int xp = userContext.getXp() + deltaXp;
        LevelConfig levelConfig = levelService.getLevel(userContext.getLevelId());
        if (xp >= levelConfig.getXp2LevelUp()) {
            LevelConfig newLevelConfig = levelService.getNextLevel(levelConfig);
            userContext.setLevelId(newLevelConfig.getLevelId());
            userContext.setXp(0);
            gameEngineControl.updateLevel(newLevelConfig.getLevelId());
            cockpitService.updateLevelAndXp(userContext);
            itemCockpitService.onStateChanged();
            dialogManager.onLevelPassed(userContext, levelConfig, newLevelConfig);
        } else {
            userContext.setXp(xp);
            cockpitService.updateLevelAndXp(userContext);
        }
    }

    public void onOnBoxPicked(BoxContent boxContent) {
        for (InventoryItem inventoryItem : boxContent.getInventoryItems()) {
            userContext.addInventoryItem(inventoryItem.getId());
        }
        dialogManager.showBoxPicked(boxContent);
    }

    public int getMyLimitation4ItemType(int itemTypeId) {
        int levelCount = levelService.getLevel(userContext.getLevelId()).limitation4ItemType(itemTypeId);
        int planetCount = getPlanetConfig().imitation4ItemType(itemTypeId);
        return Math.min(levelCount, planetCount);
    }
}
