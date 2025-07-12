package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "LEVEL_UNLOCK")
public class LevelUnlockEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
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
                                      BaseItemTypeService baseItemTypeCrudPersistence,
                                      ImageService imageService) {
        setId(levelUnlockConfig.getId());
        setInternalName(levelUnlockConfig.getInternalName());
        setCrystalCost(levelUnlockConfig.getCrystalCost());
        setBaseItemType(baseItemTypeCrudPersistence.getEntity(levelUnlockConfig.getBaseItemType()));
        setBaseItemTypeCount(levelUnlockConfig.getBaseItemTypeCount());
        setThumbnail(imageService.getImageLibraryEntity(levelUnlockConfig.getThumbnail()));
    }

    @JsonGetter("baseItemType")
    public Integer getBaseItemTypeId() {
        return baseItemType != null ? baseItemType.getId() : null;
    }

    @JsonSetter("baseItemType")
    public void setBaseItemTypeId(Integer id) {
        if (id != null) {
            BaseItemTypeEntity entity = new BaseItemTypeEntity();
            entity.setId(id);
            this.baseItemType = entity;
        } else {
            this.baseItemType = null;
        }
    }

    @JsonGetter("thumbnail")
    public Integer getThumbnailId() {
        return thumbnail != null ? thumbnail.getId() : null;
    }

    @JsonSetter("thumbnail")
    public void setThumbnailId(Integer id) {
        if (id != null) {
            ImageLibraryEntity entity = new ImageLibraryEntity();
            entity.setId(id);
            this.thumbnail = entity;
        } else {
            this.thumbnail = null;
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

        LevelUnlockEntity that = (LevelUnlockEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

    public LevelUnlockEntity toJsonLevelUnlockEnty() {
        return this;
    }
}
