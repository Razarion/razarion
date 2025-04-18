package com.btxtech.server.model.engine.quest;

import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.engine.BotConfigEntityPersistence;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import jakarta.persistence.*;

@Entity
@Table(name = "QUEST_CONDITION")
public class ConditionConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private ConditionTrigger conditionTrigger;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ComparisonConfigEntity comparisonConfig;

    public ConditionConfig toQuestConfig() {
        return new ConditionConfig().conditionTrigger(conditionTrigger).comparisonConfig(comparisonConfig.toComparisonConfig());
    }

    public void fromConditionConfig(BotConfigEntityPersistence botConfigEntityPersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, ConditionConfig conditionConfig) {
        conditionTrigger = conditionConfig.getConditionTrigger();
        if(comparisonConfig == null) {
            comparisonConfig = new ComparisonConfigEntity();
        }
        comparisonConfig.fromComparisonConfig(botConfigEntityPersistence, baseItemTypeCrudPersistence, conditionConfig.getComparisonConfig());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConditionConfigEntity that = (ConditionConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
