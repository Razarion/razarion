package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.ItemLifecycle;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

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
    private ActivityService activityService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private PlanetService planetService;
    @Inject
    private LevelService levelService;
    @Inject
    private ItemTypeService itemTypeService;
    private final Map<Integer, PlayerBase> bases = new HashMap<>();
    private int lastBaseItId;
    private final Collection<SyncBaseItem> activeItems = new ArrayList<>();
    private final Collection<SyncBaseItem> activeItemQueue = new ArrayList<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        bases.clear();
        lastBaseItId = 0;
    }

    public PlayerBase createHumanBase(UserContext userContext) {
        return createBase(userContext.getName(), Character.HUMAN, userContext);
    }

    public PlayerBase createBotBase(BotConfig botConfig) {
        return createBase(botConfig.getName(), botConfig.isNpc() ? Character.BOT_NCP : Character.BOT, null);
    }

    private PlayerBase createBase(String name, Character character, UserContext userContext) {
        synchronized (bases) {
            lastBaseItId++;
            if (bases.containsKey(lastBaseItId)) {
                throw new IllegalStateException("Base with Id already exits: " + lastBaseItId);
            }
            PlayerBase playerBase = new PlayerBase(lastBaseItId, name, character, userContext);
            bases.put(lastBaseItId, playerBase);
            activityService.onBaseCreated(playerBase);
            return playerBase;
        }
    }

    public SyncItem createSyncBaseItem4Factory(BaseItemType toBeBuilt, Vertex position, PlayerBase base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public SyncItem createSyncBaseItem4Builder(BaseItemType toBeBuilt, Vertex position, PlayerBase base, SyncBaseItem createdBy) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public SyncBaseItem spawnSyncBaseItem(BaseItemType toBeBuilt, DecimalPosition position, PlayerBase base, boolean noSpawn) throws ItemLimitExceededException, HouseSpaceExceededException {
        return spawnSyncBaseItem(toBeBuilt, terrainService.calculatePositionGroundMesh(position), base, noSpawn);
    }

    public SyncBaseItem spawnSyncBaseItem(BaseItemType toBeBuilt, Vertex position, PlayerBase base, boolean noSpawn) throws ItemLimitExceededException, HouseSpaceExceededException {
        if (!isAlive(base)) {
            throw new BaseDoesNotExistException(base);
        }

        if (base.isAbandoned()) {
            throw new IllegalStateException();
        }

        if (base.getCharacter().isHuman()) {
            checkItemLimit4ItemAdding(toBeBuilt, 1, base);
        }

        position = collisionService.correctPosition(position, toBeBuilt);

        SyncBaseItem syncBaseItem = syncItemContainerService.createSyncBaseItem(toBeBuilt, position);
        if (noSpawn) {
            syncBaseItem.setup(base, ItemLifecycle.ALIVE);
        } else {
            syncBaseItem.setup(base, ItemLifecycle.SPAWN);
        }
        syncBaseItem.setSpawnProgress(0);
        syncBaseItem.setBuildup(1.0);
        base.addItem(syncBaseItem);

        addToQueue(syncBaseItem);
        activityService.onSpawnSyncItem(syncBaseItem);

        return syncBaseItem;
    }

    public void killSyncItem(SyncBaseItem target, SyncBaseItem actor) {
        activityService.onKilledSyncBaseItem(target, actor);
        PlayerBase base = target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        if (base.getItemCount() == 0) {
            activityService.onBaseKilled(base, actor);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
        }
    }

    public void removeSyncItem(SyncBaseItem target) {
        activityService.onSyncBaseItemRemoved(target);
        PlayerBase base = target.getBase();
        base.removeItem(target);
        syncItemContainerService.destroySyncItem(target);
        if (base.getItemCount() == 0) {
            activityService.onBaseRemoved(base);
            synchronized (bases) {
                bases.remove(base.getBaseId());
            }
        }
    }

    public void checkItemLimit4ItemAdding(BaseItemType newItemType, int itemCount2Add, PlayerBase simpleBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        if (isLevelLimitation4ItemTypeExceeded(newItemType, itemCount2Add, simpleBase)) {
            throw new ItemLimitExceededException();
        }
        if (isHouseSpaceExceeded(simpleBase, newItemType)) {
            throw new HouseSpaceExceededException();
        }
    }

    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, int itemCount2Add, PlayerBase playerBase) throws NoSuchItemTypeException {
        return getItemCount(playerBase, newItemType) + itemCount2Add > getLimitation4ItemType(playerBase, newItemType);
    }

    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, int itemCount2Add, UserContext userContext) throws NoSuchItemTypeException {
        PlayerBase playerBase = getPlayerBase(userContext);
        return playerBase == null || isLevelLimitation4ItemTypeExceeded(newItemType, itemCount2Add, playerBase);
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

    public Collection<SyncBaseItem> getEnemyItems(final PlayerBase playerBase, final Region region) {
        final Collection<SyncBaseItem> enemyItems = new ArrayList<>();
        syncItemContainerService.iterateOverItems(false, false, null, (ItemIteratorHandler<Void>) syncItem -> {
            if (syncItem instanceof SyncBaseItem && ((SyncBaseItem) syncItem).isEnemy(playerBase) && (region == null || region.isInside(syncItem))) {
                enemyItems.add((SyncBaseItem) syncItem);
            }
            return null;
        });
        return enemyItems;
    }

    public int getLimitation4ItemType(UserContext userContext, int itemTypeId) {
        int levelCount = levelService.getLevel(userContext.getLevelId()).limitation4ItemType(itemTypeId);
        int planetCount = planetService.getPlanetConfig().imitation4ItemType(itemTypeId);
        return Math.min(levelCount, planetCount);
    }

    public int getLimitation4ItemType(UserContext userContext, BaseItemType itemType) {
        return getLimitation4ItemType(userContext, itemType.getId());
    }

    public int getLimitation4ItemType(PlayerBase playerBase, BaseItemType itemType) {
        return getLimitation4ItemType(playerBase.getUserContext(), itemType);
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem1, SyncBaseItem syncBaseItem2) {
        PlayerBase playerBase1 = syncBaseItem1.getBase();
        PlayerBase playerBase2 = syncBaseItem2.getBase();
        return playerBase1.isEnemy(playerBase2);
    }

    public Collection<SyncBaseItem> getItemLifecycleBaseItems(ItemLifecycle itemLifecycle) {
        Collection<SyncBaseItem> total = new ArrayList<>();
        synchronized (bases) {
            for (PlayerBase base : bases.values()) {
                total.addAll(base.getItems().stream().filter(syncBaseItem -> syncBaseItem.getItemLifecycle() == itemLifecycle).collect(Collectors.toList()));
            }
        }
        return total;
    }

    public boolean hasEnemyForSpawn(DecimalPosition position, double itemFreeRadius) {
        return false; // TODO if enemies implemented
    }

    public boolean isHouseSpaceExceeded(UserContext userContext, BaseItemType toBeBuiltType, int itemCount2Add) {
        PlayerBase playerBase = getPlayerBase(userContext);
        return playerBase == null || isHouseSpaceExceeded(playerBase, toBeBuiltType, itemCount2Add);
    }

    public boolean isHouseSpaceExceeded(PlayerBase playerBase, BaseItemType toBeBuiltType) {
        return isHouseSpaceExceeded(playerBase, toBeBuiltType, 1);
    }

    public boolean isHouseSpaceExceeded(PlayerBase playerBase, BaseItemType toBeBuiltType, int itemCount2Add) {
        return playerBase.getUsedHouseSpace() + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > playerBase.getHouseSpace() + planetService.getPlanetConfig().getHouseSpace();
    }

    public PlayerBase getPlayerBase(UserContext userContext) {
        synchronized (bases) {
            for (PlayerBase playerBase : bases.values()) {
                if (userContext.equals(playerBase.getUserContext())) {
                    return playerBase;
                }
            }
        }
        return null;
    }

    public int getItemCount(UserContext userContext) {
        PlayerBase playerBase = getPlayerBase(userContext);
        if (playerBase != null) {
            return playerBase.getItemCount();
        } else {
            return 0;
        }
    }

    public int getItemCount(UserContext userContext, int itemTypeId) {
        PlayerBase playerBase = getPlayerBase(userContext);
        if (playerBase != null) {
            return getItemCount(playerBase, itemTypeId);
        } else {
            return 0;
        }
    }

    private int getItemCount(PlayerBase playerBase, int baseItemTypeId) {
        return getItemCount(playerBase, itemTypeService.getBaseItemType(baseItemTypeId));
    }

    public int getAccountBalance(UserContext userContext) {
        PlayerBase playerBase = getPlayerBase(userContext);
        if (playerBase != null) {
            return (int) playerBase.getResources();
        } else {
            return 0;
        }
    }

    public void tick(long timeStamp) {
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
                    break;
                }
                if (activeItem.isIdle()) {
                    iterator.remove();
                    break;
                }
                try {
                    if (!activeItem.tick(timeStamp)) {
                        try {
                            activeItem.stop();
                            // TODO addGuardingBaseItem(activeItem);
                            iterator.remove();
                        } catch (Throwable t) {
                            exceptionHandler.handleException("Error during deactivation of active item: " + activeItem, t);
                        }
                    }
                } catch (BaseDoesNotExistException e) {
                    activeItem.stop();
                    iterator.remove();
                } catch (PositionTakenException e) {
                    activeItem.stop();
                    activityService.onPositionTakenException(e);
                    iterator.remove();
                } catch (PathCanNotBeFoundException e) {
                    activeItem.stop();
                    activityService.onPathCanNotBeFoundException(e);
                    iterator.remove();
                } catch (PlaceCanNotBeFoundException e) {
                    activeItem.stop();
                    activityService.onPlaceCanNotBeFoundException(e);
                    iterator.remove();
                } catch (Throwable t) {
                    activeItem.stop();
                    exceptionHandler.handleException(t);
                    iterator.remove();
                }
            }
        }
    }

    public void addToQueue(SyncBaseItem activeItem) {
        synchronized (activeItemQueue) {
            if (!activeItems.contains(activeItem) && !activeItemQueue.contains(activeItem)) {
                activeItemQueue.add(activeItem);
            }
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

    // TODO  Collection<SyncBaseItem> getEnemyItems(PlayerBase base, Rectangle region);

    // TODO boolean hasEnemyInRange(PlayerBase simpleBase, Index middlePoint, int range);

    // TODO boolean hasStandingItemsInRect(Rectangle rectangle, SyncItem exceptThat);

    public boolean isSyncItemOverlapping(SyncItem syncItem) {
        throw new UnsupportedOperationException();
    }

    // TODO boolean isSyncItemOverlapping(SyncItem syncItem, Index positionToCheck, Double angelToCheck, Collection<SyncItem> exceptionThem);

    // TODO boolean isUnmovableSyncItemOverlapping(BoundingBox boundingBox, Index positionToCheck);

    public void checkBuildingsInRect(BaseItemType toBeBuiltType, Vertex toBeBuildPosition) {
        throw new UnsupportedOperationException();
    }

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

    public SyncBaseItem getFirstEnemyItemInRange(SyncBaseItem baseSyncItem) {
        throw new UnsupportedOperationException();
    }

    // TODO -- > is in SyncItemContainerService SyncItem getItemAtPosition(Index absolutePosition);

    // TODO void sellItem(Id id) throws ItemDoesNotExistException, NotYourBaseException;

    // TODO boolean hasItemsInRectangleFast(Rectangle rectangle);

}
