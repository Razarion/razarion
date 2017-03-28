package com.btxtech.gameengine;

import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

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
                    SyncBaseItemSimpleDto simpleDto = ((SyncBaseItem) syncItem).createSyncBaseItemSimpleDto();
                    // Alive
                    if (!simpleDto.checkSpawning() && simpleDto.checkBuildup() && simpleDto.checkHealth()) {
                        aliveModelMatrices.add(new ModelMatrices(simpleDto.getModel(), simpleDto.getInterpolatableVelocity(), nativeMatrixFactory));
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
