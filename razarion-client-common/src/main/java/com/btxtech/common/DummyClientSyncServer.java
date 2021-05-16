package com.btxtech.common;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class DummyClientSyncServer extends SyncService {
    @Override
    protected void sendSyncBaseItems(List<SyncBaseItemInfo> infos) {
        // Called in client-worker when running as Master -> ignore
    }
}
