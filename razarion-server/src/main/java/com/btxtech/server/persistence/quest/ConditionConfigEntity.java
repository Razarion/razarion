package com.btxtech.server.persistence.quest;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BotConfigEntityPersistence;
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
