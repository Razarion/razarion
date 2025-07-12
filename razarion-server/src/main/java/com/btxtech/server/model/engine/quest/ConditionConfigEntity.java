package com.btxtech.server.model.engine.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    public void fromConditionConfig(ConditionConfig conditionConfig) {
        conditionTrigger = conditionConfig.getConditionTrigger();
        if (comparisonConfig == null) {
            comparisonConfig = new ComparisonConfigEntity();
        }
        comparisonConfig.fromComparisonConfig(conditionConfig.getComparisonConfig());
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
