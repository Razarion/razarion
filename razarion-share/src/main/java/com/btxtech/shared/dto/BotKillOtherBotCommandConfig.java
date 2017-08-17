package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotKillOtherBotCommandConfig extends BotKillBaseCommandConfig<BotKillOtherBotCommandConfig> {
    private Integer targetBotAuxiliaryId;

    public Integer getTargetBotAuxiliaryId() {
        return targetBotAuxiliaryId;
    }

    public BotKillOtherBotCommandConfig setTargetBotAuxiliaryId(Integer targetBotAuxiliaryId) {
        this.targetBotAuxiliaryId = targetBotAuxiliaryId;
        return this;
    }
}
