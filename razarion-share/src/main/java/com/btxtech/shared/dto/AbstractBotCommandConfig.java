package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class AbstractBotCommandConfig<T extends AbstractBotCommandConfig> {
    private int botAuxiliaryId;

    public int getBotAuxiliaryId() {
        return botAuxiliaryId;
    }

    public T setBotAuxiliaryId(int botAuxiliaryId) {
        this.botAuxiliaryId = botAuxiliaryId;
        return (T) this;
    }
}
