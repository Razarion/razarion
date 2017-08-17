package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 13.09.2016.
 */
public class BotMoveCommandConfig extends AbstractBotCommandConfig<BotMoveCommandConfig> {
    private Integer baseItemTypeId;
    private DecimalPosition targetPosition;

    public Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public BotMoveCommandConfig setBaseItemTypeId(Integer baseItemTypeId) {
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
