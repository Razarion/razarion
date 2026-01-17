package com.btxtech.server.model.engine.quest;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.model.engine.BotConfigEntity;
import com.btxtech.server.model.engine.PlaceConfigEntity;
import com.btxtech.server.model.engine.StartRegionConfigEntity;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.service.PersistenceUtil.extractId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private boolean includeExisting;
    private Integer time; // In seconds
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity placeConfig;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "QUEST_COMPARISON_BOT",
            joinColumns = @JoinColumn(name = "comparisonConfig"),
            inverseJoinColumns = @JoinColumn(name = "botConfig"))
    private List<BotConfigEntity> bots;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startRegionId")
    private StartRegionConfigEntity startRegion;

    public ComparisonConfig toComparisonConfig() {
        ComparisonConfig comparisonConfig = new ComparisonConfig()
                .count(count)
                .includeExisting(includeExisting)
                .timeSeconds(time)
                .startRegionId(extractId(startRegion, BaseEntity::getId));
        if (typeCount != null && !typeCount.isEmpty()) {
            comparisonConfig.typeCount(typeCount.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> b)));
        }
        if (bots != null && !bots.isEmpty()) {
            comparisonConfig.botIds(bots.stream().map(BotConfigEntity::getId).collect(Collectors.toList()));
        }
        if (placeConfig != null) {
            comparisonConfig.placeConfig(placeConfig.toPlaceConfig());
        }
        return comparisonConfig;
    }

    public void fromComparisonConfig(ComparisonConfig comparisonConfig) {
        includeExisting = comparisonConfig.isIncludeExisting();
        count = comparisonConfig.getCount();
        time = comparisonConfig.getTimeSeconds();
        if (comparisonConfig.getStartRegionId() != null) {
            startRegion = (StartRegionConfigEntity) new StartRegionConfigEntity().id(comparisonConfig.getStartRegionId());
        } else {
            startRegion = null;
        }
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
                typeCount.put((BaseItemTypeEntity) new BaseItemTypeEntity().id(entry.getKey()), entry.getValue());
            }
        } else {
            typeCount = null;
        }
        if (comparisonConfig.getBotIds() != null) {
            if (bots == null) {
                bots = new ArrayList<>();
            }
            bots.clear();
            comparisonConfig.getBotIds().forEach(botId -> bots.add((BotConfigEntity) new BotConfigEntity().id(botId)));
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
