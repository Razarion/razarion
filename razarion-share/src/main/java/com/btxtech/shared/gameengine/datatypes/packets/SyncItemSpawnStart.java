package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.datatypes.DecimalPosition;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
public class SyncItemSpawnStart {
    private int baseItemTypeId;
    private DecimalPosition position;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }
}
