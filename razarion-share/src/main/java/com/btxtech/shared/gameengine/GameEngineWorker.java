package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.ResourceItemPosition;
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
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.IntIntMap;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
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
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SynchronizationSendingContext;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.utils.ExceptionUtil;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.07.2016.
 */
public abstract class GameEngineWorker implements PlanetTickListener, QuestListener, GameLogicListener {

    private final Logger logger = Logger.getLogger(GameEngineWorker.class.getName());
    private final List<SyncBaseItem> killedSyncBaseItems = new ArrayList<>();
    private final List<Integer> removedSyncBaseItemIds = new ArrayList<>();
    private final PlanetService planetService;
    private final InitializeService initializeService;
    private final BotService botService;
    private final ResourceService resourceService;
    private final BaseItemService baseItemService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final QuestService questService;
    private final BoxService boxService;
    private final CommandService commandService;
    private final PerfmonService perfmonService;
    private final TerrainService terrainService;
    private final Provider<AbstractServerGameConnection> connectionInstance;
    private UserContext userContext;
    private PlayerBase playerBase;
    private int xpFromKills;
    private boolean sendTickUpdate;
    private AbstractServerGameConnection serverConnection;
    private GameEngineMode gameEngineMode;
    private String gameSessionUuid;

    public GameEngineWorker(Provider<AbstractServerGameConnection> connectionInstance,
                            TerrainService terrainService,
                            PerfmonService perfmonService,
                            GameLogicService logicService,
                            CommandService commandService,
                            BoxService boxService,
                            QuestService questService,
                            SyncItemContainerServiceImpl syncItemContainerService,
                            BaseItemService baseItemService,
                            ResourceService resourceService,
                            BotService botService,
                            InitializeService initializeService,
                            PlanetService planetService) {
        this.connectionInstance = connectionInstance;
        this.terrainService = terrainService;
        this.perfmonService = perfmonService;
        this.commandService = commandService;
        this.boxService = boxService;
        this.questService = questService;
        this.syncItemContainerService = syncItemContainerService;
        this.baseItemService = baseItemService;
        this.resourceService = resourceService;
        this.botService = botService;
        this.initializeService = initializeService;
        this.planetService = planetService;
        questService.addQuestListener(this);
        logicService.setGameLogicListener(this);
    }

    protected abstract void sendToClient(GameEngineControlPackage.Command command, Object... object);

    protected abstract int[] convertIntArray(int[] intArray);

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                initialise((StaticGameConfig) controlPackage.getData(0), (PlanetConfig) controlPackage.getData(1), (UserContext) controlPackage.getData(2), (GameEngineMode) controlPackage.getData(3), (String) controlPackage.getData(4));
                break;
            case INITIALIZE_WARM:
                initialiseWarm((PlanetConfig) controlPackage.getData(0), (UserContext) controlPackage.getData(1), (GameEngineMode) controlPackage.getData(2), (String) controlPackage.getData(3));
                break;
            case START:
                start((String) controlPackage.getData(0));
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
                createHumanBaseWithBaseItem((Integer) controlPackage.getData(0), (IntIntMap) controlPackage.getData(1), (String) controlPackage.getData(2), (String) controlPackage.getData(3), (DecimalPosition) controlPackage.getData(4));
                break;
            case USE_INVENTORY_ITEM:
                useInventoryItem((UseInventoryItem) controlPackage.getData(0));
                break;
            case CREATE_BOXES:
                boxService.dropBoxes((List<BoxItemPosition>) controlPackage.getSingleData());
                break;
            case ACTIVATE_QUEST:
                activateQuest(userContext.getUserId(), (QuestConfig) controlPackage.getData(0));
                break;
            case COMMAND_ATTACK:
                commandService.attack((IdsDto) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_FINALIZE_BUILD:
                commandService.finalizeBuild((IdsDto) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_BUILD:
                commandService.build((int) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1), (int) controlPackage.getData(2));
                break;
            case COMMAND_FABRICATE:
                commandService.fabricate((int) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_HARVEST:
                commandService.harvest((IdsDto) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_MOVE:
                commandMove((IdsDto) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1));
                break;
            case COMMAND_PICK_BOX:
                commandService.pickupBox((IdsDto) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_LOAD_CONTAINER:
                commandService.loadContainer((IdsDto) controlPackage.getData(0), (int) controlPackage.getData(1));
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
            case TERRAIN_TILE_REQUEST:
                getTerrainTile((Index) controlPackage.getData(0));
                break;
            case SELL_ITEMS:
                sellItems((IdsDto) controlPackage.getData(0));
                break;
            case GET_TERRAIN_TYPE:
                getTerrainType((Index) controlPackage.getData(0));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }

    private void commandMove(IdsDto syncBaseItemIds, DecimalPosition destination) {
        try {
            commandService.move(syncBaseItemIds, destination);
            sendToClient(GameEngineControlPackage.Command.COMMAND_MOVE_ACK);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.commandMove() failed", t);
            sendToClient(GameEngineControlPackage.Command.COMMAND_MOVE_ACK);
        }
    }

    private void initialise(StaticGameConfig staticGameConfig, PlanetConfig planetConfig, UserContext userContext, GameEngineMode gameEngineMode, String gameSessionUuid) {
        try {
            this.gameSessionUuid = gameSessionUuid;
            initializeService.setStaticGameConfig(staticGameConfig);
            planetService.addTickListener(this);
            initWarmInternal(planetConfig, userContext, gameEngineMode,
                    () -> sendToClient(GameEngineControlPackage.Command.INITIALIZED),
                    failString -> sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, failString));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.initialise()", t);
            sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, ExceptionUtil.setupStackTrace(null, t));
        }
    }

    private void initialiseWarm(PlanetConfig planetConfig, UserContext userContext, GameEngineMode gameEngineMode, String gameSessionUuid) {
        try {
            this.gameSessionUuid = gameSessionUuid;
            initWarmInternal(planetConfig, userContext, gameEngineMode, () -> {
                sendToClient(GameEngineControlPackage.Command.INITIALIZED);
            }, failString -> {
                sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, failString);
            });
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.initialiseWarm()", t);
            sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, ExceptionUtil.setupStackTrace(null, t));
        }
    }

    private void initWarmInternal(PlanetConfig planetConfig, UserContext userContext, GameEngineMode gameEngineMode, Runnable finishCallback, Consumer<String> failCallback) {
        this.gameEngineMode = gameEngineMode;
        this.userContext = userContext;
        planetService.initialise(planetConfig, gameEngineMode, null, finishCallback, failCallback);
    }

    public void onInitialSlaveSyncItemInfo(InitialSlaveSyncItemInfo initialSlaveSyncItemInfo) {
        planetService.initialSlaveSyncItemInfo(initialSlaveSyncItemInfo);
        if (initialSlaveSyncItemInfo.getActualBaseId() != null) {
            playerBase = baseItemService.getPlayerBase4BaseId(initialSlaveSyncItemInfo.getActualBaseId());
        }
        serverConnection.tickCountRequest();
        DecimalPosition basePosition = findScrollToBasePosition();
        if (basePosition != null) {
            sendToClient(GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED, basePosition);
        } else {
            // Marshaller can not handle null value
            sendToClient(GameEngineControlPackage.Command.INITIAL_SLAVE_SYNCHRONIZED_NO_BASE);
        }
    }

    private DecimalPosition findScrollToBasePosition() {
        if (playerBase == null) {
            return null;
        }
        final SingleHolder<DecimalPosition> factoryPosition = new SingleHolder<>();
        final SingleHolder<DecimalPosition> builderPosition = new SingleHolder<>();
        final SingleHolder<DecimalPosition> unitPosition = new SingleHolder<>();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getBase().equals(playerBase)) {
                return null;
            }
            BaseItemType baseItemType = syncBaseItem.getBaseItemType();
            if (baseItemType.getFactoryType() != null) {
                factoryPosition.setO(syncBaseItem.getAbstractSyncPhysical().getPosition());
                return true;
            }
            if (baseItemType.getBuilderType() != null) {
                builderPosition.setO(syncBaseItem.getAbstractSyncPhysical().getPosition());
            }
            unitPosition.setO(syncBaseItem.getAbstractSyncPhysical().getPosition());
            return null;
        });

        if (!factoryPosition.isEmpty()) {
            return factoryPosition.getO();
        }
        if (!builderPosition.isEmpty()) {
            return builderPosition.getO();
        }
        return unitPosition.getO();
    }

    private void createHumanBaseWithBaseItem(int levelId, IntIntMap unlockedItemLimit, String userId, String name, DecimalPosition position) {
        if (serverConnection != null) {
            serverConnection.createHumanBaseWithBaseItem(position);
        } else {
            if (name.isEmpty()) {
                name = null;
            }
            playerBase = baseItemService.createHumanBaseWithBaseItem(levelId, unlockedItemLimit.getMap(), userId, name, position);
        }
    }

    public void start(String bearerToken) {
        planetService.enableTracking(false);
        planetService.start();
        perfmonService.start(gameSessionUuid);
        if (gameEngineMode == GameEngineMode.SLAVE) {
            serverConnection = connectionInstance.get();
            serverConnection.init(bearerToken);
        }
    }

    public void stop() {
        try {
            perfmonService.stop();
            botService.killAllBots();
            planetService.stop();
            userContext = null;
            playerBase = null;
            killedSyncBaseItems.clear();
            removedSyncBaseItemIds.clear();
            xpFromKills = 0;
            sendTickUpdate = false;
            if (serverConnection != null) {
                serverConnection.close();
                serverConnection = null;
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.stop() failed", t);
        }
        sendToClient(GameEngineControlPackage.Command.STOP_RESPONSE);
    }

    private void activateQuest(String userId, QuestConfig questConfig) {
        questService.activateCondition(userId, questConfig);
        onQuestProgressUpdate(userId, questService.getQuestProgressInfo(userId));
    }

    @Override
    public void onPostTick(SynchronizationSendingContext synchronizationSendingContext) {
        if (!sendTickUpdate) {
            return;
        }
        sendTickUpdate = false;
        try {
            NativeTickInfo nativeTickInfo = new NativeTickInfo();
            List<NativeSyncBaseItemTickInfo> tmp = new ArrayList<>();
            TerrainAnalyzer terrainAnalyzer = terrainService.getTerrainAnalyzer();
            syncItemContainerService.iterateOverItems(true, true, null, syncItem -> {
                try {
                    if (syncItem instanceof SyncBaseItem) {
                        SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                        tmp.add(syncBaseItem.createNativeSyncBaseItemTickInfo(terrainAnalyzer));
                    }
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "onPostTick failed syncItem: " + syncItem, t);
                }
                return null;
            });
            nativeTickInfo.updatedNativeSyncBaseItemTickInfos = tmp.stream().toArray(value -> new NativeSyncBaseItemTickInfo[tmp.size()]);
            if (gameEngineMode == GameEngineMode.SLAVE) {
                nativeTickInfo.xpFromKills = xpFromKills;
            }
            xpFromKills = 0;
            nativeTickInfo.killedSyncBaseItems = convertAndClearKilled();
            nativeTickInfo.removeSyncBaseItemIds = convertAndClearRemoved();
            if (playerBase != null) {
                nativeTickInfo.resources = (int) playerBase.getResources();
            }
            sendToClient(GameEngineControlPackage.Command.TICK_UPDATE_RESPONSE, nativeTickInfo);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.onPostTick() failed", t);
            sendToClient(GameEngineControlPackage.Command.TICK_UPDATE_RESPONSE_FAIL);
        }
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        SyncResourceItemSimpleDto syncResourceItemSimpleDto = new SyncResourceItemSimpleDto();
        syncResourceItemSimpleDto.setId(syncResourceItem.getId());
        syncResourceItemSimpleDto.setItemTypeId(syncResourceItem.getItemType().getId());
        syncResourceItemSimpleDto.setPosition(terrainService.getTerrainAnalyzer().toPosition3d(syncResourceItem.getAbstractSyncPhysical().getPosition()));
        sendToClient(GameEngineControlPackage.Command.RESOURCE_CREATED, syncResourceItemSimpleDto);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        sendToClient(GameEngineControlPackage.Command.RESOURCE_DELETED, syncResourceItem.getId());
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        SyncBoxItemSimpleDto syncBoxItemSimpleDto = new SyncBoxItemSimpleDto();
        syncBoxItemSimpleDto.setId(syncBoxItem.getId());
        syncBoxItemSimpleDto.setItemTypeId(syncBoxItem.getItemType().getId());
        syncBoxItemSimpleDto.setPosition(terrainService.getTerrainAnalyzer().toPosition3d(syncBoxItem.getAbstractSyncPhysical().getPosition()));
        sendToClient(GameEngineControlPackage.Command.BOX_CREATED, syncBoxItemSimpleDto);
    }

    @Override
    public void onBoxPicked(String userId, BoxContent boxContent) {
        if (userContext.getUserId().equals(userId)) {
            sendToClient(GameEngineControlPackage.Command.BOX_PICKED, boxContent);
        }
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem syncBoxItem) {
        sendToClient(GameEngineControlPackage.Command.BOX_DELETED, syncBoxItem.getId());
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_IDLE, syncBaseItem.createNativeSyncBaseItemTickInfo(terrainService.getTerrainAnalyzer()));
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_START_SPAWNED, syncBaseItem.createNativeSyncBaseItemTickInfo(terrainService.getTerrainAnalyzer()));
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        if (playerBase.getUserId() != null && playerBase.getUserId().equals(userContext.getUserId())) {
            this.playerBase = playerBase;
        }
        sendBaseToClient(GameEngineControlPackage.Command.BASE_CREATED, playerBase);
    }

    @Override
    public void onBaseSlaveCreated(PlayerBase playerBase) {
        sendBaseToClient(GameEngineControlPackage.Command.BASE_CREATED, playerBase);
    }

    public void onServerBaseCreated(PlayerBaseInfo playerBaseInfo) {
        baseItemService.createBaseSlave(playerBaseInfo);
        if (playerBaseInfo.getUserId() != null && playerBaseInfo.getUserId().equals(userContext.getUserId())) {
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

    public void onServerBaseHumanPlayerIdChanged(PlayerBaseInfo playerBaseInfo) {
        PlayerBase playerBase = baseItemService.updateHumanPlayerId(playerBaseInfo.getBaseId(), playerBaseInfo.getUserId());
        sendBaseToClient(GameEngineControlPackage.Command.BASE_UPDATED, playerBase);
    }

    private void sendBaseToClient(GameEngineControlPackage.Command cmd, PlayerBase playerBase) {
        PlayerBaseDto playerBaseDto = new PlayerBaseDto();
        playerBaseDto.setBaseId(playerBase.getBaseId());
        playerBaseDto.setName(playerBase.getName());
        playerBaseDto.setCharacter(playerBase.getCharacter());
        playerBaseDto.setUserId(playerBase.getUserId());
        playerBaseDto.setBotId(playerBase.getBotId());
        sendToClient(cmd, playerBaseDto);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actorBase) {
        if (playerBase.getUserId() != null && playerBase.getUserId().equals(userContext.getUserId())) {
            this.playerBase = null;
        }
        sendToClient(GameEngineControlPackage.Command.BASE_DELETED, playerBase.getBaseId());
    }

    @Override
    public void onQuestPassed(String userId, QuestConfig questConfig) {
        if (userContext.getUserId().equals(userId)) {
            sendToClient(GameEngineControlPackage.Command.QUEST_PASSED);
        }
    }

    public UserContext getUserContext() {
        return userContext;
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem target, SyncBaseItem actor) {
        killedSyncBaseItems.add(target);
        if (actor.getBase().getUserId() != null && actor.getBase().getUserId().equals(userContext.getUserId())) {
            xpFromKills += target.getBaseItemType().getXpOnKilling();
        }
    }

    @Override
    public void onSyncBaseItemKilledSlave(SyncBaseItem target) {
        killedSyncBaseItems.add(target);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        removedSyncBaseItemIds.add(syncBaseItem.getId());
    }

    @Override
    public void onProjectileFired(SyncBaseItem syncBaseItem, DecimalPosition target) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_FIRED, syncBaseItem.getId(), target);
    }

    @Override
    public void onProjectileDetonation(int baseItemTypeId, DecimalPosition position) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_DETONATION, baseItemTypeId, position);
    }

    @Override
    public void onSlaveCommandSent(SyncBaseItem syncBaseItemSave, BaseCommand baseCommand) {
        if (serverConnection != null) {
            serverConnection.onCommandSent(baseCommand);
        }
    }

    private void onPerfmonRequest() {
        sendToClient(GameEngineControlPackage.Command.PERFMON_RESPONSE, perfmonService.peekClientPerfmonStatistics());
    }

    private void getTerrainTile(Index terrainTileIndex) {
        long time = System.currentTimeMillis();
        TerrainTile terrainTile = terrainService.generateTerrainTile(terrainTileIndex);
        sendToClient(GameEngineControlPackage.Command.TERRAIN_TILE_RESPONSE, terrainTile);
        perfmonService.onTerrainTile(terrainTileIndex, System.currentTimeMillis() - time);
    }

    private void sellItems(IdsDto items) {
        if (gameEngineMode == GameEngineMode.MASTER) {
            baseItemService.sellItems(items.getIds(), playerBase);
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
        userContext.levelId(levelId);
        baseItemService.updateLevel(userContext.getUserId(), levelId);
    }

    @Override
    public void onQuestProgressUpdate(String userId, QuestProgressInfo questProgressInfo) {
        if (questProgressInfo != null) {
            sendToClient(GameEngineControlPackage.Command.QUEST_PROGRESS, questProgressInfo);
        }
    }

    @Override
    public void onEnergyStateChanged(PlayerBase base, int consuming, int generating) {
        if (playerBase != null && playerBase.equals(base)) {
            sendToClient(GameEngineControlPackage.Command.ENERGY_CHANGED, consuming, generating);
        }
    }

    public void onConnectionLost() {
        sendToClient(GameEngineControlPackage.Command.CONNECTION_LOST);
    }

    public void updateResourceSlave(Integer resources) {
        if (playerBase != null) {
            playerBase.setResources(resources);
        }
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    private NativeSimpleSyncBaseItemTickInfo[] convertAndClearKilled() {
        if (killedSyncBaseItems.isEmpty()) {
            return null;
        }
        NativeSimpleSyncBaseItemTickInfo[] killed = killedSyncBaseItems.stream().map(item -> {
            NativeSimpleSyncBaseItemTickInfo nativeSimpleSyncBaseItemTickInfo = new NativeSimpleSyncBaseItemTickInfo();
            nativeSimpleSyncBaseItemTickInfo.id = item.getId();
            nativeSimpleSyncBaseItemTickInfo.itemTypeId = item.getBaseItemType().getId();
            nativeSimpleSyncBaseItemTickInfo.contained = item.isContainedIn();
            if (!nativeSimpleSyncBaseItemTickInfo.contained) {
                nativeSimpleSyncBaseItemTickInfo.x = item.getAbstractSyncPhysical().getPosition().getX();
                nativeSimpleSyncBaseItemTickInfo.y = item.getAbstractSyncPhysical().getPosition().getY();
            }
            return nativeSimpleSyncBaseItemTickInfo;
        }).toArray(value -> new NativeSimpleSyncBaseItemTickInfo[killedSyncBaseItems.size()]);
        killedSyncBaseItems.clear();
        return killed;
    }

    private int[] convertAndClearRemoved() {
        if (removedSyncBaseItemIds.isEmpty()) {
            return null;
        }
        int[] removedIds = removedSyncBaseItemIds.stream().mapToInt(integer -> integer).toArray();
        removedSyncBaseItemIds.clear();
        return convertIntArray(removedIds);
    }


    private void getTerrainType(Index nodeIndex) {
        try {
            sendToClient(GameEngineControlPackage.Command.GET_TERRAIN_TYPE_ANSWER,
                    nodeIndex,
                    terrainService.getTerrainAnalyzer().getTerrainType(nodeIndex));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "GameEngineWorker.getTerrainType() failed", t);
            sendToClient(GameEngineControlPackage.Command.GET_TERRAIN_TYPE_ANSWER, nodeIndex, TerrainType.BLOCKED);

        }
    }
}
