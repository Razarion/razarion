package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 13.09.2016.
 */
public class BotMoveCommandConfig {
    private int botId;
    private int baseItemTypeId;
    private DecimalPosition decimalPosition;

    public int getBotId() {
        return botId;
    }

    public BotMoveCommandConfig setBotId(int botId) {
        this.botId = botId;
        return this;
    }

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
