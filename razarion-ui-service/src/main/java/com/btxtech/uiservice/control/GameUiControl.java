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
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.cockpit.TopRightCockpit;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.unlock.UnlockUiService;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @ApplicationScoped lead to crashes with errai CDI
public class GameUiControl { // Equivalent worker class is PlanetService
    private static final long HOME_SCROLL_TIMEOUT = 5000;
    private Logger logger = Logger.getLogger(GameUiControl.class.getName());
    @Inject
    private Instance<Scene> sceneInstance;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private MainCockpitService cockpitService;
    @Inject
    private TopRightCockpit topRightCockpit;
    @Inject
    private ChatUiService chatUiService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private LevelService levelService;
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private UserUiService userUiService;
    @Inject
    private Boot boot;
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
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private UnlockUiService unlockUiService;
    @Inject
    private RenderService renderService;
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
    private long lastHomeScroll;
    private Collection<SyncBaseItemSimpleDto> visitedHomeItems = new ArrayList<>();
    private QuestConfig serverQuest;
    private QuestProgressInfo serverQuestProgress;

    public void setColdGameUiContext(ColdGameUiContext coldGameUiContext) {
        this.coldGameUiContext = coldGameUiContext;
        userUiService.init(coldGameUiContext.getUserContext());
        unlockUiService.setLevelUnlockConfigs(coldGameUiContext.getLevelUnlockConfigs());

        AlarmRaiser.onNull(coldGameUiContext.getWarmGameUiContext(), Alarm.Type.NO_WARM_GAME_UI_CONTEXT);
        gameEngineMode = coldGameUiContext.getWarmGameUiContext().getGameEngineMode();
        AlarmRaiser.onNull(gameEngineMode, Alarm.Type.INVALID_GAME_UI_CONTEXT, "No engine mode", coldGameUiContext.getWarmGameUiContext().getGameUiControlConfigId());
        initServerQuest(coldGameUiContext.getWarmGameUiContext().getSlaveQuestInfo());
    }

    public void onWarmGameConfigLoaded(WarmGameUiContext warmGameUiContext) {
        this.coldGameUiContext.warmGameUiContext(warmGameUiContext);
        gameEngineMode = warmGameUiContext.getGameEngineMode();
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
        terrainScrollHandler.setPlanetSize(getPlanetConfig().getSize());
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
        terrainScrollHandler.setPlanetSize(getPlanetConfig().getSize());
    }

    public void start() {
        startTimeStamp = new Date();
        cockpitService.show(userUiService.getUserContext());
        chatUiService.start();
        nextSceneNumber = 0;
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.startDetailedTracking(getPlanetConfig().getId());
            }
            scenes = coldGameUiContext.getWarmGameUiContext().getSceneConfigs();
            topRightCockpit.setBotSceneIndicationInfos(null);
        } else if (gameEngineMode == GameEngineMode.SLAVE) {
            topRightCockpit.setBotSceneIndicationInfos(coldGameUiContext.getWarmGameUiContext().getBotSceneIndicationInfos());
            return; // Scene started if slave synchronized (from GameEngine)
        } else if (gameEngineMode == GameEngineMode.PLAYBACK) {
            scenes = setupPlaybackScenes();
            playbackControl.start(coldGameUiContext.getWarmGameUiContext().getPlaybackGameUiControlConfig());
            topRightCockpit.setBotSceneIndicationInfos(null);
        } else {
            throw new IllegalArgumentException("Unknown GameEngineMode: " + coldGameUiContext.getWarmGameUiContext().getGameEngineMode());
        }
        if (scenes.isEmpty()) {
            throw new AlarmRaisedException(Alarm.Type.INVALID_GAME_UI_CONTEXT,
                    "No scenes defined",
                    coldGameUiContext.getWarmGameUiContext().getGameUiControlConfigId());
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
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.stopDetailedTracking();
            }
            // TODO Temporary fix for showing move to first multiplayer planet. Pervents loading new planet if multiplayer planet is done. Because there is no new planet
            modalDialogManager.showLeaveStartTutorial(() -> {
                screenCover.fadeInLoadingCover();
                boot.startWarm(); // Replace by LifecycleService. Move boot back to client package. Not needed in DevTools.
            });
        }
    }

    public ColdGameUiContext getColdGameUiContext() {
        return coldGameUiContext;
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

        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet fade out").setRemoveLoadingCover(true));
        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet viewfield").setViewFieldConfig(new ViewFieldConfig().toPosition(scrollToPosition)));
        sceneConfigs.add(new SceneConfig().setInternalName("script: Process Server Quests").setProcessServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupSlaveSpawnScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        DecimalPosition position = null;
        if (coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion() != null) {
            position = GeometricUtil.findFreeRandomPosition(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion(), null);
            sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet viewfield").setViewFieldConfig(new ViewFieldConfig().toPosition(position)));
        } else {
            logger.warning("No StartRegion defined. Scroll to 0:0 position");
            sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet viewfield default").setViewFieldConfig(new ViewFieldConfig().toPosition(DecimalPosition.NULL)));
        }
        // Set camera Position
        // Fade out
        sceneConfigs.add(new SceneConfig().setInternalName("script: Multiplayer Planet fade out").setRemoveLoadingCover(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0).setSuggestedPosition(position);
        baseItemPlacerConfig.setAllowedArea(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
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

    public void onServerBotSceneIndicationChange(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        topRightCockpit.setBotSceneIndicationInfos(botSceneIndicationInfos);
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
            scenes.add(new SceneConfig().setInternalName("Multiplayer wait for base created").setWaitForBaseCreated(true).setStartPointPlacerConfig(baseItemPlacerConfig));
            scenes.add(new SceneConfig().setInternalName("script: Process Server Quests").setProcessServerQuests(true));
            nextSceneNumber = 0;
            runScene();
        }
    }

    public boolean isSellSuppressed() {
        return currentScene != null && currentScene.getSceneConfig().isSuppressSell() != null && currentScene.getSceneConfig().isSuppressSell();
    }

    public void scrollToHome() {
        if (terrainScrollHandler.isScrollDisabled()) {
            return;
        }
        if (System.currentTimeMillis() > lastHomeScroll + HOME_SCROLL_TIMEOUT) {
            visitedHomeItems.clear();
        }
        lastHomeScroll = System.currentTimeMillis();
        Collection<SyncBaseItemSimpleDto> myItems = baseItemUiService.findMyItems();
        if (myItems.isEmpty()) {
            return;
        }
        Optional<SyncBaseItemSimpleDto> optional = myItems.stream().filter(syncBaseItemSimpleDto -> !visitedHomeItems.contains(syncBaseItemSimpleDto)).findFirst();
        SyncBaseItemSimpleDto itemToScrollTo;
        if (optional.isPresent()) {
            itemToScrollTo = optional.get();
        } else {
            visitedHomeItems.clear();
            itemToScrollTo = CollectionUtils.getFirst(myItems);
        }
        DecimalPosition cameraPosition = projectionTransformation.viewFieldCenterToCamera(itemToScrollTo.getPosition2d(), 0);
        camera.setTranslateXY(cameraPosition.getX(), cameraPosition.getY());
        visitedHomeItems.add(itemToScrollTo);
    }

    public enum RadarState {
        NONE,
        NO_POWER,
        WORKING
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
}
