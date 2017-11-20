package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Entity
@Table(name = "LEVEL")
public class LevelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int number;
    private int xp2LevelUp;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "LEVEL_LIMITATION")
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "level")
    private Collection<LevelUnlockEntity> levelUnlockEntities;

    public Integer getId() {
        return id;
    }

    public LevelConfig toLevelConfig() {
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        if (this.itemTypeLimitation != null) {
            for (Map.Entry<BaseItemTypeEntity, Integer> entry : this.itemTypeLimitation.entrySet()) {
                itemTypeLimitation.put(entry.getKey().getId(), entry.getValue());
            }
        }
        return new LevelConfig().setLevelId(id).setNumber(number).setXp2LevelUp(xp2LevelUp).setItemTypeLimitation(itemTypeLimitation);
    }

    public LevelEditConfig toLevelEditConfig() {
        LevelEditConfig levelEditConfig = new LevelEditConfig();
        levelEditConfig.setLevelId(id).setNumber(number).setXp2LevelUp(xp2LevelUp);
        if (this.itemTypeLimitation != null) {
            levelEditConfig.setItemTypeLimitation(this.itemTypeLimitation.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> b)));
        }
        if (levelUnlockEntities != null) {
            levelEditConfig.setLevelUnlockConfigs(levelUnlockEntities.stream().map(LevelUnlockEntity::toLevelUnlockConfig).collect(Collectors.toList()));
        }
        return levelEditConfig;
    }

    public void fromLevelEditConfig(LevelEditConfig levelEditConfig, Map<BaseItemTypeEntity, Integer> itemTypeLimitation, Collection<LevelUnlockEntity> levelUnlockEntities) {
        this.number = levelEditConfig.getNumber();
        this.xp2LevelUp = levelEditConfig.getXp2LevelUp();
        if (this.itemTypeLimitation == null) {
            this.itemTypeLimitation = new HashMap<>();
        }
        this.itemTypeLimitation.clear();
        this.itemTypeLimitation.putAll(itemTypeLimitation);
        if (this.levelUnlockEntities == null) {
            this.levelUnlockEntities = new ArrayList<>();
        }
        this.levelUnlockEntities.clear();
        if (levelUnlockEntities != null) {
            this.levelUnlockEntities.addAll(levelUnlockEntities);
        }
    }

    public int getNumber() {
        return number;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    public LevelUnlockEntity getLevelUnlockEntity(int levelUnlockEntityId) {
        if (levelUnlockEntities == null) {
            throw new IllegalArgumentException("No LevelUnlockEntity for levelUnlockEntityId: " + levelUnlockEntityId + " in level with id: " + id);
        }
        return levelUnlockEntities.stream().filter(levelUnlockEntity -> levelUnlockEntityId == levelUnlockEntity.getId()).findFirst().orElseThrow(() -> new IllegalArgumentException("No LevelUnlockEntity for levelUnlockEntityId: " + levelUnlockEntityId + " in level with id: " + id));
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
