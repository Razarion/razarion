package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import org.dominokit.jackson.annotation.JSONMapper;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2017.
 */
@JSONMapper
public class InitialSlaveSyncItemInfo {
    private double tickCount;
    private Integer actualBaseId;
    private List<SyncBaseItemInfo> syncBaseItemInfos;
    private List<PlayerBaseInfo> playerBaseInfos;
    private List<SyncResourceItemInfo> syncResourceItemInfos;
    private List<SyncBoxItemInfo> syncBoxItemInfos;

    public double getTickCount() {
        return tickCount;
    }

    public InitialSlaveSyncItemInfo setTickCount(double tickCount) {
        this.tickCount = tickCount;
        return this;
    }

    public Integer getActualBaseId() {
        return actualBaseId;
    }

    public InitialSlaveSyncItemInfo setActualBaseId(Integer actualBaseId) {
        this.actualBaseId = actualBaseId;
        return this;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncBaseItemInfos;
    }

    public InitialSlaveSyncItemInfo setSyncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        this.syncBaseItemInfos = syncBaseItemInfos;
        return this;
    }

    public List<PlayerBaseInfo> getPlayerBaseInfos() {
        return playerBaseInfos;
    }

    public InitialSlaveSyncItemInfo setPlayerBaseInfos(List<PlayerBaseInfo> playerBaseInfos) {
        this.playerBaseInfos = playerBaseInfos;
        return this;
    }

    public List<SyncResourceItemInfo> getSyncResourceItemInfos() {
        return syncResourceItemInfos;
    }

    public InitialSlaveSyncItemInfo setSyncResourceItemInfos(List<SyncResourceItemInfo> syncResourceItemInfos) {
        this.syncResourceItemInfos = syncResourceItemInfos;
        return this;
    }

    public List<SyncBoxItemInfo> getSyncBoxItemInfos() {
        return syncBoxItemInfos;
    }

    public InitialSlaveSyncItemInfo setSyncBoxItemInfos(List<SyncBoxItemInfo> syncBoxItemInfos) {
        this.syncBoxItemInfos = syncBoxItemInfos;
        return this;
    }
}
