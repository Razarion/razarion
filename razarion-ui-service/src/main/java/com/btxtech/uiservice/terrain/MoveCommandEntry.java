package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

import java.util.Collection;

public class MoveCommandEntry {
    private final Collection<SyncBaseItemSimpleDto> movables;
    private final DecimalPosition terrainPosition;

    public MoveCommandEntry(Collection<SyncBaseItemSimpleDto> movables, DecimalPosition terrainPosition) {
        this.movables = movables;
        this.terrainPosition = terrainPosition;
    }

    public Collection<SyncBaseItemSimpleDto> getMovables() {
        return movables;
    }

    public DecimalPosition getTerrainPosition() {
        return terrainPosition;
    }
}
