package com.btxtech.shared.gameengine.datatypes.config.bot;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;

/**
 * User: beat
 * Date: 14.06.12
 * Time: 12:41
 */
@Portable
public class BotEnragementStateConfig {
    private String name;
    private Collection<BotItemConfig> botItems;
    private Integer enrageUpKills;

    public BotEnragementStateConfig setName(String name) {
        this.name = name;
        return this;
    }

    public BotEnragementStateConfig setBotItems(Collection<BotItemConfig> botItems) {
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

    public Collection<BotItemConfig> getBotItems() {
        return botItems;
    }

    public Integer getEnrageUpKills() {
        return enrageUpKills;
    }
}
