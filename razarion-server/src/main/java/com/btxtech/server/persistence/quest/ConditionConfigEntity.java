package com.btxtech.server.persistence.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 06.05.2017.
 */
@Entity
@Table(name = "QUEST_CONDITION")
public class ConditionConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private ConditionTrigger conditionTrigger;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ComparisonConfigEntity comparisonConfig;

    public ConditionConfig toQuestConfig() {
        return new ConditionConfig().setConditionTrigger(conditionTrigger).setComparisonConfig(comparisonConfig.toComparisonConfig());
    }

    public void fromConditionConfig(ConditionConfig conditionConfig) {
        conditionTrigger = conditionConfig.getConditionTrigger();
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
