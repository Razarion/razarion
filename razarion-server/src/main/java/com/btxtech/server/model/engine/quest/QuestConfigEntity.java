package com.btxtech.server.model.engine.quest;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.TipConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@Entity
@Table(name = "QUEST")
public class QuestConfigEntity extends BaseEntity implements ObjectNameIdProvider {
    private int xp;
    private int razarion;
    private int crystal;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConditionConfigEntity conditionConfigEntity;
    private String tipString;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity tipActorItemType;

    public QuestConfig toQuestConfig() {
        QuestConfig questConfig = new QuestConfig().id(getId()).internalName(getInternalName()).xp(xp).razarion(razarion).crystal(crystal);
        if (conditionConfigEntity != null) {
            questConfig.conditionConfig(conditionConfigEntity.toQuestConfig());
        }
        if (tipString != null) {
            questConfig.setTipConfig(new TipConfig()
                    .tipString(tipString)
                    .actorItemTypeId(extractId(tipActorItemType, BaseEntity::getId)));
        }
        return questConfig;
    }

    public void fromQuestConfig(QuestConfig questConfig) {
        setInternalName(questConfig.getInternalName());
        xp = questConfig.getXp();
        razarion = questConfig.getRazarion();
        crystal = questConfig.getCrystal();
        if (questConfig.getConditionConfig() != null) {
            if (conditionConfigEntity == null) {
                conditionConfigEntity = new ConditionConfigEntity();
            }
            conditionConfigEntity.fromConditionConfig(questConfig.getConditionConfig());
        } else {
            conditionConfigEntity = null;
        }
        TipConfig tipConfig = questConfig.getTipConfig();
        if (tipConfig != null) {
            tipString = tipConfig.getTipString();
            if (tipConfig.getActorItemTypeId() != null) {
                tipActorItemType = (BaseItemTypeEntity) new BaseItemTypeEntity().id(tipConfig.getActorItemTypeId());
            } else {
                tipActorItemType = null;
            }
        } else {
            tipString = null;
            tipActorItemType = null;
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

        QuestConfigEntity that = (QuestConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(getId(), getInternalName());
    }
}
