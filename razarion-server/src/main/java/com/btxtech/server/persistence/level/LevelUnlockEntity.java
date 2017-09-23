package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
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
        LevelUnlockConfig levelUnlockConfig = new LevelUnlockConfig().setId(id).setInternalName(internalName).setBaseItemTypeCount(baseItemTypeCount).setCrystalCost(crystalCost);
        if(i18nName != null) {
            levelUnlockConfig.setI18nName(i18nName.toI18nString());
        }
        if(i18nDescription != null) {
            levelUnlockConfig.setI18nDescription(i18nDescription.toI18nString());
        }
        if (baseItemType != null) {
            levelUnlockConfig.setBaseItemType(baseItemType.getId());
        }
        if (thumbnail != null) {
            levelUnlockConfig.setThumbnail(thumbnail.getId());
        }
        return levelUnlockConfig;
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
