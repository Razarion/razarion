package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Singleton
public class SyncItemContainerService {
    private static final int MAX_TRIES = 10000;
    private Logger logger = Logger.getLogger(SyncItemContainerService.class.getName());
    private int lastItemId = 0;
    private final HashMap<Integer, SyncItem> items = new HashMap<>();
    @Inject
    private Instance<SyncItem> syncItemInstance;
    @Inject
    @Named(SyncItem.SYNC_PHYSICAL_AREA)
    private Instance<SyncPhysicalArea> syncPhysicalAreaInstance;
    @Inject
    @Named(SyncItem.SYNC_PHYSICAL_MOVABLE)
    private Instance<SyncPhysicalMovable> syncPhysicalMovableInstance;
    @Inject
    private ObstacleContainer obstacleContainer;

    public void clear() {
        items.clear();
        lastItemId = 0;
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
        syncPhysicalArea.init(syncResourceItem, resourceItemType.getRadius(), resourceItemType.isFixVerticalNorm(), position2d, zRotation);
        initAndAdd(resourceItemType, syncResourceItem, syncPhysicalArea);
        return syncResourceItem;
    }

    SyncBoxItem createSyncBoxItem(BoxItemType boxItemType, DecimalPosition position2d, double zRotation) {
        SyncBoxItem syncBoxItem = syncItemInstance.select(SyncBoxItem.class).get();
        SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
        syncPhysicalArea.init(syncBoxItem, boxItemType.getRadius(), boxItemType.isFixVerticalNorm(), position2d, zRotation);
        initAndAdd(boxItemType, syncBoxItem, syncPhysicalArea);
        return syncBoxItem;
    }

    private void initAndAdd(ItemType itemType, SyncItem syncItem, SyncPhysicalArea syncPhysicalArea) {
        synchronized (items) {
            syncItem.init(lastItemId, itemType, syncPhysicalArea);
            items.put(lastItemId, syncItem);
            lastItemId++;
        }
        syncItem.getSyncPhysicalArea().setupPosition3d();
    }

    private void initAndAddSlave(ItemType itemType, int syncItemId, SyncItem syncItem, SyncPhysicalArea syncPhysicalArea) {
        synchronized (items) {
            syncItem.init(syncItemId, itemType, syncPhysicalArea);
            items.put(syncItemId, syncItem);
        }
        syncItem.getSyncPhysicalArea().setupPosition3d();
    }

    private SyncPhysicalArea createSyncPhysicalArea(SyncBaseItem syncBaseItem, BaseItemType baseItemType, DecimalPosition position2d, double zRotation) {
        PhysicalAreaConfig physicalAreaConfig = baseItemType.getPhysicalAreaConfig();
        if (physicalAreaConfig.fulfilledMovable()) {
            SyncPhysicalMovable syncPhysicalMovable = syncPhysicalMovableInstance.get();
            syncPhysicalMovable.init(syncBaseItem, baseItemType.getPhysicalAreaConfig(), position2d, zRotation, null);
            return syncPhysicalMovable;
        } else {
            SyncPhysicalArea syncPhysicalArea = syncPhysicalAreaInstance.get();
            syncPhysicalArea.init(syncBaseItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), position2d, zRotation);
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
    }

    private SyncItem getSyncItem(int id) {
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

    private boolean hasItemsInRange(DecimalPosition position, double radius) {
        return iterateOverItems(false, false, false, syncItem -> syncItem.getSyncPhysicalArea().overlap(position, radius));
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

    public DecimalPosition getFreeRandomPosition(double radius, PlaceConfig placeConfig) {
        Polygon2D polygon = placeConfig.getPolygon2D();
        if (polygon == null) {
            throw new IllegalArgumentException("To find a random place, a polygon must be set");
        }

        Rectangle2D aabb = polygon.toAabb();
        Random random = new Random();
        for (int i = 0; i < MAX_TRIES; i++) {
            double width = random.nextDouble() * aabb.width();
            double height = random.nextDouble() * aabb.height();
            DecimalPosition possiblePosition = aabb.getStart().add(width, height);

            if (!isFree(possiblePosition, radius)) {
                continue;
            }
            return possiblePosition;
        }
        throw new PlaceCanNotBeFoundException(radius, placeConfig);
    }

    private boolean isFree(DecimalPosition position, double radius) {
        return !obstacleContainer.overlap(position, radius) && !hasItemsInRange(position, radius);
    }

    public boolean isFree(DecimalPosition position, BaseItemType baseItemType) {
        double radius = baseItemType.getPhysicalAreaConfig().getRadius();
        return isFree(position, radius);
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
        iterateOverBaseItems(false, false, null, syncBaseItem -> {
            syncBaseItemInfos.add(syncBaseItem.getSyncInfo());
            return null;
        });
        return syncBaseItemInfos;
    }
}