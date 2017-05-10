package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2017.
 */
public class SlaveSyncItemInfo {
    private Integer actualBaseId;
    private List<SyncBaseItemInfo> syncBaseItemInfos;
    private List<PlayerBaseInfo> playerBaseInfos;
    private List<SyncResourceItemInfo> syncResourceItemInfos;

    public Integer getActualBaseId() {
        return actualBaseId;
    }

    public SlaveSyncItemInfo setActualBaseId(Integer actualBaseId) {
        this.actualBaseId = actualBaseId;
        return this;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncBaseItemInfos;
    }

    public SlaveSyncItemInfo setSyncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        this.syncBaseItemInfos = syncBaseItemInfos;
        return this;
    }

    public List<PlayerBaseInfo> getPlayerBaseInfos() {
        return playerBaseInfos;
    }

    public SlaveSyncItemInfo setPlayerBaseInfos(List<PlayerBaseInfo> playerBaseInfos) {
        this.playerBaseInfos = playerBaseInfos;
        return this;
    }

    public List<SyncResourceItemInfo> getSyncResourceItemInfos() {
        return syncResourceItemInfos;
    }

    public SlaveSyncItemInfo setSyncResourceItemInfos(List<SyncResourceItemInfo> syncResourceItemInfos) {
        this.syncResourceItemInfos = syncResourceItemInfos;
        return this;
    }


}
