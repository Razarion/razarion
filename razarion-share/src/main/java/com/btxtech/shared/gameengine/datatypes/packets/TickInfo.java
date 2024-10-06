package com.btxtech.shared.gameengine.datatypes.packets;

import org.dominokit.jackson.annotation.JSONMapper;

import java.util.List;

@JSONMapper
public class TickInfo {
    private double tickCount;
    private List<SyncBaseItemInfo> syncBaseItemInfos;

    public double getTickCount() {
        return tickCount;
    }

    public void setTickCount(double tickCount) {
        this.tickCount = tickCount;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncBaseItemInfos;
    }

    public void setSyncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        this.syncBaseItemInfos = syncBaseItemInfos;
    }

    public TickInfo tickCount(double tickCount) {
        setTickCount(tickCount);
        return this;
    }

    public TickInfo syncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        setSyncBaseItemInfos(syncBaseItemInfos);
        return this;
    }
}
