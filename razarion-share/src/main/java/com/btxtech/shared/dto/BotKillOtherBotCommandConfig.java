package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotKillOtherBotCommandConfig extends BotKillBaseCommandConfig<BotKillOtherBotCommandConfig> {
    private int targetBotId;

    public int getTargetBotId() {
        return targetBotId;
    }

    public BotKillOtherBotCommandConfig setTargetBotId(int targetBotId) {
        this.targetBotId = targetBotId;
        return this;
    }
}
