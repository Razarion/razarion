package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class AbstractBotCommandConfig<T extends AbstractBotCommandConfig> {
    private Integer botAuxiliaryId;

    public Integer getBotAuxiliaryId() {
        return botAuxiliaryId;
    }

    public T setBotAuxiliaryId(Integer botAuxiliaryId) {
        this.botAuxiliaryId = botAuxiliaryId;
        return (T) this;
    }
}
