package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.ColdGameUiControlConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.utils.GeometricUtil;
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
import java.util.HashSet;
import java.util.List;
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
    @Inject
    private PlaybackControl playbackControl;
    private ColdGameUiControlConfig coldGameUiControlConfig;
    private int nextSceneNumber;
    private Scene currentScene;
    private Date startTimeStamp;
    private Date sceneStartTimeStamp;
    private List<SceneConfig> scenes;
    private AbstractServerSystemConnection abstractServerSystemConnection;
    private GameEngineMode gameEngineMode;
    private int consuming;
    private int generating;

    public void setColdGameUiControlConfig(ColdGameUiControlConfig coldGameUiControlConfig) {
        this.coldGameUiControlConfig = coldGameUiControlConfig;
        gameEngineMode = coldGameUiControlConfig.getWarmGameUiControlConfig().getGameEngineMode();
        userUiService.setUserContext(coldGameUiControlConfig.getUserContext());
    }

    public void onWarmGameConfigLoaded(WarmGameUiControlConfig warmGameUiControlConfig) {
        this.coldGameUiControlConfig.setWarmGameUiControlConfig(warmGameUiControlConfig);
        gameEngineMode = warmGameUiControlConfig.getGameEngineMode();
    }

    public void init() {
        abstractServerSystemConnection = serverSystemConnectionInstance.get();
        abstractServerSystemConnection.init();
        itemTypeService.init(coldGameUiControlConfig.getStaticGameConfig());
        terrainTypeService.init(coldGameUiControlConfig.getStaticGameConfig());
        levelService.init(coldGameUiControlConfig.getStaticGameConfig());
        inventoryService.init(coldGameUiControlConfig.getStaticGameConfig());
        gameUiControlInitEvent.fire(new GameUiControlInitEvent(coldGameUiControlConfig));
        terrainScrollHandler.setPlayGround(coldGameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlayGround());
    }

    public void initWarm() {
        gameEngineMode = coldGameUiControlConfig.getWarmGameUiControlConfig().getGameEngineMode();
        terrainScrollHandler.setPlayGround(coldGameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig().getPlayGround());
    }

    public void start() {
        startTimeStamp = new Date();
        cockpitService.show(userUiService.getUserContext());
        nextSceneNumber = 0;
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiControlConfig.getWarmGameUiControlConfig().isDetailedTracking()) {
                trackerService.startDetailedTracking(getPlanetConfig().getPlanetId());
            }
            scenes = coldGameUiControlConfig.getWarmGameUiControlConfig().getSceneConfigs();
        } else if (gameEngineMode == GameEngineMode.SLAVE) {
            scenes = setupSlaveScenes();
        } else if (gameEngineMode == GameEngineMode.PLAYBACK) {
            scenes = setupPlaybackScenes();
            playbackControl.start(coldGameUiControlConfig.getWarmGameUiControlConfig().getPlaybackGameUiControlConfig());
        } else {
            throw new IllegalArgumentException("Unknown GameEngineMode: " + coldGameUiControlConfig.getWarmGameUiControlConfig().getGameEngineMode());
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
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiControlConfig.getWarmGameUiControlConfig().isDetailedTracking()) {
                trackerService.stopDetailedTracking();
            }
            // TODO Temporary fix for showing move to first multiplayer planet. Pervents loading new planet if multiplayer planet is done. Because there is no new planet
            modalDialogManager.showLeaveStartTutorial(() -> {
                screenCover.fadeInLoadingCover();
                clientRunner.startWarm();
            });
        }
    }

    public ColdGameUiControlConfig getColdGameUiControlConfig() {
        return coldGameUiControlConfig;
    }

    public PlanetConfig getPlanetConfig() {
        return coldGameUiControlConfig.getWarmGameUiControlConfig().getPlanetConfig();
    }

    public void onQuestPassed() {
        if (currentScene != null) {
            currentScene.onQuestPassed();
        }
    }

    public void onOwnBaseCreated() {
        if (currentScene != null) {
            currentScene.onOwnBaseCreated();
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
        if (coldGameUiControlConfig.getWarmGameUiControlConfig().getSlaveSyncItemInfo().getActualBaseId() != null) {
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
        for (SyncBaseItemInfo syncBaseItemInfo : coldGameUiControlConfig.getWarmGameUiControlConfig().getSlaveSyncItemInfo().getSyncBaseItemInfos()) {
            if (syncBaseItemInfo.getBaseId() == coldGameUiControlConfig.getWarmGameUiControlConfig().getSlaveSyncItemInfo().getActualBaseId()) {
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

        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet viewfield").setViewFieldConfig(new ViewFieldConfig().setToPosition(position)));
        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet fade out").setRemoveLoadingCover(true));
        sceneConfigs.add(new SceneConfig().setInternalName("script: Process Server Quests").setProcessServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupSlaveSpawnScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        DecimalPosition position = null;
        if (coldGameUiControlConfig.getWarmGameUiControlConfig().getSlavePlanetConfig().getStartRegion() != null) {
            position = GeometricUtil.findFreeRandomPosition(coldGameUiControlConfig.getWarmGameUiControlConfig().getSlavePlanetConfig().getStartRegion(), null);
            sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet viewfield").setViewFieldConfig(new ViewFieldConfig().setToPosition(position)));
        }
        // Set camera Position
        // Fade out
        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet fade out").setRemoveLoadingCover(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0).setSuggestedPosition(position);
        baseItemPlacerConfig.setAllowedArea(coldGameUiControlConfig.getWarmGameUiControlConfig().getSlavePlanetConfig().getStartRegion());
        sceneConfigs.add(new SceneConfig().setInternalName("Multiplayer wait for base created").setWaitForBaseCreated(true).setStartPointPlacerConfig(baseItemPlacerConfig));
        sceneConfigs.add(new SceneConfig().setInternalName("script: Process Server Quests").setProcessServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupPlaybackScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().setRemoveLoadingCover(true));
        return sceneConfigs;
    }

    public Set<Integer> getAllTextureIds() {
        Set<Integer> textureIds = Shape3DUtils.getAllTextures(coldGameUiControlConfig.getShape3Ds());
        for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
            if (baseItemType.getBuildupTextureId() != null) {
                textureIds.add(baseItemType.getBuildupTextureId());
            }
            if (baseItemType.getDemolitionImageId() != null) {
                textureIds.add(baseItemType.getDemolitionImageId());
            }
        }

        for (SlopeSkeletonConfig slopeSkeletonConfig : coldGameUiControlConfig.getStaticGameConfig().getSlopeSkeletonConfigs()) {
            if (slopeSkeletonConfig.getTextureId() != null) {
                textureIds.add(slopeSkeletonConfig.getTextureId());
            }
        }

        GroundSkeletonConfig groundSkeletonConfig = coldGameUiControlConfig.getStaticGameConfig().getGroundSkeletonConfig();
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
        for (SlopeSkeletonConfig slopeSkeletonConfig : coldGameUiControlConfig.getStaticGameConfig().getSlopeSkeletonConfigs()) {
            if (slopeSkeletonConfig.getBmId() != null) {
                bumpIds.add(slopeSkeletonConfig.getBmId());
            }
        }

        GroundSkeletonConfig groundSkeletonConfig = coldGameUiControlConfig.getStaticGameConfig().getGroundSkeletonConfig();
        if (groundSkeletonConfig.getTopBmId() != null) {
            bumpIds.add(groundSkeletonConfig.getTopBmId());
        }
        if (groundSkeletonConfig.getBottomBmId() != null) {
            bumpIds.add(groundSkeletonConfig.getBottomBmId());
        }
        if (coldGameUiControlConfig.getStaticGameConfig().getWaterConfig().getBmId() != null) {
            bumpIds.add(coldGameUiControlConfig.getStaticGameConfig().getWaterConfig().getBmId());
        }
        return bumpIds;
    }

    public void onLevelUpdate(LevelConfig newLevelConfig) {
        abstractServerSystemConnection.onLevelChanged(newLevelConfig);
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        if (currentScene != null) {
            currentScene.onQuestProgress(questProgressInfo);
        }
    }

    public void onQuestActivated(QuestConfig quest) {
        if (currentScene != null) {
            currentScene.onQuestActivated(quest);
        }
    }

    public void onQuestPassedServer(QuestConfig quest) {
        if (currentScene != null) {
            currentScene.onQuestPassedServer(quest);
        }
    }

    public boolean hasActiveServerQuest() {
        return currentScene != null && currentScene.getServerQuest() != null;
    }

    public void onEnergyChanged(int consuming, int generating) {
        cockpitService.onEnergyChanged(consuming, generating);
        this.consuming = consuming;
        this.generating = generating;
        handleRadarState(baseItemUiService.hasRadar());
    }

    public void onRadarStateChanged(boolean hasRadar) {
        handleRadarState(hasRadar);
    }

    private void handleRadarState(boolean hasRadar) {
        RadarState radarState;
        if (!hasRadar) {
            radarState = RadarState.NONE;
        } else {
            if (consuming <= generating) {
                radarState = RadarState.WORKING;
            } else {
                radarState = RadarState.NO_POWER;
            }
        }
        cockpitService.showRadar(radarState);
    }

    public void onBaseLost() {
        if (gameEngineMode == GameEngineMode.SLAVE) {
            if (currentScene != null) {
                currentScene.cleanup();
            }
            cockpitService.onEnergyChanged(0,0);
            scenes = new ArrayList<>();
            BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0);
            baseItemPlacerConfig.setAllowedArea(coldGameUiControlConfig.getWarmGameUiControlConfig().getSlavePlanetConfig().getStartRegion());
            scenes.add(new SceneConfig().setInternalName("Multiplayer wait for base created").setWaitForBaseCreated(true).setStartPointPlacerConfig(baseItemPlacerConfig));
            scenes.add(new SceneConfig().setInternalName("script: Process Server Quests").setProcessServerQuests(true));
            nextSceneNumber = 0;
            runScene();
        }
    }

    public boolean isSellSuppressed() {
        return currentScene != null && currentScene.getSceneConfig().isSuppressSell() != null && currentScene.getSceneConfig().isSuppressSell();
    }

    public enum RadarState {
        NONE,
        NO_POWER,
        WORKING
    }
}
