package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Beat
 * 15.07.2016.
 */
@ApplicationScoped // Rename to BaseService
public class BaseItemService {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private PlanetService planetService;
    @Inject
    private LevelService levelService;
    @Inject
    private CommandService commandService;
    @Inject
    private ItemTypeService itemTypeService;
    private final Map<Integer, PlayerBase> bases = new HashMap<>();
    private int lastBaseItId;
    private final Collection<SyncBaseItem> activeItems = new ArrayList<>();
    private final Collection<SyncBaseItem> activeItemQueue = new ArrayList<>();
    private final Collection<SyncBaseItem> guardingItems = new ArrayList<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        activeItems.clear();
        activeItemQueue.clear();
        bases.clear();
        guardingItems.clear();
        lastBaseItId = 0;
    }

    public PlayerBase getFirstHumanBase() {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getCharacter().isHuman()) {
                    return playerBase;
                }
            }
        }
        throw new IllegalStateException("No human base found");
    }

    public PlayerBase createHumanBase(int resources, int levelId, int userId, String name) {
        return createBase(name, Character.HUMAN, resources, levelId, userId);
    }


    private void surrenderHumanBase(int userId) {
        PlayerBase playerBase = getPlayerBase4UserId(userId);
        if (playerBase != null) {
            gameLogicService.onSurrenderBase(playerBase);
            while (!playerBase.getItems().isEmpty()) {
                removeSyncItem(CollectionUtils.getFirst(playerBase.getItems()));
            }
        }
    }

    public void createHumanBaseWithBaseItem(int levelId, int userId, String name, int baseItemTypeId, DecimalPosition position) {
        surrenderHumanBase(userId);
        PlayerBase playerBase = createHumanBase(planetService.getPlanetConfig().getStartRazarion(), levelId, userId, name);
        spawnSyncBaseItem(itemTypeService.getBaseItemType(baseItemTypeId), position, 0, playerBase, false);
    }

    public void updateLevel(int userId, int levelId) {
        PlayerBase playerBase = getPlayerBase4UserId(userId);
        if (playerBase == null) {
            throw new IllegalArgumentException("No base for userId: " + userId);
        }
        playerBase.setLevelId(levelId);
    }

    public PlayerBase createBotBase(BotConfig botConfig) {
        return createBase(botConfig.getName(), botConfig.isNpc() ? Character.BOT_NCP : Character.BOT, 0, null, null);
    }

    private PlayerBase createBase(String name, Character character, int resources, Integer levelId, Integer userId) {
        synchronized (bases) {
            lastBaseItId++;
            if (bases.containsKey(lastBaseItId)) {
                throw new IllegalStateException("Base with Id already exits: " + lastBaseItId);
            }
            PlayerBase playerBase = new PlayerBase(lastBaseItId, name, character, resources, levelId, userId);
            bases.put(lastBaseItId, playerBase);
            gameLogicService.onBaseCreated(playerBase);
            return playerBase;
        }
    }

    public SyncBaseItem createSyncBaseItem4Factory(BaseItemType toBeBuilt, DecimalPosition position, PlayerBase base) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, 0, base);
        syncBaseItem.setSpawnProgress(1.0);
        syncBaseItem.setBuildup(1.0);
        gameLogicService.onFactorySyncItem(syncBaseItem, toBeBuilt);
        return syncBaseItem;
    }

    public SyncBaseItem createSyncBaseItem4Builder(BaseItemType toBeBuilt, DecimalPosition position, PlayerBase base) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(toBeBuilt, position, 0, base);

        syncBaseItem.setSpawnProgress(1.0);
        gameLogicService.onBuildingSyncItem(syncBaseItem, toBeBuilt);

        return syncBaseItem;
    }

    public void spawnSyncBaseItem(int baseItemTypeId, Collection<DecimalPosition> positions, PlayerBase base) throws ItemLimitExceededException, HouseSpaceExceededException {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        for (DecimalPosition position : positions) {
            spawnSyncBaseItem(baseItemType, position, 0, base, false);
        }
    }

    public SyncBaseItem spawnSyncBaseItem(BaseItemType baseItemTypeId, DecimalPosition position, double zRotation, PlayerBase base, boolean noSpawn) throws ItemLimitExceededException, HouseSpaceExceededException {
        SyncBaseItem syncBaseItem = createSyncBaseItem(baseItemTypeId, position, zRotation, base);
        syncBaseItem.setBuildup(1.0);

        if (noSpawn) {
            syncBaseItem.setSpawnProgress(1.0);
            syncBaseItem.handleIfItemBecomesReady();
        } else {
            gameLogicService.onSpawnSyncItemStart(syncBaseItem);
        }

        return syncBaseItem;
    }

    private SyncBaseItem createSyncBaseItem(BaseItemType toBeBuilt, DecimalPosition position2d, double zRotation, PlayerBase base) throws ItemLimitExceededException, HouseSpaceExceededException {
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
        gameLogicService.onKilledSyncBaseItem(target, actor);
        PlayerBase base = target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        if (base.getItemCount() == 0) {
            gameLogicService.onBaseKilled(base, actor);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
        }
    }

    public void removeSyncItem(SyncBaseItem target) {
        target.clearHealth();
        gameLogicService.onSyncBaseItemRemoved(target);
        PlayerBase base = target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        if (base.getItemCount() == 0) {
            gameLogicService.onBaseRemoved(base);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
        }
    }

    public void checkItemLimit4ItemAdding(BaseItemType newItemType, int itemCount2Add, PlayerBase simpleBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        if (isLevelLimitation4ItemTypeExceeded(newItemType, itemCount2Add, simpleBase)) {
            throw new ItemLimitExceededException(newItemType, itemCount2Add, simpleBase);
        }
        if (isHouseSpaceExceeded(simpleBase, newItemType)) {
            throw new HouseSpaceExceededException();
        }
    }

    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, int itemCount2Add, PlayerBase playerBase) throws NoSuchItemTypeException {
        return playerBase.getCharacter().isBot() || getItemCount(playerBase, newItemType) + itemCount2Add > getLimitation4ItemType(newItemType.getId(), playerBase.getLevelId());
    }

    public int getItemCount(PlayerBase playerBase, BaseItemType baseItemType) {
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
        int planetCount = planetService.getPlanetConfig().imitation4ItemType(itemTypeId);
        return Math.min(levelCount, planetCount);
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem1, SyncBaseItem syncBaseItem2) {
        PlayerBase playerBase1 = syncBaseItem1.getBase();
        PlayerBase playerBase2 = syncBaseItem2.getBase();
        return playerBase1.isEnemy(playerBase2);
    }

    public boolean hasEnemyForSpawn(DecimalPosition position, double itemFreeRadius) {
        return false; // TODO if enemies implemented
    }

    public boolean isHouseSpaceExceeded(PlayerBase playerBase, BaseItemType toBeBuiltType) {
        return isHouseSpaceExceeded(playerBase, toBeBuiltType, 1);
    }

    public boolean isHouseSpaceExceeded(PlayerBase playerBase, BaseItemType toBeBuiltType, int itemCount2Add) {
        return playerBase.getUsedHouseSpace() + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > playerBase.getHouseSpace() + planetService.getPlanetConfig().getHouseSpace();
    }

    public PlayerBase getPlayerBase4UserId(int userId) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (playerBase.getUserId() != null && playerBase.getUserId() == userId) {
                    return playerBase;
                }
            }
        }
        return null;
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
                    gameLogicService.onSyncBaseItemIdle(activeItem);
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

            if (syncBaseItem.getSyncConsumer() != null && !syncBaseItem.getSyncConsumer().isOperating()) {
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
            commandService.defend(guardingItem, target);
            return true;
        } else {
            return false;
        }
    }

    // --------------------------------------------------------------------------

    @Deprecated // Use syncBaseItemContainerService.getSyncItem
    public SyncBaseItem getItem(int id) throws ItemDoesNotExistException {
        throw new UnsupportedOperationException();
    }

    // TODO List<SyncBaseItem> getBaseItems(List<Id> baseItemsIds) throws ItemDoesNotExistException;

    // TODO List<Id> getBaseItemIds(List<SyncBaseItem> baseItems);

    public boolean baseObjectExists(SyncItem currentBuildup) {
        throw new UnsupportedOperationException();
    }

    // TODO SyncItem newSyncItem(Id id, Index position, int itemTypeId, PlayerBase base) throws NoSuchItemTypeException;

    // TODO Collection<SyncBaseItem> getItems4Base(PlayerBase simpleBase);

    // TODO Collection<SyncBaseItem> getItems4BaseAndType(PlayerBase simpleBase, int itemTypeId);

    // TODO Collection<SyncBaseItem> getItems4BaseAndType(boolean includingNoPosition, final PlayerBase simpleBase, final int itemTypeId);

    // TODO Collection<? extends SyncItem> getItems(ItemType itemType, PlayerBase simpleBase);

    // TODO boolean hasEnemyInRange(PlayerBase simpleBase, Index middlePoint, int range);

    // TODO boolean hasStandingItemsInRect(Rectangle rectangle, SyncItem exceptThat);

    public boolean isSyncItemOverlapping(SyncItem syncItem) {
        throw new UnsupportedOperationException();
    }

    // TODO boolean isSyncItemOverlapping(SyncItem syncItem, Index positionToCheck, Double angelToCheck, Collection<SyncItem> exceptionThem);

    // TODO boolean isUnmovableSyncItemOverlapping(BoundingBox boundingBox, Index positionToCheck);

    public Collection<SyncBaseItem> getBaseItemsInRadius(DecimalPosition position, int radius, PlayerBase playerBase, Collection<BaseItemType> baseItemTypeFilter) {
        throw new UnsupportedOperationException();
    }

    // TODO Collection<SyncItem> getItemsInRectangle(Rectangle rectangle);

    // TODO Collection<SyncItem> getItemsInRectangleFast(Rectangle rectangle);

    // TODO Collection<SyncItem> getItemsInRectangleFastIncludingDead(Rectangle rectangle);

    // TODO Collection<SyncBaseItem> getBaseItemsInRectangle(Rectangle rectangle, PlayerBase simpleBase, Collection<BaseItemType> baseItemTypeFilter);

    // TODO Collection<SyncBaseItem> getBaseItemsInRectangle(Region region, PlayerBase simpleBase, Collection<BaseItemType> baseItemTypeFilter);

    // TODO boolean hasItemsInRectangle(Rectangle rectangle);

    // TODO SyncBaseItem getNearestEnemyItem(final Index middle, final Set<Integer> filter, final PlayerBase simpleBase);

    // TODO SyncResourceItem getNearestResourceItem(final Index middle);

    // TODO SyncBoxItem getNearestBoxItem(final Index middle);

    // TODO void killSyncItems(Collection<? extends SyncItem> syncItems);

    // TODO -- > is in SyncItemContainerService SyncItem getItemAtPosition(Index absolutePosition);

    // TODO void sellItem(Id id) throws ItemDoesNotExistException, NotYourBaseException;

    // TODO boolean hasItemsInRectangleFast(Rectangle rectangle);

}
