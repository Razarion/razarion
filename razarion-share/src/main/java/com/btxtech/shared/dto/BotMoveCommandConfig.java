package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 13.09.2016.
 */
public class BotMoveCommandConfig extends AbstractBotCommandConfig<BotMoveCommandConfig> {
    private int baseItemTypeId;
    private DecimalPosition decimalPosition;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public BotMoveCommandConfig setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public DecimalPosition getDecimalPosition() {
        return decimalPosition;
    }

    public BotMoveCommandConfig setDecimalPosition(DecimalPosition decimalPosition) {
        this.decimalPosition = decimalPosition;
        return this;
    }
}
