package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.ItemLifecycle;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncItemPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 15.07.2016.
 */
@ApplicationScoped
public class BaseItemService {
    @Inject
    private ActivityService activityService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    private final Map<Integer, PlayerBase> bases = new HashMap<>();
    private final Map<PlayerBase, Collection<SyncBaseItem>> baseItems = new HashMap<>();
    private int lastBaseItId;

    public PlayerBase createBotBase(BotConfig botConfig) {
        synchronized (bases) {
            lastBaseItId++;
            if (bases.containsKey(lastBaseItId)) {
                throw new IllegalStateException("Base with Id already exits: " + lastBaseItId);
            }
            PlayerBase playerBase = new PlayerBase(lastBaseItId, botConfig.getName(), true, botConfig.isNpc());
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

    public SyncItem createSyncBaseItem4Beam(BaseItemType toBeBuilt, Index position, PlayerBase base) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException {
        if (!isAlive(base)) {
            throw new BaseDoesNotExistException(base);
        }

        if (base.isAbandoned()) {
            throw new IllegalStateException();
        }

        if (base.getCharacter().isHuman()) {
            checkItemLimit4ItemAdding(toBeBuilt, base);
        }

        Vertex vertexPosition = terrainService.getVertexAt(position);
        vertexPosition = collisionService.correctPosition(vertexPosition, toBeBuilt);

        SyncBaseItem syncBaseItem = syncItemContainerService.createSyncItem(SyncBaseItem.class, toBeBuilt, new SyncItemPosition(vertexPosition, toBeBuilt.getRadius()));
        syncBaseItem.setup(base, ItemLifecycle.SPAWN);
        syncBaseItem.setSpawnProgress(0);

        synchronized (baseItems) {
            Collection<SyncBaseItem> itemsInBase = baseItems.get(base);
            if (itemsInBase == null) {
                itemsInBase = new ArrayList<>();
                baseItems.put(base, itemsInBase);
            }
            itemsInBase.add(syncBaseItem);
        }

        activityService.onSpawnSyncItem(syncBaseItem);

        return syncBaseItem;
    }

    public void checkItemLimit4ItemAdding(BaseItemType newItemType, PlayerBase simpleBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        throw new UnsupportedOperationException();
//        if (isLevelLimitation4ItemTypeExceeded(newItemType, simpleBase)) {
//            throw new ItemLimitExceededException();
//        }
//        if (isHouseSpaceExceeded(simpleBase, newItemType)) {
//            throw new HouseSpaceExceededException();
//        }
    }


    public boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, PlayerBase playerBase) throws NoSuchItemTypeException {
        return getItemCount(playerBase, newItemType) >= getLimitation4ItemType(playerBase, newItemType);
    }

    /**
     * All items of a specific base
     *
     * @param playerBase base
     * @return null or unmodifiable collection with SyncBaseItems
     */
    public Collection<SyncBaseItem> getItems(PlayerBase playerBase) {
        synchronized (baseItems) {
            Collection<SyncBaseItem> syncBaseItems = baseItems.get(playerBase);
            if (syncBaseItems == null) {
                return null;
            }
            return Collections.unmodifiableCollection(syncBaseItems);
        }
    }

    public int getItemCount(PlayerBase playerBase, BaseItemType baseItemType) {
        int count = 0;
        for (SyncBaseItem syncBaseItem : getItems(playerBase)) {
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
        syncItemContainerService.iterateOverItems(false, false, null, new ItemIteratorHandler<Void>() {
            @Override
            public Void handleItem(SyncItem syncItem) {
                if (syncItem instanceof SyncBaseItem && ((SyncBaseItem) syncItem).isEnemy(playerBase) && (region == null || region.isInside(syncItem))) {
                    enemyItems.add((SyncBaseItem) syncItem);
                }

                return null;
            }
        });
        return enemyItems;
    }

    public int getLimitation4ItemType(PlayerBase playerBase, BaseItemType itemType) {
        throw new UnsupportedOperationException();
//        int levelCount = levelService.getLevelScope(playerBase).getLimitation4ItemType(itemType.getId());
//        int planetCount = planetService.getPlanetConfig().getLimitation4ItemType(itemType.getId());
//        return Math.min(levelCount, planetCount);
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem1, SyncBaseItem syncBaseItem2) {
        PlayerBase playerBase1 = syncBaseItem1.getBase();
        PlayerBase playerBase2 = syncBaseItem2.getBase();
        return playerBase1.isEnemy(playerBase2);
    }

    public Collection<SyncBaseItem> getSyncBaseItems() {
        Collection<SyncBaseItem> total = new ArrayList<>();
        synchronized (baseItems) {
            for (Collection<SyncBaseItem> syncBaseItems : baseItems.values()) {
                total.addAll(syncBaseItems);
            }
        }
        return total;
    }

    public Collection<SyncBaseItem> getBeamingSyncBaseItems() {
        Collection<SyncBaseItem> total = new ArrayList<>();
        synchronized (baseItems) {
            for (Collection<SyncBaseItem> syncBaseItems : baseItems.values()) {
                for (SyncBaseItem syncBaseItem : syncBaseItems) {
                    if (syncBaseItem.getItemLifecycle() == ItemLifecycle.SPAWN) {
                        total.addAll(syncBaseItems);
                    }
                }
            }
        }
        return total;
    }

    // --------------------------------------------------------------------------

    public SyncItem getItem(int id) throws ItemDoesNotExistException {
        throw new UnsupportedOperationException();
    }

    // TODO List<SyncBaseItem> getBaseItems(List<Id> baseItemsIds) throws ItemDoesNotExistException;

    // TODO List<Id> getBaseItemIds(List<SyncBaseItem> baseItems);

    public void killSyncItem(SyncItem killedItem, PlayerBase actor, boolean force, boolean explode) {
        throw new UnsupportedOperationException();
    }

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

    public Collection<SyncBaseItem> getBaseItemsInRadius(Index position, int radius, PlayerBase playerBase, Collection<BaseItemType> baseItemTypeFilter) {
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

    // TODO SyncItem getItemAtAbsolutePosition(Index absolutePosition);

    // TODO void sellItem(Id id) throws ItemDoesNotExistException, NotYourBaseException;

    // TODO boolean hasItemsInRectangleFast(Rectangle rectangle);

}
