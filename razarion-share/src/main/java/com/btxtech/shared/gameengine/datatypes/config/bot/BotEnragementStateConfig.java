package com.btxtech.shared.gameengine.datatypes.config.bot;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;
import java.util.stream.Collectors;

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

    public BotEnragementStateConfig cloneWithAbsolutePosition(DecimalPosition absoluteCenter) {
        BotEnragementStateConfig botEnragementStateConfig = new BotEnragementStateConfig();
        botEnragementStateConfig.name = name;
        botEnragementStateConfig.botItems = botItems.stream().map(botItemConfig -> botItemConfig.cloneWithAbsolutePosition(absoluteCenter)).collect(Collectors.toList());
        botEnragementStateConfig.enrageUpKills = enrageUpKills;
        return botEnragementStateConfig;
    }
}
