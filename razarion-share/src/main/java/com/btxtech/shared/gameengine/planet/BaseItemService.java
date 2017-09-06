package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.NotYourBaseException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.BackupPlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton // Rename to BaseService
public class BaseItemService {
    // private Logger logger = Logger.getLogger(BaseItemService.class.getName());
    private static final double ITEM_SELL_FACTOR = 0.5;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private LevelService levelService;
    @Inject
    private Instance<CommandService> commandService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private EnergyService energyService;
    private final Map<Integer, PlayerBase> bases = new HashMap<>();
    private int lastBaseItId = 1;
    private final Collection<SyncBaseItem> activeItems = new ArrayList<>();
    private final Collection<SyncBaseItem> activeItemQueue = new ArrayList<>();
    private final Collection<SyncBaseItem> guardingItems = new ArrayList<>();
    private PlanetConfig planetConfig;
    private GameEngineMode gameEngineMode;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        activeItems.clear();
        activeItemQueue.clear();
        bases.clear();
        guardingItems.clear();
        lastBaseItId = 1;
        if (planetActivationEvent.getType() == PlanetActivationEvent.Type.INITIALIZE) {
            gameEngineMode = planetActivationEvent.getGameEngineMode();
            planetConfig = planetActivationEvent.getPlanetConfig();
            if (gameEngineMode == GameEngineMode.SLAVE && planetActivationEvent.getSlaveSyncItemInfo() != null) {
                for (PlayerBaseInfo playerBaseInfo : planetActivationEvent.getSlaveSyncItemInfo().getPlayerBaseInfos()) {
                    createBaseSlave(playerBaseInfo);
                }

                Map<SyncBaseItem, SyncBaseItemInfo> tmp = new HashMap<>();
                for (SyncBaseItemInfo syncBaseItemInfo : planetActivationEvent.getSlaveSyncItemInfo().getSyncBaseItemInfos()) {
                    SyncBaseItem syncBaseItem = createSyncBaseItemSlave(syncBaseItemInfo, getPlayerBase4BaseId(syncBaseItemInfo.getBaseId()));
                    tmp.put(syncBaseItem, syncBaseItemInfo);
                }

                for (Map.Entry<SyncBaseItem, SyncBaseItemInfo> entry : tmp.entrySet()) {
                    synchronizeActivateSlave(entry.getKey(), entry.getValue());
                }
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

    public PlayerBaseFull createHumanBase(int startRazarion, int levelId, HumanPlayerId humanPlayerId, String name) {
        return createBaseMaster(name, Character.HUMAN, startRazarion, levelId, humanPlayerId);
    }

    private void surrenderHumanBase(HumanPlayerId humanPlayerId) {
        PlayerBaseFull playerBase = getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        if (playerBase != null) {
            gameLogicService.onSurrenderBase(playerBase);
            while (!playerBase.getItems().isEmpty()) {
                removeSyncItem(CollectionUtils.getFirst(playerBase.getItems()));
            }
        }
    }

    public PlayerBaseFull createHumanBaseWithBaseItem(int levelId, HumanPlayerId humanPlayerId, String name, DecimalPosition position) {
        surrenderHumanBase(humanPlayerId);
        PlayerBaseFull playerBase = createHumanBase(planetConfig.getStartRazarion(), levelId, humanPlayerId, name);
        spawnSyncBaseItem(itemTypeService.getBaseItemType(planetConfig.getStartBaseItemTypeId()), position, 0, playerBase, false);
        return playerBase;
    }

    public void updateLevel(HumanPlayerId humanPlayerId, int levelId) {
        PlayerBaseFull playerBase = getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        if (playerBase == null) {
            throw new IllegalArgumentException("No base for humanPlayerId: " + humanPlayerId);
        }
        playerBase.setLevelId(levelId);
    }

    public PlayerBaseFull createBotBase(BotConfig botConfig) {
        return createBaseMaster(botConfig.getName(), botConfig.isNpc() ? Character.BOT_NCP : Character.BOT, 0, null, null);
    }

    private PlayerBaseFull createBaseMaster(String name, Character character, int startRazarion, Integer levelId, HumanPlayerId humanPlayerId) {
        synchronized (bases) {
            lastBaseItId++;
            if (bases.containsKey(lastBaseItId)) {
                throw new IllegalStateException("createBaseMaster: Base with Id already exits: " + lastBaseItId);
            }
            PlayerBaseFull playerBase = new PlayerBaseFull(lastBaseItId, name, character, startRazarion, levelId, humanPlayerId);
            bases.put(lastBaseItId, playerBase);
            gameLogicService.onBaseCreated(playerBase);
            return playerBase;
        }
    }

    public void createBaseSlave(PlayerBaseInfo playerBaseInfo) {
        synchronized (bases) {
            if (bases.containsKey(playerBaseInfo.getBaseId())) {
                throw new IllegalStateException("createBaseSlave: Base with Id already exits: " + playerBaseInfo.getBaseId());
            }
            PlayerBase playerBase = new PlayerBase(playerBaseInfo.getBaseId(), playerBaseInfo.getName(), playerBaseInfo.getCharacter(), playerBaseInfo.getResources(), playerBaseInfo.getHumanPlayerId());
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


    public SyncBaseItem createSyncBaseItem4Factory(BaseItemType toBeBuilt, DecimalPosition position, PlayerBaseFull base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, 0, base);
        syncBaseItem.setSpawnProgress(1.0);
        syncBaseItem.setBuildup(1.0);
        gameLogicService.onFactorySyncItem(syncBaseItem, createdBy);
        return syncBaseItem;
    }

    public SyncBaseItem createSyncBaseItem4Builder(BaseItemType toBeBuilt, DecimalPosition position, PlayerBaseFull base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, 0, base);

        syncBaseItem.setSpawnProgress(1.0);
        gameLogicService.onBuildingSyncItem(syncBaseItem, createdBy);

        return syncBaseItem;
    }

    public void spawnSyncBaseItems(int baseItemTypeId, Collection<DecimalPosition> positions, PlayerBaseFull base) throws ItemLimitExceededException, HouseSpaceExceededException {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        for (DecimalPosition position : positions) {
            spawnSyncBaseItem(baseItemType, position, 0, base, false);
        }
    }

    public SyncBaseItem spawnSyncBaseItem(BaseItemType baseItemType, DecimalPosition position, double zRotation, PlayerBaseFull base, boolean noSpawn) throws ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(baseItemType, position, zRotation, base);
        syncBaseItem.setBuildup(1.0);

        if (noSpawn) {
            syncBaseItem.setSpawnProgress(1.0);
            syncBaseItem.handleIfItemBecomesReady();
        } else {
            gameLogicService.onSpawnSyncItemStart(syncBaseItem);
        }

        return syncBaseItem;
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
        if (base.getItemCount() == 0) {
            gameLogicService.onBaseKilled(base, actor);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
            energyService.onBaseKilled(base);
        }
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
        SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItem(syncBaseItemId);
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
        return getItemCount(playerBase, newItemType) + itemCount2Add > getLimitation4ItemType(newItemType.getId(), playerBase.getLevelId());
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

    public Collection<SyncBaseItem> findEnemyItems(final PlayerBase playerBase, PlaceConfig region) {
        Collection<SyncBaseItem> enemyItems = new ArrayList<>();
        syncItemContainerService.iterateOverItems(false, false, null, (ItemIteratorHandler<Void>) syncItem -> {
            if (syncItem instanceof SyncBaseItem && ((SyncBaseItem) syncItem).isEnemy(playerBase) && (region == null || region.checkInside(syncItem))) {
                enemyItems.add((SyncBaseItem) syncItem);
            }
            return null;
        });
        return enemyItems;
    }

    private SyncBaseItem findNearestEnemy(SyncBaseItem guardingItem) {
        Collection<SyncBaseItem> enemyItems = findEnemyItems(guardingItem.getBase(), new PlaceConfig().setPosition(guardingItem.getSyncPhysicalArea().getPosition2d()).setRadius(guardingItem.getBaseItemType().getWeaponType().getRange() + guardingItem.getSyncPhysicalArea().getRadius()));
        double distance = Double.MAX_VALUE;
        SyncBaseItem nearest = null;
        for (SyncBaseItem enemyItem : enemyItems) {
            double tmpDistance = enemyItem.getSyncPhysicalArea().getDistance(guardingItem);
            if (distance > tmpDistance) {
                distance = tmpDistance;
                nearest = enemyItem;
            }
        }
        return nearest;
    }

    public int getLimitation4ItemType(int itemTypeId, int levelId) {
        int levelCount = levelService.getLevel(levelId).limitation4ItemType(itemTypeId);
        int planetCount = planetConfig.imitation4ItemType(itemTypeId);
        return Math.min(levelCount, planetCount);
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

    public PlayerBaseFull getPlayerBaseFull4HumanPlayerId(HumanPlayerId humanPlayerId) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getHumanPlayerId() != null && playerBase.getHumanPlayerId().equals(humanPlayerId)) {
                    return (PlayerBaseFull) playerBase;
                }
            }
        }
        return null;
    }

    public PlayerBase getPlayerBase4HumanPlayerId(HumanPlayerId humanPlayerId) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getHumanPlayerId() != null && playerBase.getHumanPlayerId().equals(humanPlayerId)) {
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
                    if (!addGuardingBaseItem(activeItem)) {
                        gameLogicService.onSyncBaseItemIdle(activeItem);
                    }
                    continue;
                }
                try {
                    if (!activeItem.tick()) {
                        try {
                            activeItem.stop();
                            iterator.remove();
                            if (!addGuardingBaseItem(activeItem)) {
                                gameLogicService.onSyncBaseItemIdle(activeItem);
                            }
                        } catch (Throwable t) {
                            exceptionHandler.handleException("Error during deactivation of active item: " + activeItem, t);
                        }
                    }
                } catch (Throwable t) {
                    activeItem.stop();
                    exceptionHandler.handleException(t);
                    iterator.remove();
                    gameLogicService.onSyncBaseItemIdle(activeItem);
                }
            }
        }
        synchronized (guardingItems) {
            guardingItems.removeIf(this::handleGuardingItemHasEnemiesInRange);
        }
    }

    public void addToActiveItemQueue(SyncBaseItem activeItem) {
        synchronized (activeItemQueue) {
            if (!activeItems.contains(activeItem) && !activeItemQueue.contains(activeItem)) {
                activeItemQueue.add(activeItem);
            }
        }
    }

    private boolean addGuardingBaseItem(SyncBaseItem syncBaseItem) {
        try {
            if (PlanetService.MODE != PlanetMode.MASTER) {
                return false;
            }

            if (syncBaseItem.getSyncWeapon() == null || !syncBaseItem.isAlive()) {
                return false;
            }

            if (!syncBaseItem.isAlive()) {
                return false;
            }

            if (handleGuardingItemHasEnemiesInRange(syncBaseItem)) {
                return true;
            }

            synchronized (guardingItems) {
                guardingItems.add(syncBaseItem);
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        return false;
    }

    private boolean handleGuardingItemHasEnemiesInRange(SyncBaseItem guardingItem) {
        SyncBaseItem target = findNearestEnemy(guardingItem);
        if (target != null) {
            commandService.get().defend(guardingItem, target);
            return true;
        } else {
            return false;
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

    public List<BackupPlayerBaseInfo> getBackupPlayerBaseInfos(boolean saveUnregistered) {
        List<BackupPlayerBaseInfo> playerBaseInfos = new ArrayList<>();
        for (PlayerBase playerBase : bases.values()) {
            if (playerBase.getCharacter().isBot()) {
                continue;
            }
            if (!saveUnregistered) {
                if (playerBase.getHumanPlayerId().getUserId() == null) {
                    continue;
                }
            }
            playerBaseInfos.add(((PlayerBaseFull) playerBase).getBackupPlayerBaseInfo());
        }
        return playerBaseInfos;
    }

    public void fillBackup(BackupPlanetInfo backupPlanetInfo, boolean saveUnregistered) {
        List<SyncBaseItemInfo> syncBaseItemInfos = getSyncBaseItemInfos();
        syncBaseItemInfos.removeIf(syncBaseItemInfo -> {
            if (!isSaveNeeded(syncBaseItemInfo.getBaseId(), saveUnregistered)) {
                return true;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItemInfo.getItemTypeId());
            if (baseItemType.getWeaponType() != null && syncBaseItemInfo.getTarget() != null) {
                SyncItem targetItem = syncItemContainerService.getSyncItem(syncBaseItemInfo.getTarget());
                if (targetItem instanceof SyncBaseItem) {
                    SyncBaseItem targetSyncBaseItem = (SyncBaseItem) targetItem;
                    if (isEnemy(syncItemContainerService.getSyncBaseItem(syncBaseItemInfo.getId()), targetSyncBaseItem)) {
                        if (!isSaveNeeded(targetSyncBaseItem.getBase(), saveUnregistered)) {
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
            return false;
        });
        backupPlanetInfo.setSyncBaseItemInfos(syncBaseItemInfos);
        backupPlanetInfo.setPlayerBaseInfos(getBackupPlayerBaseInfos(saveUnregistered));
    }

    private boolean isSaveNeeded(int playerBaseId, boolean saveUnregistered) {
        PlayerBase playerBase = getPlayerBase4BaseId(playerBaseId);
        return isSaveNeeded(playerBase, saveUnregistered);
    }

    private boolean isSaveNeeded(PlayerBase playerBase, boolean saveUnregistered) {
        return !playerBase.getCharacter().isBot() && (saveUnregistered || playerBase.getHumanPlayerId().getUserId() != null);
    }

    public void restore(BackupPlanetInfo backupPlanetInfo) {
        lastBaseItId = 1;
        backupPlanetInfo.getPlayerBaseInfos().forEach(playerBaseInfo -> {
            lastBaseItId = Math.max(playerBaseInfo.getBaseId(), lastBaseItId);
            bases.put(playerBaseInfo.getBaseId(), new PlayerBaseFull(lastBaseItId, playerBaseInfo.getName(), playerBaseInfo.getCharacter(), playerBaseInfo.getResources(), playerBaseInfo.getLevel(), playerBaseInfo.getHumanPlayerId()));
        });
        Map<SyncBaseItem, SyncBaseItemInfo> tmp = new HashMap<>();
        for (SyncBaseItemInfo syncBaseItemInfo : backupPlanetInfo.getSyncBaseItemInfos()) {
            SyncBaseItem syncBaseItem = createSyncBaseItemRestore(syncBaseItemInfo, (PlayerBaseFull) getPlayerBase4BaseId(syncBaseItemInfo.getBaseId()));
            tmp.put(syncBaseItem, syncBaseItemInfo);
        }
        for (Map.Entry<SyncBaseItem, SyncBaseItemInfo> entry : tmp.entrySet()) {
            synchronizeActivateSlave(entry.getKey(), entry.getValue());
        }
    }

    // --------------------------------------------------------------------------

    @Deprecated // Use syncBaseItemContainerService.getSyncItem
    public SyncBaseItem getItem(int id) throws ItemDoesNotExistException {
        throw new UnsupportedOperationException();
    }
}
