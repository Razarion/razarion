package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.DoubleHolder;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Singleton
public class SyncItemContainerService {
    private static final int CELL_LENGTH_EXPONENT = 4; // 2 ^ exponent
    private static final int CELL_LENGTH = (int) Math.pow(2, CELL_LENGTH_EXPONENT);
    private Logger logger = Logger.getLogger(SyncItemContainerService.class.getName());
    @Inject
    private Instance<SyncItem> syncItemInstance;
    @Inject
    @Named(SyncItem.SYNC_PHYSICAL_AREA)
    private Instance<SyncPhysicalArea> syncPhysicalAreaInstance;
    @Inject
    @Named(SyncItem.SYNC_PHYSICAL_MOVABLE)
    private Instance<SyncPhysicalMovable> syncPhysicalMovableInstance;
    @Inject
    private TerrainService terrainService;
    @Inject
    private Instance<GuardingItemService> guardingItemServiceInstanceInstance;
    @Inject
    private Instance<BotService> botServices;
    private int lastItemId = 1;
    private final HashMap<Integer, SyncItem> items = new HashMap<>();
    private final HashMap<Index, SyncItemContainerCell> cells = new HashMap<>();
    private final Set<SyncBaseItem> pathingChangedItem = new HashSet<>();

    public void clear() {
        items.clear();
        cells.clear();
        pathingChangedItem.clear();
        lastItemId = 1;
    }

    public <T> T iterateOverItems(boolean includeNoPosition, boolean includeDead, T defaultReturn, ItemIteratorHandler<T> itemIteratorHandler) {
        return iterateOverItems(includeNoPosition, includeDead, defaultReturn, null, itemIteratorHandler);
    }

    public <T> T iterateOverItems(boolean includeNoPosition, boolean includeDead, T defaultReturn, SyncItem ignoreMe, ItemIteratorHandler<T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (ignoreMe != null && ignoreMe.equals(syncItem)) {
                    continue;
                }

                if (!includeDead && !syncItem.isAlive()) {
                    continue;
                }
                if (!includeNoPosition && !syncItem.getSyncPhysicalArea().hasPosition()) {
                    continue;
                }
                T result = itemIteratorHandler.handleItem(syncItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultReturn;
    }

    /**
     * Calls function for every sync base item
     *
     * @param itemIteratorHandler syncItem : Function<SyncBaseItem, T> returns null if the iteration shall continue T if the iteration shall stop
     */
    public <T> T iterateOverBaseItems(boolean includeNoPosition, boolean includeDead, T defaultReturn, Function<SyncBaseItem, T> itemIteratorHandler) {
        return iterateOverBaseItems(includeNoPosition, includeDead, null, defaultReturn, itemIteratorHandler);
    }

    /**
     * Calls function for every sync base item
     *
     * @param itemIteratorHandler syncItem : Function<SyncBaseItem, T> returns null if the iteration shall continue T if the iteration shall stop
     */
    private <T> T iterateOverBaseItems(boolean includeNoPosition, boolean includeDead, SyncBaseItem ignoreMe, T defaultReturn, Function<SyncBaseItem, T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (ignoreMe != null && ignoreMe.equals(syncItem)) {
                    continue;
                }

                if (!(syncItem instanceof SyncBaseItem)) {
                    continue;
                }

                SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                if (!includeDead && !syncBaseItem.isAlive()) {
                    continue;
                }
                if (!includeNoPosition && !syncBaseItem.getSyncPhysicalArea().hasPosition()) {
                    continue;
                }
                T result = itemIteratorHandler.apply(syncBaseItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultReturn;
    }

    /**
     * Calls function for every sync resource item
     *
     * @param itemIteratorHandler syncItem : Function<SyncResourceItem, T> returns null if the iteration shall continue T if the iteration shall stop
     */
    private <T> T iterateOverResourceItems(T defaultReturn, Function<SyncResourceItem, T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (!(syncItem instanceof SyncResourceItem)) {
                    continue;
                }

                SyncResourceItem resourceItem = (SyncResourceItem) syncItem;
                if (!resourceItem.isAlive()) {
                    continue;
                }
                T result = itemIteratorHandler.apply(resourceItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultReturn;
    }

    /**
     * Calls function for every sync box item
     *
     * @param itemIteratorHandler syncItem : Function<SyncBoxItem, T> returns null if the iteration shall continue T if the iteration shall stop
     */
    private <T> T iterateOverBoxItems(T defaultReturn, Function<SyncBoxItem, T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (!(syncItem instanceof SyncBoxItem)) {
                    continue;
                }

                SyncBoxItem syncBoxItem = (SyncBoxItem) syncItem;
                T result = itemIteratorHandler.apply(syncBoxItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultReturn;
    }

    public SyncBaseItem createSyncBaseItem(BaseItemType baseItemType, DecimalPosition position2d, double zRotation) {
        SyncBaseItem syncBaseItem = syncItemInstance.select(SyncBaseItem.class).get();
        SyncPhysicalArea syncPhysicalArea = createSyncPhysicalArea(syncBaseItem, baseItemType, position2d, zRotation);
        initAndAdd(baseItemType, syncBaseItem, syncPhysicalArea);
        return syncBaseItem;
    }

    public SyncBaseItem createSyncBaseItemSlave(BaseItemType baseItemType, int syncItemId, DecimalPosition position2d, double zRotation) {
        SyncBaseItem syncBaseItem = syncItemInstance.select(SyncBaseItem.class).get();
        SyncPhysicalArea syncPhysicalArea = createSyncPhysicalArea(syncBaseItem, baseItemType, position2d, zRotation);
        initAndAddSlave(baseItemType, syncItemId, syncBaseItem, syncPhysicalArea);
        return syncBaseItem;
    }

    SyncResourceItem createSyncResourceItem(ResourceItemType resourceItemType, DecimalPosition position2d, double zRotation) {
        SyncResourceItem syncResourceItem = syncItemInstance.select(SyncResourceItem.class).get();
        SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
        syncPhysicalArea.init(syncResourceItem, resourceItemType.getRadius(), resourceItemType.isFixVerticalNorm(), resourceItemType.getTerrainType(), position2d, zRotation);
        initAndAdd(resourceItemType, syncResourceItem, syncPhysicalArea);
        return syncResourceItem;
    }

    SyncResourceItem createSyncResourceItemSlave(ResourceItemType resourceItemType, int syncItemId, DecimalPosition position2d, double zRotation) {
        SyncResourceItem syncResourceItem = syncItemInstance.select(SyncResourceItem.class).get();
        SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
        syncPhysicalArea.init(syncResourceItem, resourceItemType.getRadius(), resourceItemType.isFixVerticalNorm(), resourceItemType.getTerrainType(), position2d, zRotation);
        initAndAddSlave(resourceItemType, syncItemId, syncResourceItem, syncPhysicalArea);
        return syncResourceItem;
    }


    SyncBoxItem createSyncBoxItem(BoxItemType boxItemType, DecimalPosition position2d, double zRotation) {
        SyncBoxItem syncBoxItem = syncItemInstance.select(SyncBoxItem.class).get();
        SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
        syncPhysicalArea.init(syncBoxItem, boxItemType.getRadius(), boxItemType.isFixVerticalNorm(), boxItemType.getTerrainType(), position2d, zRotation);
        initAndAdd(boxItemType, syncBoxItem, syncPhysicalArea);
        return syncBoxItem;
    }

    SyncBoxItem createSyncBoxItemSlave(BoxItemType boxItemType, int syncItemId, DecimalPosition position2d, double zRotation) {
        SyncBoxItem syncBoxItem = syncItemInstance.select(SyncBoxItem.class).get();
        SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
        syncPhysicalArea.init(syncBoxItem, boxItemType.getRadius(), boxItemType.isFixVerticalNorm(), boxItemType.getTerrainType(), position2d, zRotation);
        initAndAddSlave(boxItemType, syncItemId, syncBoxItem, syncPhysicalArea);
        return syncBoxItem;
    }

    private void initAndAdd(ItemType itemType, SyncItem syncItem, SyncPhysicalArea syncPhysicalArea) {
        synchronized (items) {
            syncItem.init(lastItemId, itemType, syncPhysicalArea);
            SyncItem old = items.put(lastItemId, syncItem);
            if (old != null) {
                throw new IllegalArgumentException("SyncItemContainerService.initAndAdd(). Id is not free. New: " + syncItem + " old: " + old);
            }
            lastItemId++;
        }
        syncItem.getSyncPhysicalArea().setupPosition3d();
    }

    private void initAndAddSlave(ItemType itemType, int syncItemId, SyncItem syncItem, SyncPhysicalArea syncPhysicalArea) {
        synchronized (items) {
            syncItem.init(syncItemId, itemType, syncPhysicalArea);
            SyncItem old = items.put(syncItemId, syncItem);
            if (old != null) {
                throw new IllegalArgumentException("SyncItemContainerService.initAndAddSlave(). Id is not free. New: " + syncItem + " old: " + old);
            }
            lastItemId = Math.max(lastItemId + 1, syncItemId + 1);
        }
        if (syncItem.getSyncPhysicalArea().hasPosition()) {
            syncItem.getSyncPhysicalArea().setupPosition3d();
        }
    }

    private SyncPhysicalArea createSyncPhysicalArea(SyncBaseItem syncBaseItem, BaseItemType baseItemType, DecimalPosition position2d, double zRotation) {
        PhysicalAreaConfig physicalAreaConfig = baseItemType.getPhysicalAreaConfig();
        if (physicalAreaConfig.fulfilledMovable()) {
            SyncPhysicalMovable syncPhysicalMovable = syncPhysicalMovableInstance.get();
            syncPhysicalMovable.init(syncBaseItem, baseItemType.getPhysicalAreaConfig(), position2d, zRotation, null);
            return syncPhysicalMovable;
        } else {
            SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
            syncPhysicalArea.init(syncBaseItem, physicalAreaConfig.getRadius(), physicalAreaConfig.getFixVerticalNorm(), physicalAreaConfig.getTerrainType(), position2d, zRotation);
            return syncPhysicalArea;
        }
    }

    void destroySyncItem(SyncItem syncItem) {
        synchronized (items) {
            SyncItem removed = items.remove(syncItem.getId());
            if (removed == null) {
                logger.severe("Item did not belong to SyncItemContainerService: " + syncItem);
            }
        }
        removedDeadItemFromCell(syncItem);
        if (syncItem instanceof SyncBaseItem) {
            guardingItemServiceInstanceInstance.get().remove((SyncBaseItem) syncItem);
        }
    }

    public SyncItem getSyncItem(int id) {
        return items.get(id);
    }

    private SyncItem getSyncItemSave(int id) {
        SyncItem syncItem = getSyncItem(id);
        if (syncItem != null) {
            return syncItem;
        } else {
            throw new ItemDoesNotExistException(id);
        }
    }

    public SyncBaseItem getSyncBaseItemSave(int id) {
        return (SyncBaseItem) getSyncItemSave(id);
    }

    public SyncBaseItem getSyncBaseItem(int id) {
        return (SyncBaseItem) getSyncItem(id);
    }

    public SyncResourceItem getSyncResourceItem(int id) {
        return (SyncResourceItem) getSyncItem(id);
    }

    private boolean hasItemsInRange(DecimalPosition position, double radius) {
        return iterateOverItems(false, false, false, syncItem -> syncItem.getSyncPhysicalArea().overlap(position, radius));
    }

    public Collection<SyncBaseItem> findEnemyItems(final PlayerBase playerBase, PlaceConfig region) {
        Collection<SyncBaseItem> enemyItems = new ArrayList<>();
        iterateOverItems(false, false, null, (ItemIteratorHandler<Void>) syncItem -> {
            if (syncItem instanceof SyncBaseItem && ((SyncBaseItem) syncItem).isEnemy(playerBase) && (region == null || region.checkInside(syncItem))) {
                enemyItems.add((SyncBaseItem) syncItem);
            }
            return null;
        });
        return enemyItems;
    }


    public boolean hasItemsInRange(Collection<DecimalPosition> positions, double radius) {
        for (DecimalPosition position : positions) {
            if (hasItemsInRange(position, radius)) {
                return true;
            }
        }
        return false;
    }

    public Collection<SyncBaseItem> findEnemyBaseItemWithPlace(Integer baseItemTypeId, PlayerBase playerBase, PlaceConfig placeConfig) {
        Collection<SyncBaseItem> result = new ArrayList<>();
        iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (baseItemTypeId != null && syncBaseItem.getItemType().getId() != baseItemTypeId) {
                return null;
            }
            if (!playerBase.isEnemy(syncBaseItem.getBase())) {
                return null;
            }
            if (placeConfig.checkInside(syncBaseItem)) {
                result.add(syncBaseItem);
            }
            return null;
        });
        return result;
    }

    public Collection<SyncBaseItem> findBaseItemInRect(Rectangle2D rectangle) {
        Collection<SyncBaseItem> result = new ArrayList<>();
        iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (syncBaseItem.getSyncPhysicalArea().overlap(rectangle)) {
                result.add(syncBaseItem);
            }
            return null;
        });
        return result;
    }

    public DecimalPosition getFreeRandomPosition(TerrainType terrainType, double radius, boolean excludeBotRealm, PlaceConfig placeConfig) {
        if (placeConfig.getPolygon2D() != null) {
            return GeometricUtil.findFreeRandomPosition(placeConfig.getPolygon2D(), decimalPosition -> {
                if (excludeBotRealm) {
                    return isFree(terrainType, decimalPosition, radius) && !botServices.get().isInRealm(decimalPosition);
                } else {
                    return isFree(terrainType, decimalPosition, radius);
                }
            });
        } else if (placeConfig.getPosition() != null) {
            if (placeConfig.getRadius() != null) {
                return GeometricUtil.findFreeRandomPosition(placeConfig.getPosition(), placeConfig.getRadius(), decimalPosition -> {
                    if (excludeBotRealm) {
                        return isFree(terrainType, decimalPosition, radius) && !botServices.get().isInRealm(decimalPosition);
                    } else {
                        return isFree(terrainType, decimalPosition, radius);
                    }
                });
            } else {
                return placeConfig.getPosition();
            }
        } else {
            throw new IllegalArgumentException("To find a random place, a polygon or a position must be set");
        }
    }

    private boolean isFree(TerrainType terrainType, DecimalPosition position, double radius) {
        return terrainService.getPathingAccess().isTerrainTypeAllowed(terrainType, position, radius) && !hasItemsInRange(position, radius);
    }

    public boolean isFree(DecimalPosition position, BaseItemType baseItemType) {
        double radius = baseItemType.getPhysicalAreaConfig().getRadius();
        return isFree(baseItemType.getPhysicalAreaConfig().getTerrainType(), position, radius);
    }

    public Collection<SyncResourceItem> findResourceItemWithPlace(int resourceItemTypeId, PlaceConfig resourceSelection) {
        Collection<SyncResourceItem> result = new ArrayList<>();
        iterateOverResourceItems(null, syncResourceItem -> {
            if (syncResourceItem.getItemType().getId() != resourceItemTypeId) {
                return null;
            }
            if (resourceSelection.checkInside(syncResourceItem)) {
                result.add(syncResourceItem);
            }
            return null;
        });
        return result;
    }

    public Collection<SyncBaseItem> getSyncBaseItems4BaseItemType(BaseItemType baseItemType) {
        Collection<SyncBaseItem> result = new ArrayList<>();
        iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (syncBaseItem.getBaseItemType().equals(baseItemType)) {
                result.add(syncBaseItem);
            }
            return null;
        });
        return result;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        List<SyncBaseItemInfo> syncBaseItemInfos = new ArrayList<>();
        iterateOverBaseItems(true, false, null, syncBaseItem -> {
            syncBaseItemInfos.add(syncBaseItem.getSyncInfo());
            return null;
        });
        return syncBaseItemInfos;
    }


    public void onPositionChanged(SyncItem syncItem, DecimalPosition oldPosition, DecimalPosition newPosition, boolean pathingService) {
        if (pathingService) {
            synchronized (pathingChangedItem) {
                pathingChangedItem.add((SyncBaseItem) syncItem);
            }
        } else {
            onPositionChanged(syncItem, oldPosition, newPosition);
        }
    }

    private void onPositionChanged(SyncItem syncItem, DecimalPosition oldPosition, DecimalPosition newPosition) {
        if (oldPosition != null && newPosition != null) {
            Index oldIndex = position2Index(oldPosition);
            Index newIndex = position2Index(newPosition);
            if (oldIndex.equals(newIndex)) {
                return;
            }
            removeFromCell(oldIndex, syncItem);
            putToCell(newIndex, syncItem);
        } else if (oldPosition == null && newPosition != null) {
            putToCell(position2Index(newPosition), syncItem);
        } else if (oldPosition != null) {
            removeFromCell(position2Index(oldPosition), syncItem);
        } else {
            logger.severe("SyncItemContainerService.onPositionChanged() Unexpected oldPosition == null && newPosition == null for: " + syncItem);
        }
    }

    public void afterPathingServiceTick() {
        synchronized (pathingChangedItem) {
            pathingChangedItem.forEach(syncBaseItem -> {
                if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                    logger.severe("SyncItemContainerService.afterPathingServiceTick() Received SyncBaseItem which can not move");
                    return;
                }
                onPositionChanged(syncBaseItem, syncBaseItem.getSyncPhysicalMovable().getOldPosition(), syncBaseItem.getSyncPhysicalMovable().getPosition2d());
            });
            pathingChangedItem.clear();
        }
    }

    private void removedDeadItemFromCell(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            if (((SyncBaseItem) syncItem).isContainedIn()) {
                return;
            }
        }
        Index cellIndex = position2Index(syncItem.getSyncPhysicalArea().getPosition2d());
        removeFromCell(cellIndex, syncItem);
    }

    private void putToCell(Index incellIndexex, SyncItem syncItem) {
        SyncItemContainerCell cell;
        synchronized (cells) {
            cell = cells.get(incellIndexex);
            if (cell == null) {
                cell = new SyncItemContainerCell();
                cells.put(incellIndexex, cell);
            }
        }
        cell.add(syncItem);
    }

    private void removeFromCell(Index cellIndex, SyncItem syncItem) {
        synchronized (cells) {
            SyncItemContainerCell cell = cells.get(cellIndex);
            if (cell == null) {
                logger.severe("SyncItemContainerService.removeFromCell() SyncItem has not been added to cell before: " + syncItem);
            } else {
                cell.remove(syncItem);
                if (cell.isEmpty()) {
                    cells.remove(cellIndex);
                }
            }
        }
    }

    private Index position2Index(DecimalPosition decimalPosition) {
        return new Index(MathHelper.shiftFloor(decimalPosition.getX(), CELL_LENGTH_EXPONENT), MathHelper.shiftFloor(decimalPosition.getY(), CELL_LENGTH_EXPONENT));
    }

    public void iterateCellQuadItem(DecimalPosition center, double width, Consumer<SyncItem> callback) {
        List<Index> cellIndexes = GeometricUtil.rasterizeRectangleInclusive(Rectangle2D.generateRectangleFromMiddlePoint(center, width, width), CELL_LENGTH);
        cellIndexes.forEach(cellIndex -> {
            SyncItemContainerCell cell = cells.get(cellIndex);
            if (cell != null) {
                cell.get().forEach(callback);
            }
        });
    }

    public void iterateCellRadiusItem(DecimalPosition center, double radius, Consumer<SyncItem> callback) {
        iterateCellQuadItem(center, 2.0 * radius + CELL_LENGTH, syncItem -> {
            if(syncItem.getSyncPhysicalArea().getPosition2d().getDistance(center) <= radius) {
                callback.accept(syncItem);
            }
        });
    }

    public void iterateCellQuadBaseItem(DecimalPosition center, double width, Consumer<SyncBaseItem> callback) {
        List<Index> cellIndexes = GeometricUtil.rasterizeRectangleInclusive(Rectangle2D.generateRectangleFromMiddlePoint(center, width, width), CELL_LENGTH);
        cellIndexes.forEach(cellIndex -> {
            SyncItemContainerCell cell = cells.get(cellIndex);
            if (cell != null) {
                cell.get().stream().filter(syncItem -> syncItem instanceof SyncBaseItem).forEach(syncItem -> callback.accept((SyncBaseItem) syncItem));
            }
        });
    }

    public SyncBaseItem findNearestHumanBaseItemOnPathCell(SimplePath simplePath, double width) {
        for (DecimalPosition wayPosition : simplePath.getWayPositions()) {
            DoubleHolder<SyncBaseItem, Double> best = new DoubleHolder<>();
            iterateCellQuadBaseItem(wayPosition, width, syncBaseItem -> {
                if (syncBaseItem.getBase().getCharacter().isHuman()) {
                    double distance = syncBaseItem.getSyncPhysicalArea().getPosition2d().getDistance(wayPosition);
                    if (best.getO1() != null) {
                        if (best.getO2() > distance) {
                            best.setO1(syncBaseItem);
                            best.setO2(distance);
                        }
                    } else {
                        best.setO1(syncBaseItem);
                        best.setO2(distance);
                    }
                }
            });
            if (best.getO1() != null) {
                return best.getO1();
            }
        }
        return null;
    }

    public Collection<SyncItem> getSyncItemsCopy() {
        return new ArrayList<>(items.values());
    }

}