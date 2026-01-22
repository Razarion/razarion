package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IntIntMap;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDtoUtils;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;

import jakarta.inject.Provider;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
public abstract class GameEngineControl {
    private final Logger logger = Logger.getLogger(GameEngineControl.class.getName());
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final BoxUiService boxUiService;
    private final AudioService audioService;
    private final GameUiControl gameUiControl;
    private final SelectionService selectionService;
    private final UserUiService userUiService;
    private final InventoryUiService inventoryUiService;
    private final TerrainUiService terrainUiService;
    private final Provider<Boot> boot;
    private final PerfmonService perfmonService;
    private final Provider<InputService> inputServices;
    private final BabylonRendererService babylonRendererService;
    private Consumer<Collection<PerfmonStatistic>> perfmonConsumer;
    private DeferredStartup deferredStartup;
    private Runnable stopCallback;

    public GameEngineControl(Provider<InputService> inputServices,
                             PerfmonService perfmonService,
                             Provider<Boot> boot,
                             TerrainUiService terrainUiService,
                             InventoryUiService inventoryUiService,
                             UserUiService userUiService,
                             SelectionService selectionService,
                             GameUiControl gameUiControl,
                             AudioService audioService,
                             BoxUiService boxUiService,
                             ResourceUiService resourceUiService,
                             BaseItemUiService baseItemUiService,
                             BabylonRendererService babylonRendererService) {
        this.inputServices = inputServices;
        this.perfmonService = perfmonService;
        this.boot = boot;
        this.terrainUiService = terrainUiService;
        this.inventoryUiService = inventoryUiService;
        this.userUiService = userUiService;
        this.selectionService = selectionService;
        this.gameUiControl = gameUiControl;
        this.audioService = audioService;
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.babylonRendererService = babylonRendererService;
    }

    protected abstract void sendToWorker(GameEngineControlPackage.Command command, Object... data);

    protected abstract void onLoaded();

    public abstract boolean isStarted();

    protected abstract NativeTickInfo castToNativeTickInfo(Object javaScriptObject);

    protected abstract NativeSyncBaseItemTickInfo castToNativeSyncBaseItemTickInfo(Object singleData);

    protected abstract void onConnectionLost();

    public void enableTracking() {
    }

    public void start(String bearerToken) {
        sendToWorker(GameEngineControlPackage.Command.START, bearerToken);
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
    }

    public void stop(Runnable stopCallback) {
        perfmonConsumer = null;
        this.stopCallback = stopCallback;
        sendToWorker(GameEngineControlPackage.Command.STOP_REQUEST);
    }

    public void init(ColdGameUiContext coldGameUiContext, DeferredStartup initializationReferredStartup) {
        this.terrainUiService.setPlanetConfig(coldGameUiContext.getWarmGameUiContext().getPlanetConfig());
        this.deferredStartup = initializationReferredStartup;
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE,
                coldGameUiContext.getStaticGameConfig(),
                coldGameUiContext.getWarmGameUiContext().getPlanetConfig(),
                userUiService.getUserContext(),
                coldGameUiContext.getWarmGameUiContext().getGameEngineMode(),
                boot.get().getGameSessionUuid());
    }

    public void initWarm(PlanetConfig planetConfig, GameEngineMode gameEngineMode, DeferredStartup deferredStartup) {
        this.terrainUiService.setPlanetConfig(planetConfig);
        this.deferredStartup = deferredStartup;
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE_WARM, planetConfig, userUiService.getUserContext(), gameEngineMode, boot.get().getGameSessionUuid());
    }

    void startBots(List<BotConfig> botConfigs) {
        sendToWorker(GameEngineControlPackage.Command.START_BOTS, botConfigs);
    }

    void executeBotCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs) {
        sendToWorker(GameEngineControlPackage.Command.EXECUTE_BOT_COMMANDS, botCommandConfigs);
    }

    void createResources(List<ResourceItemPosition> resourceItemTypePositions) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_RESOURCES, resourceItemTypePositions);
    }

    // Needs to be public or userUiService is not set
    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_HUMAN_BASE_WITH_BASE_ITEM,
                userUiService.getUserContext().getLevelId(),
                new IntIntMap().map(userUiService.getUserContext().getUnlockedItemLimit()),
                userUiService.getUserContext().getUserId(),
                userUiService.getUserContext().getName() == null ? "" : userUiService.getUserContext().getName(), // Errai demarsheller is not able to handle top level null JSON object
                position);
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem) {
        sendToWorker(GameEngineControlPackage.Command.USE_INVENTORY_ITEM, useInventoryItem);
    }

    public void moveCmd(Collection<SyncBaseItemSimpleDto> items, DecimalPosition terrainPosition) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_MOVE, SyncItemSimpleDtoUtils.toIds(items), terrainPosition);
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.MOVE));
    }

    public void attackCmd(Collection<SyncBaseItemSimpleDto> attackers, SyncBaseItemSimpleDto target) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_ATTACK, SyncItemSimpleDtoUtils.toIds(attackers), target.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.ATTACK));
    }

    public void harvestCmd(Collection<SyncBaseItemSimpleDto> harvesters, SyncResourceItemSimpleDto resource) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_HARVEST, SyncItemSimpleDtoUtils.toIds(harvesters), resource.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.HARVEST));
    }

    public void pickBoxCmd(Collection<SyncBaseItemSimpleDto> pickers, SyncBoxItemSimpleDto box) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_PICK_BOX, SyncItemSimpleDtoUtils.toIds(pickers), box.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.PICK_BOX).setSynBoxItemId(box.getId()));
    }

    public void loadContainerCmd(Collection<SyncBaseItemSimpleDto> contained, SyncBaseItemSimpleDto container) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_LOAD_CONTAINER, SyncItemSimpleDtoUtils.toIds(contained), container.getId());
        // gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.LOAD_CONTAINER).setSynBoxItemId(box.getId()));
    }

    public void unloadContainerCmd(int containerId, DecimalPosition unloadPosition) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_UNLOAD_CONTAINER, containerId, unloadPosition);
        // gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.UNLOAD_CONTAINER).setSynBoxItemId(box.getId()));
    }

    public void buildCmd(SyncBaseItemSimpleDto builder, DecimalPosition position, BaseItemType toBeBuild) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_BUILD, builder.getId(), position, toBeBuild.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.BUILD).setToBeBuiltId(toBeBuild.getId()));
    }

    public void finalizeBuildCmd(Collection<SyncBaseItemSimpleDto> builders, SyncBaseItemSimpleDto toBeFinalized) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_FINALIZE_BUILD, SyncItemSimpleDtoUtils.toIds(builders), toBeFinalized.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.FINALIZE_BUILD).setToBeFinalizedId(toBeFinalized.getId()));
    }

    public void fabricateCmd(int factoryId, BaseItemType toBeBuild) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_FABRICATE, factoryId, toBeBuild.getId());
        // TODO gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.FABRICATE).setToBeBuiltId(toBeBuild.getId()));
    }

    public void sellItems(Collection<SyncBaseItemSimpleDto> items) {
        sendToWorker(GameEngineControlPackage.Command.SELL_ITEMS, SyncItemSimpleDtoUtils.toIds(items));
    }

    public void activateQuest(QuestConfig questConfig) {
        sendToWorker(GameEngineControlPackage.Command.ACTIVATE_QUEST, questConfig);
    }

    public void dropBoxes(List<BoxItemPosition> boxItemPositions) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_BOXES, boxItemPositions);
    }

    public void updateLevel(int levelId) {
        sendToWorker(GameEngineControlPackage.Command.UPDATE_LEVEL, levelId);
    }

    public void perfmonRequest(Consumer<Collection<PerfmonStatistic>> perfmonConsumer) {
        this.perfmonConsumer = perfmonConsumer;
        sendToWorker(GameEngineControlPackage.Command.PERFMON_REQUEST);
    }

    public void requestTerrainTile(Index terrainTileIndex) {
        sendToWorker(GameEngineControlPackage.Command.TERRAIN_TILE_REQUEST, terrainTileIndex);
    }

    private void onTickUpdate(NativeTickInfo nativeTickInfo) {
        perfmonService.onEntered(PerfmonEnum.CLIENT_GAME_ENGINE_UPDATE);
        try {
            if (nativeTickInfo.killedSyncBaseItems != null) {
                selectionService.baseItemRemoved(nativeTickInfo.killedSyncBaseItems);
                baseItemUiService.onSyncBaseItemsExplode(nativeTickInfo.killedSyncBaseItems);
            }
            baseItemUiService.updateSyncBaseItems(nativeTickInfo.updatedNativeSyncBaseItemTickInfos);
            gameUiControl.setGameInfo(nativeTickInfo);
            if (nativeTickInfo.removeSyncBaseItemIds != null) {
                selectionService.baseItemRemoved(nativeTickInfo.removeSyncBaseItemIds);
                // effectVisualizationService.baseItemRemoved(nativeTickInfo.removeSyncBaseItemIds);
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in onTickUpdate", t);
        }
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
        perfmonService.onLeft(PerfmonEnum.CLIENT_GAME_ENGINE_UPDATE);
    }

    private void onTickUpdateFailed() {
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
    }

    private void onPerfmonResponse(Collection<PerfmonStatistic> statisticEntries) {
        if (perfmonConsumer != null) {
            perfmonConsumer.accept(statisticEntries);
            perfmonConsumer = null;
        }
    }

    private void onInitialized() {
        if (deferredStartup != null) {
            deferredStartup.finished();
            deferredStartup = null;
        }
    }

    private void onInitialisingFailed(String errorText) {
        if (deferredStartup != null) {
            deferredStartup.failed(new AlarmRaisedException(Alarm.Type.FAIL_START_GAME_ENGINE, errorText));
            deferredStartup = null;
        }
    }

    private void onStopped() {
        if (stopCallback != null) {
            stopCallback.run();
            stopCallback = null;
        }
    }

    public void getTerrainType(Index nodePosition) {
        sendToWorker(GameEngineControlPackage.Command.GET_TERRAIN_TYPE, nodePosition);
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case LOADED:
                onLoaded();
                break;
            case INITIALIZED:
                onInitialized();
                break;
            case INITIALISING_FAILED:
                onInitialisingFailed((String) controlPackage.getSingleData());
                break;
            case TICK_UPDATE_RESPONSE:
                onTickUpdate(castToNativeTickInfo(controlPackage.getData(0)));
                break;
            case TICK_UPDATE_RESPONSE_FAIL:
                onTickUpdateFailed();
                break;
            case SYNC_ITEM_START_SPAWNED:
                NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = castToNativeSyncBaseItemTickInfo(controlPackage.getSingleData());
                audioService.onSpawnSyncItem(nativeSyncBaseItemTickInfo);
                babylonRendererService.startSpawn(nativeSyncBaseItemTickInfo);
                // TODO gameTipService.onSpawnSyncItem(castToNativeSyncBaseItemTickInfo(controlPackage.getSingleData()));
                break;
            case SYNC_ITEM_IDLE:
                // TODO gameTipService.onSyncBaseItemIdle(castToNativeSyncBaseItemTickInfo(controlPackage.getSingleData()));
                break;
            case RESOURCE_CREATED:
                resourceUiService.addResource((SyncResourceItemSimpleDto) controlPackage.getSingleData());
                break;
            case RESOURCE_DELETED:
                resourceUiService.removeResource((Integer) controlPackage.getSingleData());
                break;
            case ENERGY_CHANGED:
                gameUiControl.onEnergyChanged((Integer) controlPackage.getData(0), (Integer) controlPackage.getData(1));
                break;
            case BOX_CREATED:
                boxUiService.addBox((SyncBoxItemSimpleDto) controlPackage.getSingleData());
                break;
            case BOX_DELETED:
                boxUiService.removeBox((Integer) controlPackage.getSingleData());
                break;
            case BASE_CREATED:
                baseItemUiService.addBase((PlayerBaseDto) controlPackage.getSingleData());
                break;
            case BASE_DELETED:
                baseItemUiService.removeBase((Integer) controlPackage.getSingleData());
                break;
            case BASE_UPDATED:
                baseItemUiService.updateBase((PlayerBaseDto) controlPackage.getSingleData());
                break;
            case QUEST_PASSED:
                gameUiControl.onQuestPassed();
                break;
            case BOX_PICKED:
                inventoryUiService.onOnBoxPicked((BoxContent) controlPackage.getSingleData());
                break;
            case PROJECTILE_FIRED:
                baseItemUiService.onProjectileFired((int) controlPackage.getData(0), (int) controlPackage.getData(1), (DecimalPosition) controlPackage.getData(2));
                break;
            case PROJECTILE_DETONATION:
                //effectVisualizationService.onProjectileDetonation((int) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1));
                break;
            case PERFMON_RESPONSE:
                onPerfmonResponse((Collection<PerfmonStatistic>) controlPackage.getData(0));
                break;
            case TERRAIN_TILE_RESPONSE:
                terrainUiService.onTerrainTileResponse((TerrainTile) controlPackage.getData(0));
                break;
            case STOP_RESPONSE:
                onStopped();
                break;
            case QUEST_PROGRESS:
                gameUiControl.onQuestProgress((QuestProgressInfo) controlPackage.getData(0), false);
                break;
            case CONNECTION_LOST:
                onConnectionLost();
                break;
            case INITIAL_SLAVE_SYNCHRONIZED:
                gameUiControl.onInitialSlaveSynchronized((DecimalPosition) controlPackage.getData(0));
                break;
            case INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: // Marshaller can not handle null value
                gameUiControl.onInitialSlaveSynchronized(null);
                break;
            case GET_TERRAIN_TYPE_ANSWER:
                inputServices.get().onGetTerrainTypeAnswer((Index) controlPackage.getData(0), (TerrainType) controlPackage.getData(1));
                break;
            case COMMAND_MOVE_ACK:
                inputServices.get().onMoveCommandAck();
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }
}
