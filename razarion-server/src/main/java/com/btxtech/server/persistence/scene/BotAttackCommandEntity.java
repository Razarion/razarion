package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.dto.BotAttackCommandConfig;

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
@Table(name = "SCENE_BOT_ATTACK_COMMAND")
public class BotAttackCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer botAuxiliaryIdId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity targetItemType;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity targetSelection;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity actorItemType;

    public BotAttackCommandConfig toBotAttackCommandConfig() {
        BotAttackCommandConfig attackCommandConfig = new BotAttackCommandConfig().setBotAuxiliaryId(botAuxiliaryIdId);
        if (targetItemType != null) {
            attackCommandConfig.setTargetItemTypeId(targetItemType.getId());
        }
        if (targetSelection != null) {
            attackCommandConfig.setTargetSelection(targetSelection.toPlaceConfig());
        }
        if (actorItemType != null) {
            attackCommandConfig.setActorItemTypeId(actorItemType.getId());
        }
        return attackCommandConfig;
    }

    public void setBotAuxiliaryIdId(Integer botAuxiliaryIdId) {
        this.botAuxiliaryIdId = botAuxiliaryIdId;
    }

    public void setTargetItemType(BaseItemTypeEntity targetItemType) {
        this.targetItemType = targetItemType;
    }

    public void setTargetSelection(PlaceConfigEntity targetSelection) {
        this.targetSelection = targetSelection;
    }

    public void setActorItemType(BaseItemTypeEntity actorItemType) {
        this.actorItemType = actorItemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotAttackCommandEntity that = (BotAttackCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
