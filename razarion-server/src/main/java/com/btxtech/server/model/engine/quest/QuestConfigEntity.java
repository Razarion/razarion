package com.btxtech.server.model.engine.quest;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.engine.BotConfigEntityPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import jakarta.persistence.*;


@Entity
@Table(name = "QUEST")
public class QuestConfigEntity extends BaseEntity implements ObjectNameIdProvider {
    private int xp;
    private int razarion;
    private int crystal;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConditionConfigEntity conditionConfigEntity;

    public QuestConfig toQuestConfig() {
        QuestConfig questConfig = new QuestConfig().id(getId()).internalName(getInternalName()).xp(xp).razarion(razarion).crystal(crystal);
        if (conditionConfigEntity != null) {
            questConfig.conditionConfig(conditionConfigEntity.toQuestConfig());
        }
        return questConfig;
    }

    public void fromQuestConfig(BotConfigEntityPersistence botConfigEntityPersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, QuestConfig questConfig) {
        setInternalName(questConfig.getInternalName());
        xp = questConfig.getXp();
        razarion = questConfig.getRazarion();
        crystal = questConfig.getCrystal();
        if (questConfig.getConditionConfig() != null) {
            if (conditionConfigEntity == null) {
                conditionConfigEntity = new ConditionConfigEntity();
            }
            conditionConfigEntity.fromConditionConfig(botConfigEntityPersistence, baseItemTypeCrudPersistence, questConfig.getConditionConfig());
        } else {
            conditionConfigEntity = null;
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
