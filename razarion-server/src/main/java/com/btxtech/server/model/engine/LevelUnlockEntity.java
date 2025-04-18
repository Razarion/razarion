package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.ui.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import jakarta.persistence.*;

@Entity
@Table(name = "LEVEL_UNLOCK")
public class LevelUnlockEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    private int crystalCost;

    public int getCrystalCost() {
        return crystalCost;
    }

    public void setCrystalCost(int crystalCost) {
        this.crystalCost = crystalCost;
    }

    public BaseItemTypeEntity getBaseItemType() {
        return baseItemType;
    }

    public void setBaseItemType(BaseItemTypeEntity baseItemType) {
        this.baseItemType = baseItemType;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public void setBaseItemTypeCount(int count) {
        this.baseItemTypeCount = count;
    }

    public void setThumbnail(ImageLibraryEntity thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LevelUnlockConfig toLevelUnlockConfig() {
        LevelUnlockConfig levelUnlockConfig = new LevelUnlockConfig()
                .id(getId())
                .internalName(getInternalName())
                .baseItemTypeCount(baseItemTypeCount)
                .crystalCost(crystalCost);
        if (baseItemType != null) {
            levelUnlockConfig.baseItemType(baseItemType.getId());
        }
        if (thumbnail != null) {
            levelUnlockConfig.thumbnail(thumbnail.getId());
        }
        return levelUnlockConfig;
    }

    public void fromLevelUnlockConfig(LevelUnlockConfig levelUnlockConfig,
                                      BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                      ImagePersistence imagePersistence) {
        setId(levelUnlockConfig.getId());
        setInternalName(levelUnlockConfig.getInternalName());
        setCrystalCost(levelUnlockConfig.getCrystalCost());
        setBaseItemType(baseItemTypeCrudPersistence.getEntity(levelUnlockConfig.getBaseItemType()));
        setBaseItemTypeCount(levelUnlockConfig.getBaseItemTypeCount());
        setThumbnail(imagePersistence.getImageLibraryEntity(levelUnlockConfig.getThumbnail()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelUnlockEntity that = (LevelUnlockEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
