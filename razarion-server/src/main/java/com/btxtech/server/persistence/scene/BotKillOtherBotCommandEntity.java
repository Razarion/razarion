package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2017.
 */
@Entity
@Table(name = "SCENE_BOT_KILL_OTHER_BOT_COMMAND")
public class BotKillOtherBotCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer botAuxiliaryIdId;
    private Integer targetBotAuxiliaryIdId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity attackerBaseItemType;
    private int dominanceFactor;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity spawnPoint;

    public BotKillOtherBotCommandConfig toBotKillOtherBotCommandConfig() {
        BotKillOtherBotCommandConfig botKillOtherBotCommandConfig = new BotKillOtherBotCommandConfig().setBotAuxiliaryId(botAuxiliaryIdId).setTargetBotAuxiliaryId(targetBotAuxiliaryIdId).setDominanceFactor(dominanceFactor);
        if (attackerBaseItemType != null) {
            botKillOtherBotCommandConfig.setAttackerBaseItemTypeId(attackerBaseItemType.getId());
        }
        if (spawnPoint != null) {
            botKillOtherBotCommandConfig.setSpawnPoint(spawnPoint.toPlaceConfig());
        }
        return botKillOtherBotCommandConfig;
    }

    public void fromBotKillOtherBotCommandConfig(BotKillOtherBotCommandConfig botKillOtherBotCommandConfig) {
        botAuxiliaryIdId = botKillOtherBotCommandConfig.getBotAuxiliaryId();
        targetBotAuxiliaryIdId = botKillOtherBotCommandConfig.getTargetBotAuxiliaryId();
        dominanceFactor = botKillOtherBotCommandConfig.getDominanceFactor();
        if (botKillOtherBotCommandConfig.getSpawnPoint() != null) {
            if (spawnPoint == null) {
                spawnPoint = new PlaceConfigEntity();
            }
            spawnPoint.fromPlaceConfig(botKillOtherBotCommandConfig.getSpawnPoint());
        } else {
            spawnPoint = null;
        }
    }

    public void setAttackerBaseItemType(BaseItemTypeEntity attackerBaseItemType) {
        this.attackerBaseItemType = attackerBaseItemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotKillOtherBotCommandEntity that = (BotKillOtherBotCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
