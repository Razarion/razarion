package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Singleton
public class SyncItemContainerService {
    private Logger logger = Logger.getLogger(SyncItemContainerService.class.getName());
    private int lastItemId = 0;
    private final HashMap<Integer, SyncItem> items = new HashMap<>();
    @Inject
    private Instance<SyncItem> instance;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        items.clear();
        lastItemId = 0;
    }

    public Collection<SyncItem> getSyncItems() {
        return Collections.unmodifiableCollection(items.values());
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
    public <T> T iterateOverBaseItems(boolean includeNoPosition, boolean includeDead, SyncBaseItem ignoreMe, T defaultReturn, Function<SyncBaseItem, T> itemIteratorHandler) {
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
    public <T> T iterateOverResourceItems(boolean includeDead, SyncBaseItem ignoreMe, T defaultReturn, Function<SyncResourceItem, T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (ignoreMe != null && ignoreMe.equals(syncItem)) {
                    continue;
                }

                if (!(syncItem instanceof SyncResourceItem)) {
                    continue;
                }

                SyncResourceItem resourceItem = (SyncResourceItem) syncItem;
                if (!includeDead && !resourceItem.isAlive()) {
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

    public SyncBaseItem createSyncBaseItem(BaseItemType baseItemType, Vertex position) {
        SyncBaseItem syncBaseItem = instance.select(SyncBaseItem.class).get();
        SyncPhysicalArea syncPhysicalArea = createSyncPhysicalArea(syncBaseItem, baseItemType, position);
        synchronized (items) {
            syncBaseItem.init(lastItemId, baseItemType, syncPhysicalArea);
            items.put(lastItemId, syncBaseItem);
            lastItemId++;
        }
        return syncBaseItem;
    }

    public SyncResourceItem createSyncResourceItem(ResourceItemType resourceItemType, Vertex position, double zRotation) {
        SyncResourceItem syncResourceItem = instance.select(SyncResourceItem.class).get();
        SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea(syncResourceItem, resourceItemType.getRadius(), position, Vertex.Z_NORM, zRotation);
        synchronized (items) {
            syncResourceItem.init(lastItemId, resourceItemType, syncPhysicalArea);
            items.put(lastItemId, syncResourceItem);
            lastItemId++;
        }
        return syncResourceItem;
    }

    private SyncPhysicalArea createSyncPhysicalArea(SyncBaseItem syncBaseItem, BaseItemType baseItemType, Vertex position) {
        PhysicalAreaConfig physicalAreaConfig = baseItemType.getPhysicalAreaConfig();
        if (physicalAreaConfig.fulfilledMovable()) {
            return new SyncPhysicalMovable(syncBaseItem, physicalAreaConfig, position, Vertex.Z_NORM, 0, null);
        } else if (physicalAreaConfig.fulfilledDirectional()) {
            throw new UnsupportedOperationException();
        } else {
            return new SyncPhysicalArea(syncBaseItem, baseItemType.getPhysicalAreaConfig(), position, Vertex.Z_NORM, 0);
        }
    }

    public void destroySyncItem(SyncItem syncItem) {
        synchronized (items) {
            SyncItem removed = items.remove(syncItem.getId());
            if (removed == null) {
                logger.severe("Item did not belong to SyncItemContainerService: " + syncItem);
            }
        }
    }

    public boolean hasItemsInRange(DecimalPosition position, double radius) {
        return iterateOverItems(false, false, false, syncItem -> syncItem.getSyncPhysicalArea().overlap(position, radius));
    }

    public SyncItem getSyncItem(int id) {
        SyncItem syncItem = items.get(id);
        if (syncItem != null) {
            return syncItem;
        } else {
            throw new ItemDoesNotExistException(id);
        }
    }

    public SyncBaseItem getSyncBaseItems(int id) {
        return (SyncBaseItem) getSyncItem(id);
    }

    public SyncItem findItemAtPosition(DecimalPosition position) {
        return iterateOverBaseItems(false, false, null, syncItem -> {
            if (syncItem.getSyncPhysicalArea().overlap(position)) {
                return syncItem;
            } else {
                return null;
            }
        });
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

    public Collection<SyncResourceItem> findResourceItemWithPlace(int resourceItemTypeId, PlaceConfig resourceSelection) {
        Collection<SyncResourceItem> result = new ArrayList<>();
        iterateOverResourceItems(false, null, null, syncResourceItem -> {
            if (syncResourceItem.getItemType().getId() != resourceItemTypeId) {
                return null;
            }
            if (syncResourceItem.getSyncPhysicalArea().contains(resourceSelection)) {
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
}