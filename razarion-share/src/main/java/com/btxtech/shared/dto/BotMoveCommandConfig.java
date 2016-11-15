package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 13.09.2016.
 */
public class BotMoveCommandConfig extends AbstractBotCommandConfig<BotMoveCommandConfig> {
    private int baseItemTypeId;
    private DecimalPosition targetPosition;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public BotMoveCommandConfig setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public DecimalPosition getTargetPosition() {
        return targetPosition;
    }

    public BotMoveCommandConfig setTargetPosition(DecimalPosition targetPosition) {
        this.targetPosition = targetPosition;
        return this;
    }
}
