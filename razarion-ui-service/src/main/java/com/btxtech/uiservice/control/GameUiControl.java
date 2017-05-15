package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameConfig;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @ApplicationScoped lead to crashes with errai CDI
public class GameUiControl { // Equivalent worker class is PlanetService
    private Logger logger = Logger.getLogger(GameUiControl.class.getName());
    @Inject
    private Instance<Scene> sceneInstance;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private LevelService levelService;
    @Inject
    private InventoryService inventoryService;
    @Inject
    private UserUiService userUiService;
    @Inject
    private ClientRunner clientRunner;
    @Inject
    private TrackerService trackerService;
    @Inject
    private Event<GameUiControlInitEvent> gameUiControlInitEvent;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private ScreenCover screenCover;
    @Inject
    private Instance<AbstractServerSystemConnection> serverSystemConnectionInstance;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    private GameUiControlConfig gameUiControlConfig;
    private int nextSceneNumber;
    private Scene currentScene;
    private Date startTimeStamp;
    private Date sceneStartTimeStamp;
    private List<SceneConfig> scenes;
    private AbstractServerSystemConnection abstractServerSystemConnection;

    public void setGameUiControlConfig(GameUiControlConfig gameUiControlConfig) {
        this.gameUiControlConfig = gameUiControlConfig;
        userUiService.setUserContext(gameUiControlConfig.getUserContext());
    }

    public void onWarmGameConfigLoaded(WarmGameConfig warmGameConfig) {
        this.gameUiControlConfig.setPlanetConfig(warmGameConfig.getPlanetConfig());
        this.gameUiControlConfig.setSlaveSyncItemInfo(warmGameConfig.getSlaveSyncItemInfo());
    }

    public void init() {
        abstractServerSystemConnection = serverSystemConnectionInstance.get();
        abstractServerSystemConnection.init();
        itemTypeService.init(gameUiControlConfig.getStaticGameConfig());
        terrainTypeService.init(gameUiControlConfig.getStaticGameConfig());
        levelService.init(gameUiControlConfig.getStaticGameConfig());
        inventoryService.init(gameUiControlConfig.getStaticGameConfig());
        gameUiControlInitEvent.fire(new GameUiControlInitEvent(gameUiControlConfig));
        terrainScrollHandler.setPlayGround(gameUiControlConfig.getPlanetConfig().getPlayGround());
    }

    public void initWarm() {
        terrainScrollHandler.setPlayGround(gameUiControlConfig.getPlanetConfig().getPlayGround());
    }

    public void start() {
        startTimeStamp = new Date();
        cockpitService.show(userUiService.getUserContext());
        nextSceneNumber = 0;
        if (this.gameUiControlConfig.getSlaveSyncItemInfo() != null) {
            scenes = setupSlaveScenes();
        } else {
            scenes = gameUiControlConfig.getSceneConfigs();
        }
        runScene();
    }

    private void runScene() {
        if (currentScene != null) {
            sceneFinished();
            currentScene.cleanup();
        }
        currentScene = sceneInstance.get();
        SceneConfig sceneConfig = scenes.get(nextSceneNumber);
        currentScene.init(sceneConfig);
        sceneStartTimeStamp = new Date();
        currentScene.run();
    }

    void onSceneCompleted() {
        if (nextSceneNumber + 1 < scenes.size()) {
            nextSceneNumber++;
            runScene();
        } else {
            if (currentScene != null) {
                sceneFinished();
                currentScene.cleanup();
                currentScene = null;
            }
            finished();
        }
    }

    private void sceneFinished() {
        if (sceneStartTimeStamp == null) {
            logger.warning("sceneStartTimeStamp == null");
            return;
        }
        trackerService.trackScene(sceneStartTimeStamp, currentScene.getSceneConfig().getInternalName());
        sceneStartTimeStamp = null;
    }

    public void finished() {
        if (startTimeStamp == null) {
            logger.warning("startTimeStamp == null");
        } else {
            trackerService.trackGameUiControl(startTimeStamp);
            startTimeStamp = null;
        }
        if (gameUiControlConfig.getSlaveSyncItemInfo() == null) {
            // Temporary fix for showing move to first multiplayer planet
            modalDialogManager.showLeaveStartTutorial(() -> {
                screenCover.fadeInLoadingCover();
                clientRunner.startWarm();
            });
        }
    }

    public GameUiControlConfig getGameUiControlConfig() {
        return gameUiControlConfig;
    }

    public PlanetConfig getPlanetConfig() {
        return gameUiControlConfig.getPlanetConfig();
    }

    public void onQuestPassed() {
        if (currentScene != null) {
            currentScene.onQuestPassed();
        }
    }

    public void setGameInfo(GameInfo gameInfo) {
        baseItemUiService.updateGameInfo(gameInfo);
        if (gameInfo.getXpFromKills() > 0) {
            userUiService.increaseXp(gameInfo.getXpFromKills());
        }
    }

    public int getMyLimitation4ItemType(int itemTypeId) {
        int levelCount = levelService.getLevel(userUiService.getUserContext().getLevelId()).limitation4ItemType(itemTypeId);
        int planetCount = getPlanetConfig().imitation4ItemType(itemTypeId);
        return Math.min(levelCount, planetCount);
    }

    private List<SceneConfig> setupSlaveScenes() {
        if (gameUiControlConfig.getSlaveSyncItemInfo().getActualBaseId() != null) {
            return setupSlaveExistingScenes();
        } else {
            return setupSlaveSpawnScenes();
        }
    }

    private List<SceneConfig> setupSlaveExistingScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        DecimalPosition factoryPosition = null;
        DecimalPosition builderPosition = null;
        DecimalPosition unitPosition = null;
        for (SyncBaseItemInfo syncBaseItemInfo : gameUiControlConfig.getSlaveSyncItemInfo().getSyncBaseItemInfos()) {
            if (syncBaseItemInfo.getBaseId() == gameUiControlConfig.getSlaveSyncItemInfo().getActualBaseId()) {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
                if (baseItemType.getFactoryType() != null) {
                    factoryPosition = syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition();
                    break;
                }
                if (baseItemType.getBuilderType() != null) {
                    builderPosition = syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition();
                }
                if (builderPosition != null) {
                    continue;
                }
                unitPosition = syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition();
            }
        }
        DecimalPosition position = unitPosition;
        if (builderPosition != null) {
            position = builderPosition;
        }
        if (factoryPosition != null) {
            position = factoryPosition;
        }

        sceneConfigs.add(new SceneConfig().setViewFieldConfig(new ViewFieldConfig().setToPosition(position)));
        sceneConfigs.add(new SceneConfig().setInternalName("script: fade out").setRemoveLoadingCover(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupSlaveSpawnScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        DecimalPosition position = gameUiControlConfig.getSlavePlanetConfig().getStartRegion().toAabb().center();
        // Set camera Position
        sceneConfigs.add(new SceneConfig().setViewFieldConfig(new ViewFieldConfig().setToPosition(position)));
        // Fade out
        sceneConfigs.add(new SceneConfig().setInternalName("script: fade out").setRemoveLoadingCover(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(position);
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(getPlanetConfig().getStartBaseItemTypeId(), 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setInternalName("Planet 1 Spawn").setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(startConditionConfig).setXp(0)));

        return sceneConfigs;
    }

    public Set<Integer> getAllTextureIds() {
        Set<Integer> textureIds = Shape3DUtils.getAllTextures(gameUiControlConfig.getShape3Ds());
        for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
            if(baseItemType.getBuildupTextureId() != null) {
                textureIds.add(baseItemType.getBuildupTextureId());
            }
            if(baseItemType.getBaseItemDemolitionImageId() != null) {
                textureIds.add(baseItemType.getBaseItemDemolitionImageId());
            }
        }

        for (SlopeSkeletonConfig slopeSkeletonConfig : gameUiControlConfig.getStaticGameConfig().getSlopeSkeletonConfigs()) {
            if (slopeSkeletonConfig.getTextureId() != null) {
                textureIds.add(slopeSkeletonConfig.getTextureId());
            }
        }

        GroundSkeletonConfig groundSkeletonConfig = gameUiControlConfig.getStaticGameConfig().getGroundSkeletonConfig();
        if (groundSkeletonConfig.getTopTextureId() != null) {
            textureIds.add(groundSkeletonConfig.getTopTextureId());
        }
        if (groundSkeletonConfig.getBottomTextureId() != null) {
            textureIds.add(groundSkeletonConfig.getBottomTextureId());
        }
        return textureIds;
    }

    public Set<Integer> getAllBumpTextureIds() {
        Set<Integer> bumpIds = new HashSet<>();
        for (SlopeSkeletonConfig slopeSkeletonConfig : gameUiControlConfig.getStaticGameConfig().getSlopeSkeletonConfigs()) {
            if (slopeSkeletonConfig.getBmId() != null) {
                bumpIds.add(slopeSkeletonConfig.getBmId());
            }
        }

        GroundSkeletonConfig groundSkeletonConfig = gameUiControlConfig.getStaticGameConfig().getGroundSkeletonConfig();
        if (groundSkeletonConfig.getTopBmId() != null) {
            bumpIds.add(groundSkeletonConfig.getTopBmId());
        }
        if (groundSkeletonConfig.getBottomBmId() != null) {
            bumpIds.add(groundSkeletonConfig.getBottomBmId());
        }
        if (gameUiControlConfig.getStaticGameConfig().getWaterConfig().getBmId() != null) {
            bumpIds.add(gameUiControlConfig.getStaticGameConfig().getWaterConfig().getBmId());
        }
        return bumpIds;
    }

    public void onLevelUpdate(LevelConfig newLevelConfig) {
        abstractServerSystemConnection.onLevelChanged(newLevelConfig);
    }
}
