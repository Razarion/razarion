package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.tracking.PlayerBaseTracking;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.NoInterpolatedTerrainTriangleException;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.utils.ExceptionUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.07.2016.
 */
public abstract class GameEngineWorker implements PlanetTickListener, QuestListener, GameLogicListener {
    private Logger logger = Logger.getLogger(GameEngineWorker.class.getName());
    @Inject
    private PlanetService planetService;
    @Inject
    private Event<StaticGameInitEvent> staticGameInitEvent;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private QuestService questService;
    @Inject
    private BoxService boxService;
    @Inject
    private CommandService commandService;
    @Inject
    private GameLogicService logicService;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainService terrainService;
    @Inject
    private Instance<AbstractServerGameConnection> connectionInstance;
    @Inject
    private Instance<WorkerTrackerHandler> workerTrackerHandlerInstance;
    private UserContext userContext;
    private PlayerBase playerBase;
    private List<SyncBaseItemSimpleDto> killed = new ArrayList<>();
    private List<SyncBaseItemSimpleDto> removed = new ArrayList<>();
    private int xpFromKills;
    private boolean sendTickUpdate;
    private AbstractServerGameConnection serverConnection;
    private GameEngineMode gameEngineMode;
    private WorkerTrackerHandler workerTrackerHandler;
    private String gameSessionUuid;

    protected abstract void sendToClient(GameEngineControlPackage.Command command, Object... object);

    @PostConstruct
    public void postConstruct() {
        questService.addQuestListener(this);
        logicService.setGameLogicListener(this);
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                initialise((StaticGameConfig) controlPackage.getData(0), (PlanetConfig) controlPackage.getData(1), (SlaveSyncItemInfo) controlPackage.getData(2),
                        (UserContext) controlPackage.getData(3), (GameEngineMode) controlPackage.getData(4), (Boolean) controlPackage.getData(5), (String) controlPackage.getData(6));
                break;
            case INITIALIZE_WARM:
                initialiseWarm((PlanetConfig) controlPackage.getData(0), (SlaveSyncItemInfo) controlPackage.getData(1), (UserContext) controlPackage.getData(2), (GameEngineMode) controlPackage.getData(3));
                break;
            case START:
                start();
                break;
            case STOP_REQUEST:
                stop();
                break;
            case TICK_UPDATE_REQUEST:
                sendTickUpdate = true;
                break;
            case START_BOTS:
                botService.startBots((Collection<BotConfig>) controlPackage.getSingleData());
                break;
            case EXECUTE_BOT_COMMANDS:
                botService.executeCommands((List<? extends AbstractBotCommandConfig>) controlPackage.getSingleData());
                break;
            case CREATE_RESOURCES:
                resourceService.createResources((Collection<ResourceItemPosition>) controlPackage.getSingleData());
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                createHumanBaseWithBaseItem((Integer) controlPackage.getData(0), (Map<Integer, Integer>) controlPackage.getData(1), (HumanPlayerId) controlPackage.getData(2), (String) controlPackage.getData(3), (DecimalPosition) controlPackage.getData(4));
                break;
            case USE_INVENTORY_ITEM:
                useInventoryItem((UseInventoryItem) controlPackage.getData(0));
                break;
            case CREATE_BOXES:
                boxService.dropBoxes((List<BoxItemPosition>) controlPackage.getSingleData());
                break;
            case ACTIVATE_QUEST:
                activateQuest(userContext.getHumanPlayerId(), (QuestConfig) controlPackage.getData(0));
                break;
            case COMMAND_ATTACK:
                commandService.attack((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_FINALIZE_BUILD:
                commandService.finalizeBuild((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_BUILD:
                commandService.build((int) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1), (int) controlPackage.getData(2));
                break;
            case COMMAND_FABRICATE:
                commandService.fabricate((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_HARVEST:
                commandService.harvest((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_MOVE:
                commandService.move((List<Integer>) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1));
                break;
            case COMMAND_PICK_BOX:
                commandService.pickupBox((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_LOAD_CONTAINER:
                commandService.loadContainer((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_UNLOAD_CONTAINER:
                commandService.unloadContainer((int) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1));
                break;
            case UPDATE_LEVEL:
                updateLevel((int) controlPackage.getData(0));
                break;
            case PERFMON_REQUEST:
                onPerfmonRequest();
                break;
            case SINGLE_Z_TERRAIN:
                getTerrainZ((DecimalPosition) controlPackage.getData(0));
                break;
            case TERRAIN_TILE_REQUEST:
                getTerrainTile((Index) controlPackage.getData(0));
                break;
            case PLAYBACK_PLAYER_BASE:
                onPlayerBaseTracking((PlayerBaseTracking) controlPackage.getData(0));
                break;
            case PLAYBACK_SYNC_ITEM_DELETED:
                onServerSyncItemDeleted((SyncItemDeletedInfo) controlPackage.getData(0));
                break;
            case PLAYBACK_SYNC_BASE_ITEM:
                baseItemService.onSlaveSyncBaseItemChanged((SyncBaseItemInfo) controlPackage.getData(0));
                break;
            case PLAYBACK_SYNC_RESOURCE_ITEM:
                resourceService.onSlaveSyncResourceItemChanged((SyncResourceItemInfo) controlPackage.getData(0));
                break;
            case PLAYBACK_SYNC_BOX_ITEM:
                boxService.onSlaveSyncBoxItemChanged((SyncBoxItemInfo) controlPackage.getData(0));
                break;
            case SELL_ITEMS:
                sellItems((List<Integer>) controlPackage.getData(0));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }

    private void initialise(StaticGameConfig staticGameConfig, PlanetConfig planetConfig, SlaveSyncItemInfo slaveSyncItemInfo, UserContext userContext, GameEngineMode gameEngineMode, boolean detailedTracking, String gameSessionUuid) {
        try {
            this.gameSessionUuid = gameSessionUuid;
            staticGameInitEvent.fire(new StaticGameInitEvent(staticGameConfig));
            planetService.addTickListener(this);
            initWarmInternal(planetConfig, slaveSyncItemInfo, userContext, gameEngineMode, () -> {
                if (gameEngineMode == GameEngineMode.MASTER && detailedTracking) {
                    workerTrackerHandler = workerTrackerHandlerInstance.get();
                    workerTrackerHandler.start(gameSessionUuid);

                }
                sendToClient(GameEngineControlPackage.Command.INITIALIZED);
            }, failString -> {
                sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, failString);
            });
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, ExceptionUtil.setupStackTrace(null, t));
        }
    }

    private void initialiseWarm(PlanetConfig planetConfig, SlaveSyncItemInfo slaveSyncItemInfo, UserContext userContext, GameEngineMode gameEngineMode) {
        try {
            initWarmInternal(planetConfig, slaveSyncItemInfo, userContext, gameEngineMode, () -> {
                workerTrackerHandler = null;
                sendToClient(GameEngineControlPackage.Command.INITIALIZED);
            }, failString -> {
                sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, failString);
            });
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, ExceptionUtil.setupStackTrace(null, t));
        }
    }

    private void initWarmInternal(PlanetConfig planetConfig, SlaveSyncItemInfo slaveSyncItemInfo, UserContext userContext, GameEngineMode gameEngineMode, Runnable finishCallback, Consumer<String> failCallback) {
        this.gameEngineMode = gameEngineMode;
        this.userContext = userContext;
        planetService.initialise(planetConfig, gameEngineMode, null, slaveSyncItemInfo, () -> {
                    if (gameEngineMode == GameEngineMode.SLAVE && slaveSyncItemInfo.getActualBaseId() != null) {
                        playerBase = baseItemService.getPlayerBase4BaseId(slaveSyncItemInfo.getActualBaseId());
                    }
                    finishCallback.run();
                    if (gameEngineMode == GameEngineMode.SLAVE) {
                        serverConnection = connectionInstance.get();
                        serverConnection.init();
                    }
                }
                , failCallback);
    }

    private void createHumanBaseWithBaseItem(int levelId, Map<Integer, Integer> unlockedItemLimit, HumanPlayerId humanPlayerId, String name, DecimalPosition position) {
        if (serverConnection != null) {
            serverConnection.createHumanBaseWithBaseItem(position);
        } else {
            if (name.equals("")) {
                name = null;
            }
            playerBase = baseItemService.createHumanBaseWithBaseItem(levelId, unlockedItemLimit, humanPlayerId, name, position);
        }
    }

    public void start() {
        planetService.start();
        perfmonService.start(gameSessionUuid);
        // planetService.enableTracking(true);
    }

    public void stop() {
        try {
            perfmonService.stop();
            botService.killAllBots();
            planetService.stop();
            userContext = null;
            playerBase = null;
            killed.clear();
            removed.clear();
            xpFromKills = 0;
            sendTickUpdate = false;
            if (serverConnection != null) {
                serverConnection.close();
                serverConnection = null;
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
        sendToClient(GameEngineControlPackage.Command.STOP_RESPONSE);
    }

    private void activateQuest(HumanPlayerId humanPlayerId, QuestConfig questConfig) {
        questService.activateCondition(humanPlayerId, questConfig);
        onQuestProgressUpdate(humanPlayerId, questService.getQuestProgressInfo(humanPlayerId));
    }

    @Override
    public void onPostTick() {
        if (!sendTickUpdate) {
            return;
        }
        sendTickUpdate = false;
        try {
            List<SyncBaseItemSimpleDto> syncItems = new ArrayList<>();
            syncItemContainerService.iterateOverItems(true, true, null, syncItem -> {
                if (syncItem instanceof SyncBaseItem) {
                    SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                    SyncBaseItemSimpleDto simpleDto = syncBaseItem.createSyncBaseItemSimpleDto();
                    syncItems.add(simpleDto);
                }
                return null;
            });
            GameInfo gameInfo = new GameInfo();
            if (gameEngineMode == GameEngineMode.SLAVE) {
                gameInfo.setXpFromKills(xpFromKills);
            }
            xpFromKills = 0;
            List<SyncBaseItemSimpleDto> tmpKilled = killed;
            killed = new ArrayList<>();
            List<SyncBaseItemSimpleDto> tmpRemoved = removed;
            removed = new ArrayList<>();
            if (playerBase != null) {
                gameInfo.setHouseSpace(PlayerBaseFull.HOUSE_SPACE);
                gameInfo.setResources((int) playerBase.getResources());
            }
            sendToClient(GameEngineControlPackage.Command.TICK_UPDATE_RESPONSE, syncItems, gameInfo, tmpRemoved, tmpKilled);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            sendToClient(GameEngineControlPackage.Command.TICK_UPDATE_RESPONSE_FAIL);
        }
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        SyncResourceItemSimpleDto syncResourceItemSimpleDto = new SyncResourceItemSimpleDto();
        syncResourceItemSimpleDto.setId(syncResourceItem.getId());
        syncResourceItemSimpleDto.setItemTypeId(syncResourceItem.getItemType().getId());
        syncResourceItemSimpleDto.setPosition2d(syncResourceItem.getSyncPhysicalArea().getPosition2d());
        syncResourceItemSimpleDto.setPosition3d(syncResourceItem.getSyncPhysicalArea().getPosition3d());
        syncResourceItemSimpleDto.setModel(syncResourceItem.getSyncPhysicalArea().getModelMatrices());
        sendToClient(GameEngineControlPackage.Command.RESOURCE_CREATED, syncResourceItemSimpleDto);
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onResourceCreated(syncResourceItem);
        }
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        sendToClient(GameEngineControlPackage.Command.RESOURCE_DELETED, syncResourceItem.getId());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncItemDeleted(syncResourceItem, false);
        }
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        SyncBoxItemSimpleDto syncBoxItemSimpleDto = new SyncBoxItemSimpleDto();
        syncBoxItemSimpleDto.setId(syncBoxItem.getId());
        syncBoxItemSimpleDto.setItemTypeId(syncBoxItem.getItemType().getId());
        syncBoxItemSimpleDto.setPosition2d(syncBoxItem.getSyncPhysicalArea().getPosition2d());
        syncBoxItemSimpleDto.setPosition3d(syncBoxItem.getSyncPhysicalArea().getPosition3d());
        syncBoxItemSimpleDto.setModel(syncBoxItem.getSyncPhysicalArea().getModelMatrices());
        sendToClient(GameEngineControlPackage.Command.BOX_CREATED, syncBoxItemSimpleDto);
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onBoxCreated(syncBoxItem);
        }
    }

    @Override
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        if (userContext.getHumanPlayerId().equals(humanPlayerId)) {
            sendToClient(GameEngineControlPackage.Command.BOX_PICKED, boxContent);
        }
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem syncBoxItem) {
        sendToClient(GameEngineControlPackage.Command.BOX_DELETED, syncBoxItem.getId());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncItemDeleted(syncBoxItem, false);
        }
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_IDLE, syncBaseItem.createSyncBaseItemSimpleDto());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(syncBaseItem);
        }
    }

    @Override
    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        if (workerTrackerHandler != null) {
            if (currentBuildup != null) {
                workerTrackerHandler.onSyncBaseItem(currentBuildup);
            }
            workerTrackerHandler.onSyncBaseItem(syncBaseItem);
        }
    }

    @Override
    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(createdBy);
            workerTrackerHandler.onSyncBaseItem(syncBaseItem);
        }
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_START_SPAWNED, syncBaseItem.createSyncBaseItemSimpleDto());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(syncBaseItem);
        }
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        if (playerBase.getHumanPlayerId() != null && playerBase.getHumanPlayerId().equals(userContext.getHumanPlayerId())) {
            this.playerBase = playerBase;
        }
        sendBaseToClient(GameEngineControlPackage.Command.BASE_CREATED, playerBase);
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onBaseCreated(playerBase);
        }
    }

    @Override
    public void onBaseSlaveCreated(PlayerBase playerBase) {
        sendBaseToClient(GameEngineControlPackage.Command.BASE_CREATED, playerBase);
    }

    public void onServerBaseCreated(PlayerBaseInfo playerBaseInfo) {
        baseItemService.createBaseSlave(playerBaseInfo);
        if (playerBaseInfo.getHumanPlayerId() != null && playerBaseInfo.getHumanPlayerId().equals(userContext.getHumanPlayerId())) {
            this.playerBase = baseItemService.getPlayerBase4BaseId(playerBaseInfo.getBaseId());
        }
    }

    public void onServerBaseDeleted(int baseId) {
        baseItemService.deleteBaseSlave(baseId);
    }

    public void onServerBaseNameChanged(PlayerBaseInfo playerBaseInfo) {
        PlayerBase playerBase = baseItemService.changeBaseNameChanged(playerBaseInfo.getBaseId(), playerBaseInfo.getName());
        sendBaseToClient(GameEngineControlPackage.Command.BASE_UPDATED, playerBase);
    }

    private void sendBaseToClient(GameEngineControlPackage.Command cmd, PlayerBase playerBase) {
        PlayerBaseDto playerBaseDto = new PlayerBaseDto();
        playerBaseDto.setBaseId(playerBase.getBaseId());
        playerBaseDto.setName(playerBase.getName());
        playerBaseDto.setCharacter(playerBase.getCharacter());
        playerBaseDto.setHumanPlayerId(playerBase.getHumanPlayerId());
        playerBaseDto.setBotId(playerBase.getBotId());
        sendToClient(cmd, playerBaseDto);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actorBase) {
        if (playerBase.getHumanPlayerId() != null && playerBase.getHumanPlayerId().equals(userContext.getHumanPlayerId())) {
            this.playerBase = null;
        }
        sendToClient(GameEngineControlPackage.Command.BASE_DELETED, playerBase.getBaseId());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onBaseDeleted(playerBase);
        }
    }

    @Override
    public void onQuestPassed(HumanPlayerId humanPlayerId, QuestConfig questConfig) {
        if (userContext.getHumanPlayerId().equals(humanPlayerId)) {
            sendToClient(GameEngineControlPackage.Command.QUEST_PASSED);
        }
    }

    public UserContext getUserContext() {
        return userContext;
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem target, SyncBaseItem actor) {
        killed.add(target.createSyncBaseItemSimpleDto());
        if (actor.getBase().getHumanPlayerId() != null && actor.getBase().getHumanPlayerId().equals(userContext.getHumanPlayerId())) {
            xpFromKills += target.getBaseItemType().getXpOnKilling();
        }
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncItemDeleted(target, true);
        }
    }

    @Override
    public void onSyncBaseItemKilledSlave(SyncBaseItem target) {
        killed.add(target.createSyncBaseItemSimpleDto());
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        removed.add(syncBaseItem.createSyncBaseItemSimpleDto());
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncItemDeleted(syncBaseItem, false);
        }
    }

    @Override
    public void onProjectileFired(int baseItemTypeId, Vertex muzzlePosition, Vertex target) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_FIRED, baseItemTypeId, muzzlePosition, target);
    }

    @Override
    public void onProjectileDetonation(int baseItemTypeId, Vertex position) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_DETONATION, baseItemTypeId, position);
    }

    @Override
    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        if (serverConnection != null) {
            serverConnection.onCommandSent(baseCommand);
        }
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(syncItem);
        }
    }

    @Override
    public void onSyncItemLoaded(SyncBaseItem container, SyncBaseItem contained) {
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(container);
            workerTrackerHandler.onSyncBaseItem(contained);
        }
    }

    @Override
    public void onSyncItemContainerUnloaded(SyncBaseItem container) {
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(container);
        }
    }

    @Override
    public void onSyncItemUnloaded(SyncBaseItem contained) {
        if (workerTrackerHandler != null) {
            workerTrackerHandler.onSyncBaseItem(contained);
        }
    }

    private void onPerfmonRequest() {
        sendToClient(GameEngineControlPackage.Command.PERFMON_RESPONSE, perfmonService.getPerfmonStatistics(-1));
    }

    private void getTerrainZ(DecimalPosition position) {
        try {
            double z = terrainService.getSurfaceAccess().getInterpolatedZ(position);
            sendToClient(GameEngineControlPackage.Command.SINGLE_Z_TERRAIN_ANSWER, position, z);
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning("GameEngineWorker.getTerrainZ() " + e.getMessage());
            sendToClient(GameEngineControlPackage.Command.SINGLE_Z_TERRAIN_ANSWER_FAIL, position);
        }
    }

    private void getTerrainTile(Index terrainTileIndex) {
        TerrainTile terrainTile = terrainService.generateTerrainTile(terrainTileIndex);
        sendToClient(GameEngineControlPackage.Command.TERRAIN_TILE_RESPONSE, terrainTile);
    }

    private void sellItems(List<Integer> items) {
        if (gameEngineMode == GameEngineMode.MASTER) {
            baseItemService.sellItems(items, playerBase);
        } else if (gameEngineMode == GameEngineMode.SLAVE) {
            if (serverConnection != null) {
                serverConnection.sellItems(items);
            }
        } else {
            throw new IllegalStateException("GameEngineWorker.sellItems() illegal gameEngineMode: " + gameEngineMode);
        }
    }

    private void useInventoryItem(UseInventoryItem useInventoryItem) {
        if (gameEngineMode == GameEngineMode.MASTER) {
            baseItemService.useInventoryItem(useInventoryItem, (PlayerBaseFull) playerBase);
        } else if (gameEngineMode == GameEngineMode.SLAVE) {
            if (serverConnection != null) {
                serverConnection.useInventoryItem(useInventoryItem);
            }
        } else {
            throw new IllegalStateException("GameEngineWorker.useInventoryItem() illegal gameEngineMode: " + gameEngineMode);
        }
    }

    public void onServerSyncItemDeleted(SyncItemDeletedInfo syncItemDeletedInfo) {
        SyncItem syncItem = syncItemContainerService.getSyncItem(syncItemDeletedInfo.getId());
        if (syncItem instanceof SyncBaseItem) {
            baseItemService.onSlaveSyncBaseItemDeleted((SyncBaseItem) syncItem, syncItemDeletedInfo);
        } else if (syncItem instanceof SyncResourceItem) {
            resourceService.removeSyncResourceItem((SyncResourceItem) syncItem);
        } else if (syncItem instanceof SyncBoxItem) {
            boxService.removeSyncBoxSlave((SyncBoxItem) syncItem);
        } else {
            throw new IllegalArgumentException("GameEngineWorker.onServerSyncItemDeleted(): unknown type: " + syncItem + " syncItemDeletedInfo: " + syncItemDeletedInfo);
        }
    }

    private void updateLevel(int levelId) {
        userContext.setLevelId(levelId);
        baseItemService.updateLevel(userContext.getHumanPlayerId(), levelId);
    }

    public void onPlayerBaseTracking(PlayerBaseTracking playerBaseTracking) {
        if (playerBaseTracking.getPlayerBaseInfo() != null) {
            onServerBaseCreated(playerBaseTracking.getPlayerBaseInfo());
        } else if (playerBaseTracking.getDeletedBaseId() != null) {
            onServerBaseDeleted(playerBaseTracking.getDeletedBaseId());
        } else {
            throw new IllegalArgumentException("GameEngineWorker.onPlayerBaseTracking() invalid input");
        }
    }

    @Override
    public void onQuestProgressUpdate(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        sendToClient(GameEngineControlPackage.Command.QUEST_PROGRESS, questProgressInfo);
    }

    @Override
    public void onEnergyStateChanged(PlayerBase base, int consuming, int generating) {
        if (playerBase != null && playerBase.equals(base)) {
            sendToClient(GameEngineControlPackage.Command.ENERGY_CHANGED, consuming, generating);
        }
    }

    public void updateResourceSlave(Integer resources) {
        if (playerBase != null) {
            playerBase.setResources(resources);
        }
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }
}
