package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "BOT_CONFIG_ENRAGEMENT_STATE_CONFIG")
public class BotEnragementStateConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "botEnragementStateConfig", nullable = false)
    private List<BotItemConfigEntity> botItems;
    private Integer enrageUpKills;

    public BotEnragementStateConfig toBotEnragementStateConfig() {
        List<BotItemConfig> botItems = new ArrayList<>();
        if (this.botItems != null) {
            for (BotItemConfigEntity botItemEntity : this.botItems) {
                botItems.add(botItemEntity.toBotItemConfig());
            }
        }
        return new BotEnragementStateConfig().name(name).enrageUpKills(enrageUpKills).botItems(botItems);
    }

    public void fromBotEnragementStateConfig(BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, BotEnragementStateConfig botEnragementStateConfig) {
        name = botEnragementStateConfig.getName();
        enrageUpKills = botEnragementStateConfig.getEnrageUpKills();
        if (botItems == null) {
            botItems = new ArrayList<>();
        }
        botItems.clear();
        if (botEnragementStateConfig.getBotItems() != null) {
            for (BotItemConfig botItemConfig : botEnragementStateConfig.getBotItems()) {
                BotItemConfigEntity botItemConfigEntity = new BotItemConfigEntity();
                botItemConfigEntity.fromBotItemConfig(baseItemTypeCrudPersistence, botItemConfig);
                botItems.add(botItemConfigEntity);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotEnragementStateConfigEntity that = (BotEnragementStateConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
