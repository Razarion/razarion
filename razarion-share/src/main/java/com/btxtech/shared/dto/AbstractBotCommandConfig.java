package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class AbstractBotCommandConfig<T extends AbstractBotCommandConfig> {
    private int botId;

    public int getBotId() {
        return botId;
    }

    public T setBotId(int botId) {
        this.botId = botId;
        return (T) this;
    }


}
