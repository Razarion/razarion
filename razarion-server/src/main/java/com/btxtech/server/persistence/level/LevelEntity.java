package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btxtech.server.persistence.PersistenceUtil.*;

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
    private List<LevelUnlockEntity> levelUnlockEntities;

    public Integer getId() {
        return id;
    }

    public LevelConfig toLevelConfig() {
        return new LevelConfig()
                .id(id)
                .internalName(Integer.toString(number))
                .number(number)
                .xp2LevelUp(xp2LevelUp)
                .itemTypeLimitation(PersistenceUtil.extractItemTypeLimitation(itemTypeLimitation));
    }

    public LevelEditConfig toLevelEditConfig() {
        return (LevelEditConfig) new LevelEditConfig()
                .levelUnlockConfigs(toConfigList(levelUnlockEntities, LevelUnlockEntity::toLevelUnlockConfig))
                .id(id)
                .internalName(Integer.toString(number))
                .number(number)
                .xp2LevelUp(xp2LevelUp)
                .itemTypeLimitation(PersistenceUtil.extractItemTypeLimitation(itemTypeLimitation));
    }

    public void fromLevelEditConfig(LevelEditConfig levelEditConfig, Map<BaseItemTypeEntity, Integer> itemTypeLimitation, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, ImagePersistence imagePersistence) {
        this.number = levelEditConfig.getNumber();
        this.xp2LevelUp = levelEditConfig.getXp2LevelUp();
        if (this.itemTypeLimitation == null) {
            this.itemTypeLimitation = new HashMap<>();
        }
        this.itemTypeLimitation.clear();
        this.itemTypeLimitation.putAll(itemTypeLimitation);
        levelUnlockEntities = fromConfigsNoClear(levelUnlockEntities,
                levelEditConfig.getLevelUnlockConfigs(),
                LevelUnlockEntity::new,
                (levelUnlockEntity, levelUnlockConfig) -> levelUnlockEntity.fromLevelUnlockConfig(levelUnlockConfig, baseItemTypeCrudPersistence, imagePersistence),
                LevelUnlockConfig::getId,
                LevelUnlockEntity::getId);
    }

    public int getNumber() {
        return number;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    public void setLevelUnlockEntity(List<LevelUnlockEntity> levelUnlockEntities) {
        this.levelUnlockEntities = levelUnlockEntities;
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
