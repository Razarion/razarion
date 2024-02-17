package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * on 20.09.2017.
 */
@Entity
@Table(name = "LEVEL_UNLOCK")
public class LevelUnlockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    private int crystalCost;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public I18nBundleEntity getI18nName() {
        return i18nName;
    }

    public void setI18nName(I18nBundleEntity i18nName) {
        this.i18nName = i18nName;
    }

    public I18nBundleEntity getI18nDescription() {
        return i18nDescription;
    }

    public void setI18nDescription(I18nBundleEntity i18nDescription) {
        this.i18nDescription = i18nDescription;
    }

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
                .id(id)
                .internalName(internalName)
                .baseItemTypeCount(baseItemTypeCount)
                .crystalCost(crystalCost);
        if(i18nName != null) {
            levelUnlockConfig.i18nName(i18nName.toI18nString());
        }
        if(i18nDescription != null) {
            levelUnlockConfig.i18nDescription(i18nDescription.toI18nString());
        }
        if (baseItemType != null) {
            levelUnlockConfig.baseItemType(baseItemType.getId());
        }
        if (thumbnail != null) {
            levelUnlockConfig.thumbnail(thumbnail.getId());
        }
        return levelUnlockConfig;
    }

    public void fromLevelUnlockConfig(LevelUnlockConfig levelUnlockConfig, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, ImagePersistence imagePersistence) {
        setId(levelUnlockConfig.getId());
        setInternalName(levelUnlockConfig.getInternalName());
        setCrystalCost(levelUnlockConfig.getCrystalCost());
        setBaseItemType(baseItemTypeCrudPersistence.getEntity(levelUnlockConfig.getBaseItemType()));
        setBaseItemTypeCount(levelUnlockConfig.getBaseItemTypeCount());
        setThumbnail(imagePersistence.getImageLibraryEntity(levelUnlockConfig.getThumbnail()));
        setI18nName(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nName(), i18nName));
        setI18nDescription(I18nBundleEntity.fromI18nStringSafe(levelUnlockConfig.getI18nDescription(), i18nDescription));
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
