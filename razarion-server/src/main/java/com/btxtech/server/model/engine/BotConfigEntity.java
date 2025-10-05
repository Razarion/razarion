package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;

@Entity
@Table(name = "BOT_CONFIG")
public class BotConfigEntity extends BaseEntity {
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity groundBoxModel3DEntity;
    private Double groundBoxHeight;
    @ElementCollection
    @CollectionTable(name = "BOT_CONFIG_GROUND_BOX_POSITIONS", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> groundBoxPositions;

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
        List<DecimalPosition> lazyInitPositions = new ArrayList<>();
        if (groundBoxPositions != null) {
            lazyInitPositions.addAll(groundBoxPositions);
        }
        return new BotConfig()
                .auxiliaryId(auxiliaryId)
                .id(getId())
                .internalName(getInternalName())
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
                .groundBabylonMaterialId(extractId(groundBabylonMaterialEntity, BabylonMaterialEntity::getId))
                .groundBoxModel3DEntityId(extractId(groundBoxModel3DEntity, Model3DEntity::getId))
                .groundBoxHeight(groundBoxHeight)
                .groundBoxPositions(lazyInitPositions);
    }

    public BotConfigEntity fromBotConfig(BotConfig botConfig) {
        setInternalName(botConfig.getInternalName());
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
                botEnragementStateConfigEntity.fromBotEnragementStateConfig(botEnragementStateConfig);
                this.botEnragementStateConfigs.add(botEnragementStateConfigEntity);
            }
        }
        groundBabylonMaterialEntity = botConfig.getGroundBabylonMaterialId() != null ? (BabylonMaterialEntity) new BabylonMaterialEntity().id(botConfig.getGroundBabylonMaterialId()) : null;
        groundBoxModel3DEntity = botConfig.getGroundBoxModel3DEntityId() != null ? (Model3DEntity) new Model3DEntity().id(botConfig.getGroundBoxModel3DEntityId()) : null;
        groundBoxHeight = botConfig.getGroundBoxHeight();
        if (groundBoxPositions == null) {
            groundBoxPositions = new ArrayList<>();
        }
        groundBoxPositions.clear();
        groundBoxPositions.addAll(botConfig.getGroundBoxPositions());
        return this;
    }

    public BotConfigEntity setAutoAttack(boolean autoAttack) {
        this.autoAttack = autoAttack;
        return this;
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
