package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.ui.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btxtech.server.service.PersistenceUtil.*;


@Entity
@Table(name = "LEVEL")
public class LevelEntity extends BaseEntity {
    private int number;
    private int xp2LevelUp;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "LEVEL_LIMITATION")
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "level")
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
                                    BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                    ImagePersistence imagePersistence) {
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
