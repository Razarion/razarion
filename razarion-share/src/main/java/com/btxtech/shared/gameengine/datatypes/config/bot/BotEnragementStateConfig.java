package com.btxtech.shared.gameengine.datatypes.config.bot;

import java.util.List;

/**
 * User: beat
 * Date: 14.06.12
 * Time: 12:41
 */
public class BotEnragementStateConfig {
    private String name;
    private List<BotItemConfig> botItems;
    private Integer enrageUpKills;

    public BotEnragementStateConfig setName(String name) {
        this.name = name;
        return this;
    }

    public BotEnragementStateConfig setBotItems(List<BotItemConfig> botItems) {
        this.botItems = botItems;
        return this;
    }

    public BotEnragementStateConfig setEnrageUpKills(Integer enrageUpKills) {
        this.enrageUpKills = enrageUpKills;
        return this;
    }

    public String getName() {
        return name;
    }

    public List<BotItemConfig> getBotItems() {
        return botItems;
    }

    public Integer getEnrageUpKills() {
        return enrageUpKills;
    }
}
