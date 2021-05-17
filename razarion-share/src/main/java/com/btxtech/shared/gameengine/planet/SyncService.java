package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 02.10.2018.
 */
public abstract class SyncService {
    // TODO private Set<SyncBaseItem> changedPathings = new HashSet<>();
    private final List<SyncBaseItem> itemsToSend = new LinkedList<>();
    private final Set<Integer> ids = new HashSet<>();
    private GameEngineMode gameEngineMode;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        gameEngineMode = planetActivationEvent.getGameEngineMode();
    }

    public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }
        synchronized (itemsToSend) {
            if (!ids.contains(syncBaseItem.getId())) {
                ids.add(syncBaseItem.getId());
                itemsToSend.add(syncBaseItem);
            }
        }
    }

    public void afterTick(double tickCount) {
        if (itemsToSend.isEmpty()) {
            return;
        }
        TickInfo tickInfo = new TickInfo();
        tickInfo.setTickCount(tickCount);
        synchronized (itemsToSend) {
            tickInfo.setSyncBaseItemInfos(itemsToSend.stream().filter(SyncBaseItem::isAlive).map(SyncBaseItem::getSyncInfo).collect(Collectors.toList()));
            ids.clear();
            itemsToSend.clear();
        }

        sendTickInfo(tickInfo);
    }

    protected abstract void sendTickInfo(TickInfo tickInfo);
}
