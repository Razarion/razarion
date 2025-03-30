package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOT_CONFIG_ENRAGEMENT_STATE_CONFIG")
public class BotEnragementStateConfigEntity extends BaseEntity {
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
        return new BotEnragementStateConfig().name(getInternalName()).enrageUpKills(enrageUpKills).botItems(botItems);
    }

    public void fromBotEnragementStateConfig(BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, BotEnragementStateConfig botEnragementStateConfig) {
        setInternalName(botEnragementStateConfig.getName());
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
