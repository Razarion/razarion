package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import com.btxtech.server.persistence.BabylonMaterialEntity;
import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "BOT_CONFIG")
public class BotConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private Integer auxiliaryId;
    private boolean npc;
    private int actionDelay;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity realm;
    private String name;
    private boolean autoAttack;
    private Integer minInactiveMs;
    private Integer maxInactiveMs;
    private Integer minActiveMs;
    private Integer maxActiveMs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "botConfig", nullable = false)
    private List<BotEnragementStateConfigEntity> botEnragementStateConfigs;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity groundBabylonMaterialEntity;

    public Integer getId() {
        return id;
    }

    public BotConfig toBotConfig() {
        PlaceConfig realm = null;
        if (this.realm != null) {
            realm = this.realm.toPlaceConfig();
        }
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        if (this.botEnragementStateConfigs != null) {
            for (BotEnragementStateConfigEntity botEnragementStateConfigEnity : this.botEnragementStateConfigs) {
                botEnragementStateConfigs.add(botEnragementStateConfigEnity.toBotEnragementStateConfig());
            }
        }
        return new BotConfig()
                .auxiliaryId(auxiliaryId)
                .id(id)
                .internalName(internalName)
                .autoAttack(autoAttack)
                .npc(npc)
                .actionDelay(actionDelay)
                .realm(realm)
                .name(name)
                .minInactiveMs(minInactiveMs)
                .maxInactiveMs(maxInactiveMs)
                .minActiveMs(minActiveMs)
                .maxActiveMs(maxActiveMs)
                .botEnragementStateConfigs(botEnragementStateConfigs)
                .groundBabylonMaterialId(extractId(groundBabylonMaterialEntity, BabylonMaterialEntity::getId));
    }

    public void fromBotConfig(BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                              BabylonMaterialCrudPersistence babylonMaterialCrudPersistence,
                              BotConfig botConfig) {
        internalName = botConfig.getInternalName();
        auxiliaryId = botConfig.getAuxiliaryId();
        npc = botConfig.isNpc();
        actionDelay = botConfig.getActionDelay();
        if (botConfig.getRealm() != null) {
            realm = new PlaceConfigEntity();
            realm.fromPlaceConfig(botConfig.getRealm());
        } else {
            realm = null;
        }
        name = botConfig.getName();
        autoAttack = botConfig.isAutoAttack();
        minInactiveMs = botConfig.getMinInactiveMs();
        maxInactiveMs = botConfig.getMaxInactiveMs();
        minActiveMs = botConfig.getMinActiveMs();
        maxActiveMs = botConfig.getMaxActiveMs();
        if (this.botEnragementStateConfigs == null) {
            this.botEnragementStateConfigs = new ArrayList<>();
        }
        this.botEnragementStateConfigs.clear();
        if (botConfig.getBotEnragementStateConfigs() != null) {
            for (BotEnragementStateConfig botEnragementStateConfig : botConfig.getBotEnragementStateConfigs()) {
                BotEnragementStateConfigEntity botEnragementStateConfigEntity = new BotEnragementStateConfigEntity();
                botEnragementStateConfigEntity.fromBotEnragementStateConfig(baseItemTypeCrudPersistence, botEnragementStateConfig);
                this.botEnragementStateConfigs.add(botEnragementStateConfigEntity);
            }
        }
        groundBabylonMaterialEntity = babylonMaterialCrudPersistence.getEntity(botConfig.getGroundBabylonMaterialId());
    }

    public BotConfigEntity setAutoAttack(boolean autoAttack) {
        this.autoAttack = autoAttack;
        return this;
    }

    public String getInternalName() {
        return internalName;
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
