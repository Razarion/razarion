package com.btxtech.shared.gameengine.datatypes.packets;

import com.btxtech.shared.datatypes.DecimalPosition;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
public class ProjectileFiredInfo {
    private int actorSyncBaseItemId;
    private int targetSyncBaseItemId;
    private DecimalPosition targetPosition;

    public int getActorSyncBaseItemId() {
        return actorSyncBaseItemId;
    }

    public void setActorSyncBaseItemId(int actorSyncBaseItemId) {
        this.actorSyncBaseItemId = actorSyncBaseItemId;
    }

    public int getTargetSyncBaseItemId() {
        return targetSyncBaseItemId;
    }

    public void setTargetSyncBaseItemId(int targetSyncBaseItemId) {
        this.targetSyncBaseItemId = targetSyncBaseItemId;
    }

    public DecimalPosition getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(DecimalPosition targetPosition) {
        this.targetPosition = targetPosition;
    }
}
