package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "BOT_CONFIG")
public class BotConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private boolean npc;
    private int actionDelay;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity realm;
    private String name;
    private Integer minInactiveMs;
    private Integer maxInactiveMs;
    private Integer minActiveMs;
    private Integer maxActiveMs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<BotEnragementStateConfigEntity> botEnragementStateConfigs;

    public Integer getId() {
        return id;
    }

    public BotConfig toBotConfig() {
        PlaceConfig realm = null;
        if (this.realm != null) {
            realm = this.realm.toPlaceConfig();
        }
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        for (BotEnragementStateConfigEntity botEnragementStateConfigEnity : this.botEnragementStateConfigs) {
            botEnragementStateConfigs.add(botEnragementStateConfigEnity.toBotEnragementStateConfig());
        }
        return new BotConfig().setId(id).setNpc(npc).setActionDelay(actionDelay).setRealm(realm).setName(name).setMinInactiveMs(minInactiveMs).setMaxInactiveMs(maxInactiveMs).setMinActiveMs(minActiveMs).setMaxActiveMs(maxActiveMs).setBotEnragementStateConfigs(botEnragementStateConfigs);
    }

    public void fromBotConfig(ItemTypePersistence itemTypePersistence, BotConfig botConfig) {
        npc = botConfig.isNpc();
        actionDelay = botConfig.getActionDelay();
        if (botConfig.getRealm() != null) {
            realm = new PlaceConfigEntity();
            realm.fromPlaceConfig(botConfig.getRealm());
        } else {
            realm = null;
        }
        name = botConfig.getName();
        minInactiveMs = botConfig.getMinInactiveMs();
        maxInactiveMs = botConfig.getMaxInactiveMs();
        minActiveMs = botConfig.getMinActiveMs();
        maxActiveMs = botConfig.getMaxActiveMs();
        if(this.botEnragementStateConfigs == null) {
            this.botEnragementStateConfigs = new ArrayList<>();
        }
        this.botEnragementStateConfigs.clear();
        for (BotEnragementStateConfig botEnragementStateConfig : botConfig.getBotEnragementStateConfigs()) {
            BotEnragementStateConfigEntity botEnragementStateConfigEntity = new BotEnragementStateConfigEntity();
            botEnragementStateConfigEntity.fromBotEnragementStateConfig(itemTypePersistence, botEnragementStateConfig);
            this.botEnragementStateConfigs.add(botEnragementStateConfigEntity);
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

        BotConfigEntity that = (BotConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
