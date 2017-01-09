package com.btxtech.shared.gameengine.datatypes.workerdto;

import java.util.List;

/**
 * Created by Beat
 * 08.01.2017.
 */
public class GameInfo {
    private int resources;
    private List<SyncBaseItemSimpleDto> killed;

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public List<SyncBaseItemSimpleDto> getKilled() {
        return killed;
    }

    public void setKilled(List<SyncBaseItemSimpleDto> killed) {
        this.killed = killed;
    }
}
