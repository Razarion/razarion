package com.btxtech.server.persistence.quest;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "QUEST_COMPARISON_BOT",
            joinColumns = @JoinColumn(name = "comparisonConfig"),
            inverseJoinColumns = @JoinColumn(name = "botConfig"))
    private List<BotConfigEntity> bots;


    public ComparisonConfig toComparisonConfig() {
        ComparisonConfig comparisonConfig = new ComparisonConfig().setCount(count).setTime(time).setAddExisting(addExisting);
        if (typeCount != null && !typeCount.isEmpty()) {
            comparisonConfig.setTypeCount(typeCount.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> b)));
        }
        if (bots != null && !bots.isEmpty()) {
            comparisonConfig.setBotIds(bots.stream().map(BotConfigEntity::getId).collect(Collectors.toList()));
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
        if (comparisonConfig.getBotIds() != null) {
            if (bots == null) {
                bots = new ArrayList<>();
            }
            bots.clear();
            comparisonConfig.getBotIds().forEach(botId -> bots.add(itemTypePersistence.readBotConfigEntity(botId)));
        } else {
            bots = null;
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
