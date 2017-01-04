package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitService;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @ApplicationScoped lead to crashes with errai CDI
// Better name: something with game-control, client control (See: GameLogicService) -> GameControl
public class GameUiControl implements QuestListener {
    // private Logger logger = Logger.getLogger(GameUiControl.class.getName());
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
    private BaseItemService baseItemService;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private QuestService questService;
    @Inject
    private Event<GameUiControlInitEvent> gameUiControlInitEvent;
    private GameUiControlConfig gameUiControlConfig;
    private int nextSceneNumber;
    private Scene currentScene;
    private UserContext userContext;

    public void init(GameUiControlConfig gameUiControlConfig) {
        this.gameUiControlConfig = gameUiControlConfig;
        itemTypeService.init(gameUiControlConfig.getGameEngineConfig());
        terrainTypeService.init(gameUiControlConfig.getGameEngineConfig());
        gameUiControlInitEvent.fire(new GameUiControlInitEvent(gameUiControlConfig));
        this.userContext = gameUiControlConfig.getUserContext();
        cockpitService.init();
        questService.addQuestListener(this);
    }

    public void start() {
        gameEngineControl.start();
        nextSceneNumber = 0;
        runScene();
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public boolean isMyOwnProperty(SyncBaseItem syncBaseItem) {
        return syncBaseItem.getBase().getUserContext() != null && syncBaseItem.getBase().getUserContext().equals(userContext);
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem) {
        return Character.HUMAN.isEnemy(syncBaseItem.getBase().getCharacter());
    }

    private void runScene() {
        if (currentScene != null) {
            currentScene.cleanup();
        }
        currentScene = sceneInstance.get();
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

    public PlayerBase getMyBase() {
        return baseItemService.getPlayerBase(userContext);
    }

    public int getMyLimitation4ItemType(Integer itemTypeId) {
        return baseItemService.getLimitation4ItemType(userContext, itemTypeId);
    }

    public int getItemCount(int itemTypeId) {
        return baseItemService.getItemCount(userContext, itemTypeId);
    }

    public int getLimitation4ItemType(BaseItemType itemType) {
        return baseItemService.getLimitation4ItemType(userContext, itemType);
    }

    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType itemType, int itemCount2Add) {
        return baseItemService.isLevelLimitation4ItemTypeExceeded(itemType, itemCount2Add, userContext);
    }

    public boolean isHouseSpaceExceeded(BaseItemType itemType, int itemCount2Add) {
        return baseItemService.isHouseSpaceExceeded(userContext, itemType, itemCount2Add);
    }

    public int getResources() {
        return baseItemService.getResources(userContext);
    }

    public GameUiControlConfig getGameUiControlConfig() {
        return gameUiControlConfig;
    }

    @Override
    public void onQuestPassed(UserContext examinee, QuestConfig questConfig) {
        if (currentScene != null) {
            currentScene.onQuestPassed();
        }
    }
}
