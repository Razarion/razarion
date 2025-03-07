package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.cockpit.ChatUiService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.user.UserUiService;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton // @Singleton lead to crashes with errai CDI
@JsType
public class GameUiControl { // Equivalent worker class is PlanetService
    private final Logger logger = Logger.getLogger(GameUiControl.class.getName());
    private final Provider<Scene> sceneInstance;
    private final BaseItemUiService baseItemUiService;
    private final MainCockpitService cockpitService;
    private final ChatUiService chatUiService;
    private final ItemTypeService itemTypeService;
    private final TerrainTypeService terrainTypeService;
    private final LevelService levelService;
    private final InventoryTypeService inventoryTypeService;
    private final Provider<UserUiService> userUiService;
    private final Provider<Boot> boot;
    private final Provider<TrackerService> trackerService;
    private final InitializeService initializeService;
    private final ModalDialogManager modalDialogManager;
    private final Provider<ScreenCover> screenCover;
    private final Provider<AbstractServerSystemConnection> serverSystemConnectionInstance;
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
    public GameUiControl(Provider<AbstractServerSystemConnection> serverSystemConnectionInstance,
                         Provider<ScreenCover> screenCover,
                         ModalDialogManager modalDialogManager,
                         InitializeService initializeService,
                         Provider<TrackerService> trackerService,
                         Provider<Boot> boot,
                         Provider<UserUiService> userUiService,
                         InventoryTypeService inventoryTypeService,
                         LevelService levelService,
                         TerrainTypeService terrainTypeService,
                         ItemTypeService itemTypeService,
                         ChatUiService chatUiService,
                         MainCockpitService cockpitService,
                         BaseItemUiService baseItemUiService,
                         Provider<Scene> sceneInstance) {
        this.serverSystemConnectionInstance = serverSystemConnectionInstance;
        this.screenCover = screenCover;
        this.modalDialogManager = modalDialogManager;
        this.initializeService = initializeService;
        this.trackerService = trackerService;
        this.boot = boot;
        this.userUiService = userUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.levelService = levelService;
        this.terrainTypeService = terrainTypeService;
        this.itemTypeService = itemTypeService;
        this.chatUiService = chatUiService;
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
        initializeService.setColdGameUiContext(coldGameUiContext);
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
        cockpitService.show(userUiService.get().getUserContext());
        chatUiService.start();
        nextSceneNumber = 0;
        if (gameEngineMode == GameEngineMode.SLAVE) {
            // Scene started if slave synchronized (from GameEngine)
            return;
        }
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.get().startDetailedTracking(getPlanetConfig().getId());
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
        trackerService.get().trackScene(sceneStartTimeStamp, currentScene.getSceneConfig().getInternalName());
        sceneStartTimeStamp = null;
    }

    public void finished() {
        if (startTimeStamp == null) {
            logger.warning("startTimeStamp == null");
        } else {
            trackerService.get().trackGameUiControl(startTimeStamp);
            startTimeStamp = null;
        }
        if (gameEngineMode == GameEngineMode.MASTER) {
            if (coldGameUiContext.getWarmGameUiContext().isDetailedTracking()) {
                trackerService.get().stopDetailedTracking();
            }
            // TODO Temporary fix for showing move to first multiplayer planet. Pervents loading new planet if multiplayer planet is done. Because there is no new planet
            modalDialogManager.showLeaveStartTutorial(() -> {
                screenCover.get().fadeInLoadingCover();
                boot.get().startWarm(); // Replace by LifecycleService. Move boot back to client package. Not needed in DevTools.
            });
        }
    }

    public ColdGameUiContext getColdGameUiContext() {
        return coldGameUiContext;
    }

    public void setColdGameUiContext(ColdGameUiContext coldGameUiContext) {
        this.coldGameUiContext = coldGameUiContext;
        userUiService.get().init(coldGameUiContext.getUserContext());
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
            userUiService.get().increaseXp(nativeTickInfo.xpFromKills);
        }
    }

    public int getMyLimitation4ItemType(int itemTypeId) {
        int unlockedCount = userUiService.get().getUserContext().getUnlockedItemLimit().getOrDefault(itemTypeId, 0);
        int levelCount = levelService.getLevel(userUiService.get().getUserContext().getLevelId()).limitation4ItemType(itemTypeId);
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
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().suggestedPosition(position);
        baseItemPlacerConfig.allowedArea(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
        sceneConfigs.add(new SceneConfig().internalName("Multiplayer wait for base created").waitForBaseCreated(true).startPointPlacerConfig(baseItemPlacerConfig));
        sceneConfigs.add(new SceneConfig().internalName("script: Process Server Quests").processServerQuests(true));
        return sceneConfigs;
    }

    private List<SceneConfig> setupPlaybackScenes() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().removeLoadingCover(true));
        return sceneConfigs;
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
            BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().enemyFreeRadius(10.0);
            baseItemPlacerConfig.allowedArea(coldGameUiContext.getWarmGameUiContext().getSlavePlanetConfig().getStartRegion());
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
