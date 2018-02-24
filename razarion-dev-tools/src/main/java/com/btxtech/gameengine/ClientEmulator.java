package com.btxtech.gameengine;

import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 11.01.2017.
 */
@ApplicationScoped
public class ClientEmulator {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private long lastUpdateTimeStamp;
    private final Collection<ModelMatrices> aliveModelMatrices = new ArrayList<>();

    void onTick() {
        lastUpdateTimeStamp = System.currentTimeMillis();
        synchronized (aliveModelMatrices) {
            aliveModelMatrices.clear();
            syncItemContainerService.iterateOverItems(false, false, null, syncItem -> {
                if (syncItem instanceof SyncBaseItem) {
                    SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                    // Alive
                    if (!syncBaseItem.isSpawning() && syncBaseItem.isBuildup() && syncBaseItem.isHealthy()) {
                        if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                            aliveModelMatrices.add(new ModelMatrices(syncBaseItem.getSyncPhysicalArea().getModelMatrices(), syncBaseItem.getSyncPhysicalMovable().setupInterpolatableVelocity(), nativeMatrixFactory));
                        } else {
                            aliveModelMatrices.add(new ModelMatrices(syncBaseItem.getSyncPhysicalArea().getModelMatrices(), nativeMatrixFactory));
                        }
                    }
                }
                return null;
            });
        }
    }

    public Collection<ModelMatrices> getAliveModelMatrices() {
        synchronized (aliveModelMatrices) {
            return new ArrayList<>(aliveModelMatrices);
        }
    }

    public long getLastUpdateTimeStamp() {
        return lastUpdateTimeStamp;
    }
}
