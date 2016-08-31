package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncItemPosition;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    public Collection<SyncItem> getSyncItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public <T> T iterateOverItems(boolean includeNoPosition, boolean includeDead, T defaultReturn, ItemIteratorHandler<T> itemIteratorHandler) {
        synchronized (items) {
            for (SyncItem syncItem : items.values()) {
                if (!includeDead && !syncItem.isAlive()) {
                    continue;
                }
                if (!includeNoPosition && !syncItem.getSyncItemPosition().hasPosition()) {
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

    public <T extends SyncItem> T createSyncItem(Class<T> clazz, ItemType itemType, SyncItemPosition syncItemPosition) {
        T t = instance.select(clazz).get();
        synchronized (items) {
            t.init(lastItemId, itemType, syncItemPosition);
            items.put(lastItemId, t);
            lastItemId++;
        }
        return t;
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
        return iterateOverItems(false, false, false, syncItem -> syncItem.getSyncItemPosition().overlap(position, radius));
    }
}
