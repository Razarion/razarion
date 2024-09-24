package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;
import jsinterop.annotations.JsType;

import com.btxtech.client.Event;
import javax.inject.Provider;
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
@Singleton // @Singleton lead to crashes with errai CDI
@JsType
public class GameUiControl { // Equivalent worker class is PlanetService
    private final Logger logger = Logger.getLogger(GameUiControl.class.getName());

    private Provider<Scene> sceneInstance;

    private BaseItemUiService baseItemUiService;

    private MainCockpitService cockpitService;

    private QuestCockpitService questCockpitService;

    private ChatUiService chatUiService;

    private ItemTypeService itemTypeService;

    private TerrainTypeService terrainTypeService;

    private LevelService levelService;

    private InventoryTypeService inventoryTypeService;

    private UserUiService userUiService;

    private Boot boot;

    private TrackerService trackerService;

    private Event<GameUiControlInitEvent> gameUiControlInitEvent;

    private ModalDialogManager modalDialogManager;

    private Provider<ScreenCover> screenCover;

    private Provider<AbstractServerSystemConnection> serverSystemConnectionInstance;
    private ColdGameUiContext coldGameUiContext;
    private int nextSceneNumber;
    private Scene currentScene;
    private Date startTimeStamp;
    private Date sceneStartTimeStamp;
    private List<SceneConfig> scenes;
    private AbstractServerSystemConnection abstractServerSystemConnection;
    private GameEngineMode gameEngineMode;
    private int consuming;
    private int generating;
    private QuestConfig serverQuest;
    private QuestProgressInfo serverQuestProgress;

    @Inject
    public GameUiControl(Provider<com.btxtech.uiservice.control.AbstractServerSystemConnection> serverSystemConnectionInstance, Provider<com.btxtech.uiservice.cockpit.ScreenCover> screenCover, ModalDialogManager modalDialogManager, Event<com.btxtech.uiservice.control.GameUiControlInitEvent> gameUiControlInitEvent, TrackerService trackerService, Boot boot, UserUiService userUiService, InventoryTypeService inventoryTypeService, LevelService levelService, TerrainTypeService terrainTypeService, ItemTypeService itemTypeService, ChatUiService chatUiService, QuestCockpitService questCockpitService, MainCockpitService cockpitService, BaseItemUiService baseItemUiService, Provider<com.btxtech.uiservice.control.Scene> sceneInstance) {
        this.serverSystemConnectionInstance = serverSystemConnectionInstance;
        this.screenCover = screenCover;
        this.modalDialogManager = modalDialogManager;
        this.gameUiControlInitEvent = gameUiControlInitEvent;
        this.trackerService = trackerService;
        this.boot = boot;
        this.userUiService = userUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.levelService = levelService;
        this.terrainTypeService = terrainTypeService;
        this.itemTypeService = itemTypeService;
        this.chatUiService = chatUiService;
        this.questCockpitService = questCockpitService;
        this.cockpitService = cockpitService;
        this.baseItemUiService = baseItemUiService;
        this.sceneInstance = sceneInstance;
    }

    public void onWarmGameConfigLoaded(WarmGameUiContext warmGameUiContext) {
        this.coldGameUiContext.setWarmGameUiContext(warmGameUiContext);
        gameEngineMode = warmGameUiContext.getGameEngineMode();
        cockpitService.blinkAvailableUnlock(coldGameUiContext.getWarmGameUiContext().isAvailableUnlocks());
        initServerQuest(warmGameUiContext.getSlaveQuestInfo());
    }

    public void init() {
        abstractServerSystemConnection = serverSystemConnectionInstance.get();
        abstractServerSystemConnection.init();
        itemTypeService.init(coldGameUiContext.getStaticGameConfig());
        terrainTypeService.init(coldGameUiContext.getStaticGameConfig());
        levelService.init(coldGameUiContext.getStaticGameConfig());
        inventoryTypeService.init(coldGameUiContext.getStaticGameConfig());
        gameUiControlInitEvent.fire(new GameUiControlInitEvent(coldGameUiContext));
        AlarmRaiser.onNull(getPlanetConfig(),
                Alarm.Type.INVALID_GAME_UI_CONTEXT,
                "No planet",
                coldGameUiContext.getWarmGameUiContext().getGameUiControlConfigId());
        // TODO terrainScrollHandler.setPlanetSize(getPlanetConfig().getSize());
    }

    public void closeConnection() {
        if (abstractServerSystemConnection != null) {
            abstractServerSystemConnection.close();
            abstractServerSystemConnection = null;
        }
    }

    public void initWarm() {
        abstractServerSystemConnection.sendGameSessionUuid();
        gameEngineMode = coldGameUiContext.getWarmGameUiContext().getGameEngineMode();
        // TODO terrainScrollHandler.setPlanetSize(getPlanetConfig().getSize());
        cockpitService.blinkAvailableUnlock(coldGameUiContext.getWarmGameUiContext().isAvailableUnlocks());
    }

    public void start() {
        startTimeStamp = new Date();
        cockpitService.show(userUiService.getUserContext());
        chatUiService.start();
        nextSceneNumber = 0;
        if (gameEngineMode == GameEngineMode.SLAVE) {
            // Scene started if slave synchronized (from GameEngine)
            return;
        }
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.startDetailedTracking(getPlanetConfig().getId());
            }
            scenes = coldGameUiContext.getWarmGameUiContext().getSceneConfigs();
            if (scenes.isEmpty()) {
                throw new AlarmRaisedException(Alarm.Type.INVALID_GAME_UI_CONTEXT, "No scenes defined", coldGameUiContext.getWarmGameUiContext().getGameUiControlConfigId());
            }
            runScene();
            return;
        }
        throw new IllegalArgumentException("Unknown GameEngineMode: " + coldGameUiContext.getWarmGameUiContext().getGameEngineMode());
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
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.stopDetailedTracking();
            }
            // TODO Temporary fix for showing move to first multiplayer planet. Pervents loading new planet if multiplayer planet is done. Because there is no new planet
            modalDialogManager.showLeaveStartTutorial(() -> {
                screenCover.get().fadeInLoadingCover();
                boot.startWarm(); // Replace by LifecycleService. Move boot back to client package. Not needed in DevTools.
            });
        }
    }

    public ColdGameUiContext getColdGameUiContext() {
        return coldGameUiContext;
    }

    public void setColdGameUiContext(ColdGameUiContext coldGameUiContext) {
        this.coldGameUiContext = coldGameUiContext;
        userUiService.init(coldGameUiContext.getUserContext());
        cockpitService.blinkAvailableUnlock(coldGameUiContext.getWarmGameUiContext().isAvailableUnlocks());

        AlarmRaiser.onNull(coldGameUiContext.getWarmGameUiContext(), Alarm.Type.NO_WARM_GAME_UI_CONTEXT);
        gameEngineMode = coldGameUiContext.getWarmGameUiContext().getGameEngineMode();
        AlarmRaiser.onNull(gameEngineMode, Alarm.Type.INVALID_GAME_UI_CONTEXT, "No engine mode", coldGameUiContext.getWarmGameUiContext().getGameUiControlConfigId());
        initServerQuest(coldGameUiContext.getWarmGameUiContext().getSlaveQuestInfo());
    }

    public PlanetConfig getPlanetConfig() {
        return coldGameUiContext.getWarmGameUiContext().getPlanetConfig();
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

    public void setGameInfo(NativeTickInfo nativeTickInfo) {
        baseItemUiService.updateGameInfo(nativeTickInfo);
        if (nativeTickInfo.xpFromKills > 0) {
            userUiService.increaseXp(nativeTickInfo.xpFromKills);
        }
    }

    public int getMyLimitation4ItemType(int itemTypeId) {
        int unlockedCount = userUiService.getUserContext().getUnlockedItemLimit().getOrDefault(itemTypeId, 0);
        int levelCount = levelService.getLevel(userUiService.getUserContext().getLevelId()).limitation4ItemType(itemTypeId);
        int planetCount = getPlanetConfig().imitation4ItemType(itemTypeId);
        return Math.min(levelCount + unlockedCount, planetCount);
    }

    public void onInitialSlaveSynchronized(DecimalPosition scrollToPosition) {
        if (scrollToPosition != null) {
            scenes = setupSlaveExistingScenes(scrollToPosition);
        } else {
            scenes = setupSlaveSpawnScenes();
        }
        runScene();
    }

    private List<SceneConfig> setupSlaveExistingScenes(DecimalPosition scrollToPosition) {
        List<SceneConfig> sceneConfigs = new ArrayList<>();

        sceneConfigs.add(new SceneConfig().internalName("script: Multiplayer Planet viewfield").viewFieldConfig(new ViewFieldConfig().toPosition(scrollToPosition)));
        sceneConfigs.add(new SceneConfig().internalName("script: Multiplayer Planet fade out").removeLoadingCover(true));
        sceneConfigs.add(new SceneConfig().internalName("script: Process Server Quests").processServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupSlaveSpawnScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        DecimalPosition position = null;
        // Set camera Position
        if (coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion() != null) {
            if (coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getNoBaseViewPosition() != null) {
                position = coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getNoBaseViewPosition();
            } else {
                position = GeometricUtil.findFreeRandomPosition(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
            }
            sceneConfigs.add(new SceneConfig().internalName("script: Multiplayer Planet viewfield").viewFieldConfig(new ViewFieldConfig().toPosition(position)));
        } else {
            logger.warning("No StartRegion defined. Scroll to 0:0 position");
            sceneConfigs.add(new SceneConfig().internalName("script: Multiplayer Planet viewfield default").viewFieldConfig(new ViewFieldConfig().toPosition(DecimalPosition.NULL)));
        }
        // Fade out
        sceneConfigs.add(new SceneConfig().internalName("script: Multiplayer Planet fade out").removeLoadingCover(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0).setSuggestedPosition(position);
        baseItemPlacerConfig.setAllowedArea(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
        sceneConfigs.add(new SceneConfig().internalName("Multiplayer wait for base created").waitForBaseCreated(true).startPointPlacerConfig(baseItemPlacerConfig));
        sceneConfigs.add(new SceneConfig().internalName("script: Process Server Quests").processServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupPlaybackScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().removeLoadingCover(true));
        return sceneConfigs;
    }

    public Set<Integer> getAllTextureIds() {
        logger.severe("GameUiControl.getAllTextureIds(): Fix TODO");
        return new HashSet<>();
        // TODO  Set<Integer> textureIds = Shape3DUtils.getAllTextures(coldGameUiContext.getShape3Ds());
        // TODO  for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
        // TODO     if (baseItemType.getBuildupTextureId() != null) {
        // TODO         textureIds.add(baseItemType.getBuildupTextureId());
        // TODO     }
        // TODO     if (baseItemType.getDemolitionImageId() != null) {
        // TODO         textureIds.add(baseItemType.getDemolitionImageId());
        // TODO     }
        // TODO }

        // TODO for (SlopeConfig slopeConfig : coldGameUiContext.getStaticGameConfig().getSlopeConfigs()) {
        // TODO if (slopeConfig.getSlopeTextureId() != null) {
        // TODO     textureIds.add(slopeConfig.getSlopeTextureId());
        // TODO }
        // TODO }

        // TODO GroundSkeletonConfig groundSkeletonConfig = coldGameUiContext.getStaticGameConfig().getGroundSkeletonConfig();
        //  if (groundSkeletonConfig.getTopTextureId() != null) {
        //      textureIds.add(groundSkeletonConfig.getTopTextureId());
        //  }
        //  if (groundSkeletonConfig.getBottomTextureId() != null) {
        //      textureIds.add(groundSkeletonConfig.getBottomTextureId());
        //  }
        // return textureIds;
    }

    public Set<Integer> getAllBumpTextureIds() {
        Set<Integer> bumpIds = new HashSet<>();
        for (SlopeConfig slopeConfig : coldGameUiContext.getStaticGameConfig().getSlopeConfigs()) {
            // TODO if (slopeConfig.getSlopeBumpMapId() != null) {
            // TODO    bumpIds.add(slopeConfig.getSlopeBumpMapId());
            // TODO }
        }

        logger.severe("GameUiControl.getAllBumpTextureIds(): Fix TODO");
//  TODO      GroundSkeletonConfig groundSkeletonConfig = coldGameUiContext.getStaticGameConfig().getGroundSkeletonConfig();
//        if (groundSkeletonConfig.getBottomBmId() != null) {
//            bumpIds.add(groundSkeletonConfig.getBottomBmId());
//        }
//        if (coldGameUiContext.getStaticGameConfig().getWaterConfig().getNormMapId() != null) {
//            bumpIds.add(coldGameUiContext.getStaticGameConfig().getWaterConfig().getNormMapId());
//        }
        return bumpIds;
    }

    public void onLevelUpdate(LevelConfig newLevelConfig) {
        abstractServerSystemConnection.onLevelChanged(newLevelConfig);
    }

    public void sendChatMessage(String message) {
        abstractServerSystemConnection.sendChatMessage(message);
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo, boolean fromServer) {
        if (fromServer) {
            serverQuestProgress = questProgressInfo;
        }
        if (currentScene != null) {
            currentScene.onQuestProgress(questProgressInfo);
        }
    }

    public void onQuestActivatedServer(QuestConfig quest) {
        serverQuest = quest;
        if (currentScene != null) {
            currentScene.onQuestActivatedServer(quest);
        }
    }

    public void onQuestPassedServer(QuestConfig quest) {
        serverQuest = null;
        if (currentScene != null) {
            currentScene.onQuestPassedServer(quest);
        }
    }

    public boolean hasActiveServerQuest() {
        return currentScene != null && serverQuest != null;
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
            cockpitService.onEnergyChanged(0, 0);
            scenes = new ArrayList<>();
            BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0);
            baseItemPlacerConfig.setAllowedArea(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
            scenes.add(new SceneConfig().internalName("Multiplayer wait for base created").waitForBaseCreated(true).startPointPlacerConfig(baseItemPlacerConfig));
            scenes.add(new SceneConfig().internalName("script: Process Server Quests").processServerQuests(true));
            nextSceneNumber = 0;
            runScene();
        }
    }

    public boolean isSellSuppressed() {
        return currentScene != null && currentScene.getSceneConfig().isSuppressSell() != null && currentScene.getSceneConfig().isSuppressSell();
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    private void initServerQuest(SlaveQuestInfo slaveQuestInfo) {
        if (slaveQuestInfo != null) {
            serverQuest = slaveQuestInfo.getActiveQuest();
            serverQuestProgress = slaveQuestInfo.getQuestProgressInfo();
        } else {
            serverQuest = null;
            serverQuestProgress = null;
        }
    }

    public QuestConfig getServerQuest() {
        return serverQuest;
    }

    public QuestProgressInfo getServerQuestProgress() {
        return serverQuestProgress;
    }

    public enum RadarState {
        NONE,
        NO_POWER,
        WORKING
    }
}
