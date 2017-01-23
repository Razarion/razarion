package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;

import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.01.2017.
 */
public class SyncItemMonitor {
    private int syncItemId;
    private DecimalPosition position2d;
    private Vertex position3d;
    private Consumer<SyncItemMonitor> releaseMonitorCallback;
    private int monitorCount;

    public SyncItemMonitor(SyncItemSimpleDto syncItemSimpleDto, Consumer<SyncItemMonitor> releaseMonitorCallback) {
        syncItemId = syncItemSimpleDto.getId();
        position2d = syncItemSimpleDto.getPosition2d();
        position3d = syncItemSimpleDto.getPosition3d();
        this.releaseMonitorCallback = releaseMonitorCallback;
    }

    public int getSyncItemId() {
        return syncItemId;
    }

    public DecimalPosition getPosition2d() {
        return position2d;
    }

    public Vertex getPosition3d() {
        return position3d;
    }

    public void increaseMonitorCount() {
        monitorCount++;
    }

    public void update(SyncBaseItemSimpleDto syncItemSimpleDto) {
        if (position2d.equals(syncItemSimpleDto.getPosition2d())) {
            return;
        }
        position2d = syncItemSimpleDto.getPosition2d();
        position3d = syncItemSimpleDto.getPosition3d();
    }

    public void release() {
        monitorCount--;
        if (monitorCount <= 0 && releaseMonitorCallback != null) {
            releaseMonitorCallback.accept(this);
        }
    }
}
