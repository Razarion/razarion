package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 02.10.2018.
 */
public abstract class SyncService {
    // TODO private Set<SyncBaseItem> changedPathings = new HashSet<>();
    private final Set<SyncBaseItem> itemsToSend = new HashSet<>();
    private GameEngineMode gameEngineMode;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        gameEngineMode = planetActivationEvent.getGameEngineMode();
    }

    public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }
        synchronized (itemsToSend) {
            itemsToSend.add(syncBaseItem);
        }
    }

    public void afterTick() {
        if(itemsToSend.isEmpty()) {
            return;
        }
        List<SyncBaseItemInfo> infos = new LinkedList<>();
        synchronized (itemsToSend) {
            itemsToSend.stream().filter(SyncBaseItem::isAlive).map(SyncBaseItem::getSyncInfo).forEach(infos::add);
            itemsToSend.clear();
        }
        sendSyncBaseItems(infos);
    }

    protected abstract void sendSyncBaseItems(List<SyncBaseItemInfo> infos);
}
