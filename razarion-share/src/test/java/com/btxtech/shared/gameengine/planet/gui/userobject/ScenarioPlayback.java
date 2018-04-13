package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.List;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class ScenarioPlayback {
    private List<List<SyncBaseItemInfo>> syncBaseItemInfo;

    public List<List<SyncBaseItemInfo>> getSyncBaseItemInfo() {
        return syncBaseItemInfo;
    }

    public ScenarioPlayback setSyncBaseItemInfo(List<List<SyncBaseItemInfo>> syncBaseItemInfo) {
        this.syncBaseItemInfo = syncBaseItemInfo;
        return this;
    }

    public int getTickCount() {
        if (syncBaseItemInfo != null) {
            return syncBaseItemInfo.size();
        } else {
            return 0;
        }
    }
}
