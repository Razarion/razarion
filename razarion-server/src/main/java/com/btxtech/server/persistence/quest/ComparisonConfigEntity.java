package com.btxtech.server.persistence.quest;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 06.05.2017.
 */
@Entity
@Table(name = "QUEST_COMPARISON")
public class ComparisonConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer count;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "QUEST_COMPARISON_BASE_ITEM")
    private Map<BaseItemTypeEntity, Integer> typeCount;
    private Integer time;
    private Boolean addExisting;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity placeConfig;

    public ComparisonConfig toComparisonConfig() {
        ComparisonConfig comparisonConfig = new ComparisonConfig().setCount(count).setTime(time).setAddExisting(addExisting);
        if (this.typeCount == null || !this.typeCount.isEmpty()) {
            Map<Integer, Integer> typeCount = null;
            typeCount = new HashMap<>();
            for (Map.Entry<BaseItemTypeEntity, Integer> entry : this.typeCount.entrySet()) {
                typeCount.put(entry.getKey().getId(), entry.getValue());
            }
            comparisonConfig.setTypeCount(typeCount);
        }
        if (placeConfig != null) {
            comparisonConfig.setPlaceConfig(placeConfig.toPlaceConfig());
        }
        return comparisonConfig;
    }

    public void fromComparisonConfig(ItemTypePersistence itemTypePersistence, ComparisonConfig comparisonConfig) {
        count = comparisonConfig.getCount();
        time = comparisonConfig.getTime();
        addExisting = comparisonConfig.getAddExisting();
        if (comparisonConfig.getPlaceConfig() != null) {
            if (placeConfig == null) {
                placeConfig = new PlaceConfigEntity();
            }
            placeConfig.fromPlaceConfig(comparisonConfig.getPlaceConfig());
        } else {
            placeConfig = null;
        }
        if (comparisonConfig.getTypeCount() != null) {
            if (typeCount == null) {
                typeCount = new HashMap<>();
            }
            typeCount.clear();
            for (Map.Entry<Integer, Integer> entry : comparisonConfig.getTypeCount().entrySet()) {
                typeCount.put(itemTypePersistence.readBaseItemTypeEntity(entry.getKey()), entry.getValue());
            }
        } else {
            typeCount = null;
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

        ComparisonConfigEntity that = (ComparisonConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
