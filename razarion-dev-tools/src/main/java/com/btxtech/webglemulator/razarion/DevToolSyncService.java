package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class DevToolSyncService extends SyncService {
    @Override
    protected void sendSyncBaseItems(List<SyncBaseItemInfo> infos) {
        throw new UnsupportedOperationException("... ONLY IN SERVER ...");
    }
}
