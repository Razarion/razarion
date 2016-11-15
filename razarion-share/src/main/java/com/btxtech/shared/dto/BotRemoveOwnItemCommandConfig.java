package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotRemoveOwnItemCommandConfig extends AbstractBotCommandConfig<BotRemoveOwnItemCommandConfig> {
    private int baseItemType2RemoveId;

    public int getBaseItemType2RemoveId() {
        return baseItemType2RemoveId;
    }

    public BotRemoveOwnItemCommandConfig setBaseItemType2RemoveId(int baseItemType2RemoveId) {
        this.baseItemType2RemoveId = baseItemType2RemoveId;
        return this;
    }
}
