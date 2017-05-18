package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotKillOtherBotCommandConfig extends BotKillBaseCommandConfig<BotKillOtherBotCommandConfig> {
    private int targetBotAuxiliaryId;

    public int getTargetBotAuxiliaryId() {
        return targetBotAuxiliaryId;
    }

    public BotKillOtherBotCommandConfig setTargetBotAuxiliaryId(int targetBotAuxiliaryId) {
        this.targetBotAuxiliaryId = targetBotAuxiliaryId;
        return this;
    }
}
