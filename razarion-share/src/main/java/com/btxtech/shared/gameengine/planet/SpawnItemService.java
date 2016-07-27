package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncItemPosition;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncSpawnItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Singleton
public class SpawnItemService {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SyncItemContainerService container;
    @Inject
    private ActivityService activityService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainService terrainService;
    final private Collection<SyncSpawnItem> syncSpawnItems = new ArrayList<>();

    public void spawnSyncItem(BaseItemType toBeSpanned, Index position, PlayerBase playerBase) {
        if (toBeSpanned.getSpawnItemType() == null) {
            throw new IllegalArgumentException("No SpawnItemType found for: " + toBeSpanned);
        }
        Vertex vertexPosition = terrainService.getVertexAt(position);
        SyncSpawnItem syncSpawnItem = container.createSyncItem(SyncSpawnItem.class, toBeSpanned.getSpawnItemType(), new SyncItemPosition(vertexPosition, toBeSpanned.getRadius()));
        syncSpawnItem.setup(toBeSpanned, playerBase);
        synchronized (syncSpawnItems) {
            syncSpawnItems.add(syncSpawnItem);
        }
        activityService.onSpawnSyncItem(syncSpawnItem);
    }

    public void tick() {
        synchronized (syncSpawnItems) {
            for (Iterator<SyncSpawnItem> iterator = syncSpawnItems.iterator(); iterator.hasNext(); ) {
                try {
                    SyncSpawnItem syncSpawnItem = iterator.next();
                    syncSpawnItem.tick();
                    if (syncSpawnItem.isFinished()) {
                        iterator.remove();
                        activityService.onSpawnSyncItemFinished(syncSpawnItem);
                        container.destroySyncItem(syncSpawnItem);
                        baseItemService.createSyncBaseItem((BaseItemType) syncSpawnItem.getToBeCreated(), syncSpawnItem.getSyncItemPosition().getPosition(), syncSpawnItem.getPlayerBase(), null);
                    }
                } catch(Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
    }

    public Collection<SyncSpawnItem> getSyncSpawnItems() {
        return Collections.unmodifiableCollection(syncSpawnItems);
    }
}
