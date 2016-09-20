package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalDirectionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalMovableConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
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

    public <T extends SyncItem> T createSyncItem(Class<T> clazz, ItemType itemType, Vertex position) {
        T t = instance.select(clazz).get();
        SyncPhysicalArea syncPhysicalArea = createSyncPhysicalArea(t, itemType, position);
        synchronized (items) {
            t.init(lastItemId, itemType, syncPhysicalArea);
            items.put(lastItemId, t);
            lastItemId++;
        }
        return t;
    }

    private SyncPhysicalArea createSyncPhysicalArea(SyncItem syncItem, ItemType itemType, Vertex position) {
        if (itemType.getPhysicalAreaConfig() instanceof PhysicalMovableConfig) {
            return new SyncPhysicalMovable(syncItem, (PhysicalMovableConfig) itemType.getPhysicalAreaConfig(), position, Vertex.Z_NORM, 0, null);
        } else if (itemType.getPhysicalAreaConfig() instanceof PhysicalDirectionConfig) {
            throw new UnsupportedOperationException();
        } else {
            return new SyncPhysicalArea(syncItem, itemType.getPhysicalAreaConfig(), position, Vertex.Z_NORM);
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
}
