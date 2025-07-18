package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.NotYourBaseException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainTypeNotAllowedException;
import com.btxtech.shared.utils.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton // Rename to BaseService
public class BaseItemService {
    private static final double ITEM_SELL_FACTOR = 0.5;
    private final Logger logger = Logger.getLogger(BaseItemService.class.getName());
    private final GameLogicService gameLogicService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final LevelService levelService;
    private final ItemTypeService itemTypeService;
    private final EnergyService energyService;
    private final InventoryTypeService inventoryTypeService;
    private final BoxService boxService;
    private final TerrainService terrainService;
    private final GuardingItemService guardingItemService;
    private final SyncService syncService;
    private final Map<Integer, PlayerBase> bases = new HashMap<>();
    private final Collection<SyncBaseItem> activeItems = new ArrayList<>();
    private final Collection<SyncBaseItem> activeItemQueue = new ArrayList<>();
    private final Queue<BaseCommand> commandQueue = new LinkedList<>();
    private final PriorityQueue<TickInfo> pendingReceivedTickInfos = new PriorityQueue<>(Comparator.comparingDouble(TickInfo::getTickCount));
    private int lastBaseItId = 1;
    private PlanetConfig planetConfig;
    private GameEngineMode gameEngineMode;

    @Inject
    public BaseItemService(SyncService syncService,
                           GuardingItemService guardingItemService,
                           TerrainService terrainService,
                           BoxService boxService,
                           InventoryTypeService inventoryTypeService,
                           EnergyService energyService,
                           ItemTypeService itemTypeService,
                           LevelService levelService,
                           SyncItemContainerServiceImpl syncItemContainerService,
                           GameLogicService gameLogicService,
                           InitializeService initializeService) {
        this.syncService = syncService;
        this.guardingItemService = guardingItemService;
        this.terrainService = terrainService;
        this.boxService = boxService;
        this.inventoryTypeService = inventoryTypeService;
        this.energyService = energyService;
        this.itemTypeService = itemTypeService;
        this.levelService = levelService;
        this.syncItemContainerService = syncItemContainerService;
        this.gameLogicService = gameLogicService;

        initializeService.receivePlanetActivationEvent(this::onPlanetActivation);
    }

    private void onPlanetActivation(PlanetActivationEvent planetActivationEvent) {
        activeItems.clear();
        activeItemQueue.clear();
        bases.clear();
        pendingReceivedTickInfos.clear();
        guardingItemService.init(planetActivationEvent.getGameEngineMode());
        lastBaseItId = 1;
        if (planetActivationEvent.getType() == PlanetActivationEvent.Type.INITIALIZE) {
            gameEngineMode = planetActivationEvent.getGameEngineMode();
            planetConfig = planetActivationEvent.getPlanetConfig();

        }
    }

    public void setupSlave(InitialSlaveSyncItemInfo initialSlaveSyncItemInfo) {
        for (PlayerBaseInfo playerBaseInfo : initialSlaveSyncItemInfo.getPlayerBaseInfos()) {
            try {
                createBaseSlave(playerBaseInfo);
            } catch (Throwable t) {
                logger.log(Level.WARNING, t.getMessage(), t);
            }
        }

        Map<SyncBaseItem, SyncBaseItemInfo> tmp = new HashMap<>();
        for (SyncBaseItemInfo syncBaseItemInfo : initialSlaveSyncItemInfo.getSyncBaseItemInfos()) {
            try {
                SyncBaseItem syncBaseItem = createSyncBaseItemSlave(syncBaseItemInfo, getPlayerBase4BaseId(syncBaseItemInfo.getBaseId()));
                tmp.put(syncBaseItem, syncBaseItemInfo);
            } catch (Throwable t) {
                logger.log(Level.WARNING, t.getMessage(), t);
            }
        }

        for (Map.Entry<SyncBaseItem, SyncBaseItemInfo> entry : tmp.entrySet()) {
            try {
                synchronizeActivateSlave(entry.getKey(), entry.getValue());
            } catch (Throwable t) {
                logger.log(Level.WARNING, t.getMessage(), t);
            }
        }
    }

    public PlayerBaseFull getFirstHumanBase() {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getCharacter().isHuman()) {
                    return (PlayerBaseFull) playerBase;
                }
            }
        }
        throw new IllegalStateException("No human base found");
    }

    public PlayerBaseFull createHumanBase(int startRazarion, int levelId, Map<Integer, Integer> unlockedItemLimit, String userId, String name) {
        return createBaseMaster(name, Character.HUMAN, startRazarion, levelId, unlockedItemLimit, userId, null);
    }

    private void surrenderHumanBase(String userId) {
        PlayerBaseFull playerBase = getPlayerBaseFull4UserId(userId);
        if (playerBase != null) {
            gameLogicService.onSurrenderBase(playerBase);
            while (!playerBase.getItems().isEmpty()) {
                removeSyncItem(CollectionUtils.getFirst(playerBase.getItems()));
            }
        }
    }

    public PlayerBaseFull createHumanBaseWithBaseItem(int levelId, Map<Integer, Integer> unlockedItemLimit, String userId, String name, DecimalPosition position) {
        surrenderHumanBase(userId);
        PlayerBaseFull playerBase = null;
        try {
            playerBase = createHumanBase(planetConfig.getStartRazarion(), levelId, unlockedItemLimit, userId, name);
            spawnSyncBaseItem(itemTypeService.getBaseItemType(planetConfig.getStartBaseItemTypeId()), position, 0, playerBase, false);
        } catch (Exception e) {
            if (playerBase != null) {
                // If something went wrong with the base create.
                // Prevent user from having a base without any units.
                deleteBaseSlave(playerBase.getBaseId());
            }
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
        return playerBase;
    }

    public void updateLevel(String userId, int levelId) {
        PlayerBaseFull playerBase = getPlayerBaseFull4UserId(userId);
        if (playerBase != null) {
            playerBase.setLevelId(levelId);
        }
    }

    public void updateUnlockedItemLimit(String userId, Map<Integer, Integer> unlockedItemLimit) {
        PlayerBaseFull playerBase = getPlayerBaseFull4UserId(userId);
        if (playerBase != null) {
            playerBase.setUnlockedItemLimit(unlockedItemLimit);
        }
    }

    public PlayerBaseFull createBotBase(BotConfig botConfig) {
        return createBaseMaster(botConfig.getName(), botConfig.isNpc() ? Character.BOT_NCP : Character.BOT, 0, null, null, null, botConfig.getId());
    }

    private PlayerBaseFull createBaseMaster(String name, Character character, int startRazarion, Integer levelId, Map<Integer, Integer> unlockedItemLimit, String userId, Integer botId) {
        PlayerBaseFull playerBase;
        synchronized (bases) {
            lastBaseItId++;
            if (bases.containsKey(lastBaseItId)) {
                throw new IllegalStateException("createBaseMaster: Base with Id already exits: " + lastBaseItId);
            }
            playerBase = new PlayerBaseFull(lastBaseItId, name, character, startRazarion, levelId, unlockedItemLimit, userId, botId);
            bases.put(lastBaseItId, playerBase);
        }
        gameLogicService.onBaseCreated(playerBase);
        return playerBase;
    }

    public void createBaseSlave(PlayerBaseInfo playerBaseInfo) {
        synchronized (bases) {
            if (bases.containsKey(playerBaseInfo.getBaseId())) {
                throw new IllegalStateException("createBaseSlave: Base with Id already exits: " + playerBaseInfo.getBaseId());
            }
            PlayerBase playerBase = new PlayerBase(playerBaseInfo.getBaseId(), playerBaseInfo.getName(), playerBaseInfo.getCharacter(), playerBaseInfo.getResources(), playerBaseInfo.getUserId(), playerBaseInfo.getBotId());
            bases.put(playerBaseInfo.getBaseId(), playerBase);
            gameLogicService.onBaseSlaveCreated(playerBase);
        }
    }

    public void deleteBaseSlave(int baseId) {
        PlayerBase playerBase;
        synchronized (bases) {
            if (!bases.containsKey(baseId)) {
                throw new IllegalStateException("deleteBaseSlave: Base with Id does not exits: " + baseId);
            }
            playerBase = bases.remove(baseId);
            gameLogicService.onBaseRemoved(playerBase);
        }
        energyService.onBaseKilled(playerBase);
    }

    public PlayerBase changeBaseNameChanged(int baseId, String name) {
        synchronized (bases) {
            PlayerBase playerBase = bases.get(baseId);
            if (playerBase == null) {
                throw new IllegalStateException("changeBaseNameChanged: Base with Id does not exits: " + baseId);
            }
            playerBase.updateName(name);
            return playerBase;
        }
    }


    public PlayerBase updateHumanPlayerId(int baseId, String userId) {
        synchronized (bases) {
            PlayerBase playerBase = bases.get(baseId);
            if (playerBase == null) {
                throw new IllegalStateException("changeBaseNameChanged: Base with Id does not exits: " + baseId);
            }
            playerBase.updateUserId(userId);
            return playerBase;
        }
    }

    public SyncBaseItem createSyncBaseItem4Factory(BaseItemType toBeBuilt, DecimalPosition position, double angle, PlayerBaseFull base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(toBeBuilt.getPhysicalAreaConfig().getTerrainType(), position, toBeBuilt.getPhysicalAreaConfig().getRadius())) {
            throw new TerrainTypeNotAllowedException("BaseItemService.createSyncBaseItem4Factory() " + toBeBuilt + " " + position);
        }
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, angle, base);
        syncBaseItem.setSpawnProgress(1.0);
        syncBaseItem.setBuildup(1.0);
        gameLogicService.onFactorySyncItem(syncBaseItem, createdBy);
        return syncBaseItem;
    }

    public SyncBaseItem createSyncBaseItem4Builder(BaseItemType toBeBuilt, DecimalPosition position, PlayerBaseFull base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(toBeBuilt.getPhysicalAreaConfig().getTerrainType(), position, toBeBuilt.getPhysicalAreaConfig().getRadius())) {
            throw new TerrainTypeNotAllowedException("BaseItemService.createSyncBaseItem4Builder() " + toBeBuilt + " " + position);
        }
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, 0, base);

        syncBaseItem.setSpawnProgress(1.0);
        gameLogicService.onBuildingSyncItem(syncBaseItem, createdBy);

        return syncBaseItem;
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem, PlayerBaseFull base) throws ItemLimitExceededException, HouseSpaceExceededException {
        InventoryItem inventoryItem = inventoryTypeService.getInventoryItem(useInventoryItem.getInventoryId());
        if (inventoryItem.getRazarion() != null && inventoryItem.getRazarion() > 0) {
            base.addResource(inventoryItem.getRazarion());
            gameLogicService.onResourcesBalanceChanged(base, (int) base.getResources());
        } else if (inventoryItem.getBaseItemTypeId() != null) {
            if (inventoryItem.getBaseItemTypeCount() != useInventoryItem.getPositions().size()) {
                throw new IllegalArgumentException("BaseItemService.useInventoryItem() inventoryItem.getBaseItemTypeCount() != useInventoryItem.getPositions().size(): " + inventoryItem.getBaseItemTypeCount() + ", " + useInventoryItem.getPositions().size());
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(inventoryItem.getBaseItemTypeId());
            for (DecimalPosition position : useInventoryItem.getPositions()) {
                spawnSyncBaseItem(baseItemType, position, 0, base, false);
            }
        }
    }

    public SyncBaseItem spawnSyncBaseItem(BaseItemType baseItemType, DecimalPosition position, double zRotation, PlayerBaseFull base, boolean noSpawn) throws ItemLimitExceededException, HouseSpaceExceededException {
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(baseItemType.getPhysicalAreaConfig().getTerrainType(), position, baseItemType.getPhysicalAreaConfig().getRadius())) {
            throw new TerrainTypeNotAllowedException("BaseItemService.spawnSyncBaseItem() " + baseItemType + " " + position);
        }
        SyncBaseItem syncBaseItem = createSyncBaseItem(baseItemType, position, zRotation, base);
        syncBaseItem.setBuildup(1.0);

        if (noSpawn) {
            syncBaseItem.setSpawnProgress(1.0);
            syncBaseItem.handleIfItemBecomesReady();
            gameLogicService.onSpawnSyncItemNoSpan(syncBaseItem);
        } else {
            syncService.notifySendSyncBaseItem(syncBaseItem);
            gameLogicService.onSpawnSyncItemStart(syncBaseItem);
        }

        return syncBaseItem;
    }


    public void onTickInfo(long slaveTickCount, TickInfo tickInfo) {
        if (tickInfo.getTickCount() > slaveTickCount) {
            pendingReceivedTickInfos.add(tickInfo);
            return;
        }
        tickInfo.getSyncBaseItemInfos().forEach(this::onSlaveSyncBaseItemChanged);
    }

    public void onSlaveSyncBaseItemChanged(SyncBaseItemInfo syncBaseItemInfo) {
        SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItem(syncBaseItemInfo.getId());
        if (syncBaseItem == null) {
            PlayerBase playerBase = getPlayerBase4BaseId(syncBaseItemInfo.getBaseId());
            syncBaseItem = createSyncBaseItemSlave(syncBaseItemInfo, playerBase);
        }
        synchronizeActivateSlave(syncBaseItem, syncBaseItemInfo);
    }

    private SyncBaseItem createSyncBaseItemSlave(SyncBaseItemInfo syncBaseItemInfo, PlayerBase playerBase) {
        BaseItemType toBeBuilt = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
        SyncBaseItem syncBaseItem = syncItemContainerService.createSyncBaseItemSlave(toBeBuilt, syncBaseItemInfo.getId(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle());
        syncBaseItem.setup(playerBase);
        return syncBaseItem;
    }

    private SyncBaseItem createSyncBaseItemRestore(SyncBaseItemInfo syncBaseItemInfo, PlayerBaseFull playerBase) {
        BaseItemType toBeBuilt = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
        SyncBaseItem syncBaseItem = syncItemContainerService.createSyncBaseItemSlave(toBeBuilt, syncBaseItemInfo.getId(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition(), syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle());
        syncBaseItem.setup(playerBase);
        playerBase.addItem(syncBaseItem);
        return syncBaseItem;
    }

    private void synchronizeActivateSlave(SyncBaseItem syncBaseItem, SyncBaseItemInfo syncBaseItemInfo) {
        syncBaseItem.synchronize(syncBaseItemInfo);
        addToActiveItemQueue(syncBaseItem);
    }

    public void onSlaveSyncBaseItemDeleted(SyncBaseItem syncBaseItem, SyncItemDeletedInfo syncItemDeletedInfo) {
        syncBaseItem.clearHealth();
        syncItemContainerService.destroySyncItem(syncBaseItem);
        energyService.onBaseItemRemoved(syncBaseItem);
        if (syncItemDeletedInfo.isExplode()) {
            gameLogicService.onSyncBaseItemKilledSlave(syncBaseItem);
        } else {
            gameLogicService.onSyncBaseItemRemoved(syncBaseItem);
        }
    }

    private SyncBaseItem createSyncBaseItem(BaseItemType toBeBuilt, DecimalPosition position2d, double zRotation, PlayerBaseFull base) throws ItemLimitExceededException, HouseSpaceExceededException {
        if (!isAlive(base)) {
            throw new BaseDoesNotExistException(base);
        }

        if (base.isAbandoned()) {
            throw new IllegalStateException();
        }

        if (base.getCharacter().isHuman()) {
            checkItemLimit4ItemAdding(toBeBuilt, 1, base);
        }

        // TODO check item free range etc (use: BaseItemPlacerChecker) but not for factory
        SyncBaseItem syncBaseItem = syncItemContainerService.createSyncBaseItem(toBeBuilt, position2d, zRotation);
        syncBaseItem.setup(base);
        base.addItem(syncBaseItem);
        addToActiveItemQueue(syncBaseItem);

        return syncBaseItem;
    }

    public void killSyncItem(SyncBaseItem target, SyncBaseItem actor) {
        if (getGameEngineMode() != GameEngineMode.MASTER) {
            return;
        }
        gameLogicService.onSyncBaseItemKilledMaster(target, actor);
        PlayerBaseFull base = (PlayerBaseFull) target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        energyService.onBaseItemRemoved(target);
        boxService.onSyncBaseItemKilled(target);
        if (target.getSyncItemContainer() != null) {
            target.getSyncItemContainer().getContainedItems().forEach(syncBaseItem -> killContaining(syncBaseItem, actor));
        }
        if (base.getItemCount() == 0) {
            gameLogicService.onBaseKilled(base, actor);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
            energyService.onBaseKilled(base);
        }
    }

    private void killContaining(SyncBaseItem target, SyncBaseItem actor) {
        gameLogicService.onSyncBaseItemKilledMaster(target, actor);
        PlayerBaseFull base = (PlayerBaseFull) target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        energyService.onBaseItemRemoved(target);
    }

    public void removeSyncItem(SyncBaseItem target) {
        target.clearHealth();
        gameLogicService.onSyncBaseItemRemoved(target);
        PlayerBaseFull base = (PlayerBaseFull) target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        energyService.onBaseItemRemoved(target);
        if (base.getItemCount() == 0) {
            gameLogicService.onBaseRemoved(base);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
            energyService.onBaseKilled(base);
        }
    }

    public void sellItems(Collection<Integer> syncBaseItemIds, PlayerBase playerBase) throws ItemDoesNotExistException, NotYourBaseException {
        syncBaseItemIds.forEach(syncBaseItemId -> sellItem(syncBaseItemId, playerBase));
    }

    private void sellItem(int syncBaseItemId, PlayerBase playerBase) throws ItemDoesNotExistException, NotYourBaseException {
        SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(syncBaseItemId);
        if (!syncBaseItem.getBase().equals(playerBase)) {
            throw new NotYourBaseException(playerBase, syncBaseItem.getBase());
        }
        double health = syncBaseItem.getHealth();
        double fullHealth = syncBaseItem.getBaseItemType().getHealth();
        double price = syncBaseItem.getBaseItemType().getPrice();
        double buildup = syncBaseItem.getBuildup();
        double spawnProgress = syncBaseItem.getSpawnProgress();
        double resources = health / fullHealth * buildup * spawnProgress * price * ITEM_SELL_FACTOR;
        syncBaseItem.getBase().addResource(resources);
        gameLogicService.onResourcesBalanceChanged(syncBaseItem.getBase(), (int) syncBaseItem.getBase().getResources());
        removeSyncItem(syncBaseItem);
        // Remove containing
        if (syncBaseItem.getSyncItemContainer() != null) {
            syncBaseItem.getSyncItemContainer().getContainedItems().forEach(this::removeSyncItem);
        }
    }

    public void mgmtDeleteBase(int baseId) {
        Collection<SyncBaseItem> items = new ArrayList<>(((PlayerBaseFull) getPlayerBase4BaseId(baseId)).getItems());
        items.forEach(this::removeSyncItem);
    }

    public void checkItemLimit4ItemAdding(BaseItemType newItemType, int itemCount2Add, PlayerBaseFull simpleBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        if (isLevelLimitation4ItemTypeExceeded(newItemType, itemCount2Add, simpleBase)) {
            throw new ItemLimitExceededException(newItemType, itemCount2Add, simpleBase);
        }
        if (isHouseSpaceExceeded(simpleBase, newItemType)) {
            throw new HouseSpaceExceededException();
        }
    }

    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, int itemCount2Add, PlayerBaseFull playerBase) throws NoSuchItemTypeException {
        if (playerBase.getCharacter().isBot()) {
            return false;
        }
        return getItemCount(playerBase, newItemType) + itemCount2Add > getLimitation4ItemType(newItemType.getId(), playerBase.getLevelId(), playerBase.getUnlockedItemLimit());
    }

    public int getItemCount(PlayerBaseFull playerBase, BaseItemType baseItemType) {
        int count = 0;
        for (SyncBaseItem syncBaseItem : playerBase.getItems()) {
            if (syncBaseItem.getItemType().equals(baseItemType)) {
                count++;
            }
        }
        return count;
    }

    public boolean isAlive(PlayerBase base) {
        return bases.containsKey(base.getBaseId());
    }

    public int getLimitation4ItemType(int itemTypeId, int levelId, Map<Integer, Integer> unlockedItemLimitation) {
        int unlockedCount = unlockedItemLimitation.getOrDefault(itemTypeId, 0);
        int levelCount = levelService.getLevel(levelId).limitation4ItemType(itemTypeId);
        int planetCount = planetConfig.imitation4ItemType(itemTypeId);
        return Math.min(levelCount + unlockedCount, planetCount);
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem1, SyncBaseItem syncBaseItem2) {
        PlayerBase playerBase1 = syncBaseItem1.getBase();
        PlayerBase playerBase2 = syncBaseItem2.getBase();
        return playerBase1.isEnemy(playerBase2);
    }

    public boolean isHouseSpaceExceeded(PlayerBaseFull playerBase, BaseItemType toBeBuiltType) {
        return isHouseSpaceExceeded(playerBase, toBeBuiltType, 1);
    }

    public boolean isHouseSpaceExceeded(PlayerBaseFull playerBase, BaseItemType toBeBuiltType, int itemCount2Add) {
        return playerBase.getUsedHouseSpace() + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > playerBase.getHouseSpace() + planetConfig.getHouseSpace();
    }

    public PlayerBaseFull getPlayerBaseFull4UserId(String userId) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getUserId() != null && Objects.equals(playerBase.getUserId(), userId)) {
                    return (PlayerBaseFull) playerBase;
                }
            }
        }
        return null;
    }

    public PlayerBase getPlayerBase4UserId(String userId) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getUserId() != null && Objects.equals(playerBase.getUserId(), userId)) {
                    return playerBase;
                }
            }
        }
        return null;
    }

    public PlayerBase getPlayerBase4BaseId(int baseId) {
        synchronized (bases) {
            PlayerBase playerBase = bases.get(baseId);
            if (playerBase == null) {
                throw new IllegalArgumentException("No base for BaseId: " + baseId);
            }
            return playerBase;
        }
    }

    public void tick() {
        try {
            synchronized (commandQueue) {
                commandQueue.forEach(this::executeCommand);
                commandQueue.clear();
            }
            synchronized (activeItems) {
                synchronized (activeItemQueue) {
                    activeItems.addAll(activeItemQueue);
                    activeItemQueue.clear();
                }
                Iterator<SyncBaseItem> iterator = activeItems.iterator();
                while (iterator.hasNext()) {
                    SyncBaseItem activeItem = iterator.next();
                    if (!activeItem.isAlive()) {
                        iterator.remove();
                        continue;
                    }
                    if (activeItem.isIdle()) {
                        iterator.remove();
                        if (!guardingItemService.add(activeItem)) {
                            gameLogicService.onSyncBaseItemIdle(activeItem);
                        }
                        syncService.notifySendSyncBaseItem(activeItem);
                        continue;
                    }
                    try {
                        if (!activeItem.tick()) {
                            try {
                                activeItem.stop(true);
                                iterator.remove();
                                if (!guardingItemService.add(activeItem)) {
                                    gameLogicService.onSyncBaseItemIdle(activeItem);
                                }
                                syncService.notifySendSyncBaseItem(activeItem);
                            } catch (Throwable t) {
                                logger.log(Level.WARNING, "Error during deactivation of active item: " + activeItem, t);
                            }
                        }
                    } catch (Throwable t) {
                        activeItem.stop(true);
                        logger.log(Level.WARNING, t.getMessage(), t);
                        iterator.remove();
                        gameLogicService.onSyncBaseItemIdle(activeItem);
                        syncService.notifySendSyncBaseItem(activeItem);
                    }
                }
            }
            guardingItemService.tick();
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    private void executeCommand(BaseCommand baseCommand) {
        try {
            SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(baseCommand.getId());
            syncBaseItem.stop(false);
            syncBaseItem.executeCommand(baseCommand);
            addToActiveItemQueue(syncBaseItem);
            guardingItemService.remove(syncBaseItem);
            syncService.notifySendSyncBaseItem(syncBaseItem);
            gameLogicService.onMasterCommandSent(syncBaseItem);
        } catch (ItemDoesNotExistException e) {
            gameLogicService.onItemDoesNotExistException(e);
        } catch (InsufficientFundsException e) {
            gameLogicService.onInsufficientFundsException(e);
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    public void addToActiveItemQueue(SyncBaseItem activeItem) {
        synchronized (activeItemQueue) {
            if (!activeItems.contains(activeItem) && !activeItemQueue.contains(activeItem)) {
                activeItemQueue.add(activeItem);
            }
        }
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncItemContainerService.getSyncBaseItemInfos();
    }

    public List<PlayerBaseInfo> getPlayerBaseInfos() {
        List<PlayerBaseInfo> playerBaseInfos = new ArrayList<>();
        for (PlayerBase playerBase : bases.values()) {
            playerBaseInfos.add(playerBase.getPlayerBaseInfo());
        }
        return playerBaseInfos;
    }

    public List<PlayerBaseInfo> getBackupPlayerBaseInfos() {
        List<PlayerBaseInfo> playerBaseInfos = new ArrayList<>();
        for (PlayerBase playerBase : bases.values()) {
            if (playerBase.getCharacter().isBot()) {
                continue;
            }
            PlayerBaseFull playerBaseFull = (PlayerBaseFull) playerBase;
            if (playerBaseFull.getItemCount() > 0) {
                playerBaseInfos.add(playerBaseFull.getPlayerBaseInfo());
            }
        }
        return playerBaseInfos;
    }

    public void fillBackup(BackupPlanetInfo backupPlanetInfo) {
        List<SyncBaseItemInfo> syncBaseItemInfos = getSyncBaseItemInfos();
        syncBaseItemInfos.removeIf(syncBaseItemInfo -> {
            if (!isSaveNeeded(syncBaseItemInfo.getBaseId())) {
                return true;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
            if (baseItemType.getWeaponType() != null && syncBaseItemInfo.getTarget() != null) {
                SyncItem targetItem = syncItemContainerService.getSyncItem(syncBaseItemInfo.getTarget());
                if (targetItem instanceof SyncBaseItem) {
                    SyncBaseItem targetSyncBaseItem = (SyncBaseItem) targetItem;
                    if (isEnemy(syncItemContainerService.getSyncBaseItem(syncBaseItemInfo.getId()), targetSyncBaseItem)) {
                        if (!isSaveNeeded(targetSyncBaseItem.getBase())) {
                            syncBaseItemInfo.setTarget(null);
                        }
                    }
                }
            }
            if (baseItemType.getHarvesterType() != null && syncBaseItemInfo.getTarget() != null) {
                SyncItem targetItem = syncItemContainerService.getSyncItem(syncBaseItemInfo.getTarget());
                if (targetItem instanceof SyncResourceItem) {
                    syncBaseItemInfo.setTarget(null);
                }
            }
            syncBaseItemInfo.setSyncBoxItemId(null);
            return false;
        });
        backupPlanetInfo.setSyncBaseItemInfos(syncBaseItemInfos);
        backupPlanetInfo.setPlayerBaseInfos(getBackupPlayerBaseInfos());
    }

    private boolean isSaveNeeded(int playerBaseId) {
        PlayerBase playerBase = getPlayerBase4BaseId(playerBaseId);
        return isSaveNeeded(playerBase);
    }

    private boolean isSaveNeeded(PlayerBase playerBase) {
        return !playerBase.getCharacter().isBot();
    }

    public void restore(BackupPlanetInfo backupPlanetInfo, BaseRestoreProvider baseRestoreProvider) {
        Set<Integer> failedItems = new HashSet<>();
        boolean failed;
        do {
            failed = false;
            bases.clear();
            lastBaseItId = 1;
            syncItemContainerService.clear();
            energyService.clean();
            backupPlanetInfo.getPlayerBaseInfos().forEach(playerBaseInfo -> {
                try {
                    lastBaseItId = Math.max(playerBaseInfo.getBaseId(), lastBaseItId);
                    bases.put(playerBaseInfo.getBaseId(), new PlayerBaseFull(playerBaseInfo.getBaseId(), baseRestoreProvider.getName(playerBaseInfo), playerBaseInfo.getCharacter(), playerBaseInfo.getResources(), baseRestoreProvider.getLevel(playerBaseInfo), baseRestoreProvider.getUnlockedItemLimit(playerBaseInfo), playerBaseInfo.getUserId(), null));
                } catch (Exception e) {
                    logger.log(Level.WARNING, "BaseItemService.restore()", e);
                }
            });
            Collection<SyncBaseItemInfo> syncBaseItemInfos = backupPlanetInfo.getSyncBaseItemInfos().stream().filter(syncBaseItemInfo -> !failedItems.contains(syncBaseItemInfo.getId())).collect(Collectors.toList());
            Map<SyncBaseItem, SyncBaseItemInfo> tmp = new HashMap<>();
            for (SyncBaseItemInfo syncBaseItemInfo : syncBaseItemInfos) {
                try {
                    SyncBaseItem syncBaseItem = createSyncBaseItemRestore(syncBaseItemInfo, (PlayerBaseFull) getPlayerBase4BaseId(syncBaseItemInfo.getBaseId()));
                    tmp.put(syncBaseItem, syncBaseItemInfo);
                } catch (Exception e) {
                    failed = true;
                    failedItems.add(syncBaseItemInfo.getId());
                    logger.log(Level.WARNING, "BaseItemService.restore()", e);
                }
            }
            for (Map.Entry<SyncBaseItem, SyncBaseItemInfo> entry : tmp.entrySet()) {
                try {
                    synchronizeActivateSlave(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    failed = true;
                    failedItems.add(entry.getValue().getId());
                    logger.log(Level.WARNING, "BaseItemService.restore()", e);
                }
            }
        } while (failed);
        Collection<Integer> basesToRemove = bases.values().stream().filter(playerBase -> ((PlayerBaseFull) playerBase).getItemCount() == 0).map(PlayerBase::getBaseId).collect(Collectors.toList());
        basesToRemove.forEach(baseId -> {
            PlayerBase playerBase = bases.remove(baseId);
            logger.warning("BaseItemService.restore(). PlayerBase remove due to no units. baseId: " + baseId + " name: " + playerBase.getName());
        });
    }

    public void processPendingReceivedTickInfos(long tickCount) {
        while (!pendingReceivedTickInfos.isEmpty() && pendingReceivedTickInfos.peek().getTickCount() <= tickCount) {
            TickInfo tickInfo = pendingReceivedTickInfos.remove();
            // System.out.println("Synchronize pending: slaveTickCount: " + tickCount + " info tick count: " + syncBaseItemInfo.getTickCount() + ". " + syncBaseItemInfo);
            tickInfo.getSyncBaseItemInfos().forEach(this::onSlaveSyncBaseItemChanged);
        }
    }

    public void queueCommand(BaseCommand baseCommand) {
        synchronized (commandQueue) {
            commandQueue.add(baseCommand);
        }
    }
}
