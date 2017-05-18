package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;

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
@Table(name = "SCENE_BOT_KILL_HUMAN_COMMAND")
public class BotKillHumanCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer botAuxiliaryIdId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity attackerBaseItemType;
    private int dominanceFactor;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity spawnPoint;

    public BotKillHumanCommandConfig toBotKillHumanCommandConfig() {
        BotKillHumanCommandConfig botKillHumanCommandConfig = new BotKillHumanCommandConfig().setBotAuxiliaryId(botAuxiliaryIdId).setDominanceFactor(dominanceFactor);
        if (attackerBaseItemType != null) {
            botKillHumanCommandConfig.setAttackerBaseItemTypeId(attackerBaseItemType.getId());
        }
        if (spawnPoint != null) {
            botKillHumanCommandConfig.setSpawnPoint(spawnPoint.toPlaceConfig());
        }
        return botKillHumanCommandConfig;
    }

    public void fromBotKillHumanCommandConfig(BotKillHumanCommandConfig botKillHumanCommandConfig) {
        botAuxiliaryIdId = botKillHumanCommandConfig.getBotAuxiliaryId();
        dominanceFactor = botKillHumanCommandConfig.getDominanceFactor();
        if (botKillHumanCommandConfig.getSpawnPoint() != null) {
            if (spawnPoint == null) {
                spawnPoint = new PlaceConfigEntity();
            }
            spawnPoint.fromPlaceConfig(botKillHumanCommandConfig.getSpawnPoint());
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

        BotKillHumanCommandEntity that = (BotKillHumanCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
