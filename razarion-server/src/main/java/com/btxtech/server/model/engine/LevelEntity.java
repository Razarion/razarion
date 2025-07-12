package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.btxtech.server.service.PersistenceUtil.*;


@Entity
@Table(name = "LEVEL")
public class LevelEntity extends BaseEntity {
    private int number;
    private int xp2LevelUp;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "LEVEL_LIMITATION")
    @JsonIgnore
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "level")
    @JsonIgnore
    private List<LevelUnlockEntity> levelUnlockEntities;

    public LevelConfig toLevelConfig() {
        return new LevelConfig()
                .id(getId())
                .internalName(Integer.toString(number))
                .number(number)
                .xp2LevelUp(xp2LevelUp)
                .itemTypeLimitation(extractItemTypeLimitation(itemTypeLimitation));
    }

    public LevelConfig toLevelEditConfig() {
        return new LevelConfig()
                .levelUnlockConfigs(toConfigList(levelUnlockEntities, LevelUnlockEntity::toLevelUnlockConfig))
                .id(getId())
                .internalName(Integer.toString(number))
                .number(number)
                .xp2LevelUp(xp2LevelUp)
                .itemTypeLimitation(extractItemTypeLimitation(itemTypeLimitation));
    }

    public void fromLevelEditConfig(LevelConfig levelConfig,
                                    Map<BaseItemTypeEntity, Integer> itemTypeLimitation,
                                    BaseItemTypeService baseItemTypeCrudPersistence,
                                    ImageService imageService) {
        this.number = levelConfig.getNumber();
        this.xp2LevelUp = levelConfig.getXp2LevelUp();
        if (this.itemTypeLimitation == null) {
            this.itemTypeLimitation = new HashMap<>();
        }
        this.itemTypeLimitation.clear();
        this.itemTypeLimitation.putAll(itemTypeLimitation);
        levelUnlockEntities = fromConfigsNoClear(levelUnlockEntities,
                levelConfig.getLevelUnlockConfigs(),
                LevelUnlockEntity::new,
                (levelUnlockEntity, levelUnlockConfig) -> levelUnlockEntity.fromLevelUnlockConfig(levelUnlockConfig, baseItemTypeCrudPersistence, imageService),
                LevelUnlockConfig::getId,
                LevelUnlockEntity::getId);
    }

    public int getNumber() {
        return number;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    @JsonGetter("levelUnlockEntities")
    public List<LevelUnlockEntity> getJsonLevelUnlockEntities() {
        return levelUnlockEntities.stream()
                .map(LevelUnlockEntity::toJsonLevelUnlockEnty)
                .collect(Collectors.toList());
    }

    @JsonSetter("levelUnlockEntities")
    public void setJsonLevelUnlockEntities(List<LevelUnlockEntity> jsonLevelUnlockEntities) {
        if (jsonLevelUnlockEntities == null) {
            this.levelUnlockEntities = null;
            return;
        }
        this.levelUnlockEntities = jsonLevelUnlockEntities;
    }

    @JsonGetter("itemTypeLimitation")
    public Map<Integer, Integer> getJsonItemTypeLimitation() {
        if (itemTypeLimitation == null) {
            return null;
        }
        return itemTypeLimitation.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().getId() != null)
                .collect(Collectors.toMap(
                        e -> e.getKey().getId(),
                        Map.Entry::getValue
                ));
    }

    @JsonSetter("itemTypeLimitation")
    public void setJsonItemTypeLimitation(Map<Integer, Integer> jsonItemTypeLimitation) {
        if (jsonItemTypeLimitation == null) {
            return;
        }
        if (itemTypeLimitation == null) {
            itemTypeLimitation = new HashMap<>();
        } else {
            itemTypeLimitation.clear();
        }
        for (Map.Entry<Integer, Integer> entry : jsonItemTypeLimitation.entrySet()) {
            BaseItemTypeEntity entity = new BaseItemTypeEntity();
            entity.setId(entry.getKey());
            itemTypeLimitation.put(entity, entry.getValue());
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

        LevelEntity that = (LevelEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
