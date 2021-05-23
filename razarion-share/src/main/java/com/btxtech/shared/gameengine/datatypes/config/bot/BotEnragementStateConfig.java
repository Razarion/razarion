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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BotItemConfig> getBotItems() {
        return botItems;
    }

    public void setBotItems(List<BotItemConfig> botItems) {
        this.botItems = botItems;
    }

    public Integer getEnrageUpKills() {
        return enrageUpKills;
    }

    public void setEnrageUpKills(Integer enrageUpKills) {
        this.enrageUpKills = enrageUpKills;
    }

    public BotEnragementStateConfig name(String name) {
        setName(name);
        return this;
    }

    public BotEnragementStateConfig botItems(List<BotItemConfig> botItems) {
        setBotItems(botItems);
        return this;
    }

    public BotEnragementStateConfig enrageUpKills(Integer enrageUpKills) {
        setEnrageUpKills(enrageUpKills);
        return this;
    }

    public BotEnragementStateConfig clone4BotScene(DecimalPosition absoluteCenter) {
        BotEnragementStateConfig botEnragementStateConfig = new BotEnragementStateConfig();
        botEnragementStateConfig.name = name;
        botEnragementStateConfig.botItems = botItems.stream().map(botItemConfig -> botItemConfig.clone4BotScene(absoluteCenter)).collect(Collectors.toList());
        botEnragementStateConfig.enrageUpKills = enrageUpKills;
        return botEnragementStateConfig;
    }
}
