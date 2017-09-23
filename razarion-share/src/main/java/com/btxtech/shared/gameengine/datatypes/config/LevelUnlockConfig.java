package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.I18nString;

/**
 * Created by Beat
 * on 22.09.2017.
 */
public class LevelUnlockConfig {
    private Integer id;
    private String internalName;
    private Integer thumbnail;
    private I18nString i18nName;
    private I18nString i18nDescription;
    private Integer baseItemType;
    private int baseItemTypeCount;
    private int crystalCost;

    public Integer getId() {
        return id;
    }

    public LevelUnlockConfig setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getThumbnail() {
        return thumbnail;
    }

    public LevelUnlockConfig setThumbnail(Integer thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public LevelUnlockConfig setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
        return this;
    }

    public I18nString getI18nDescription() {
        return i18nDescription;
    }

    public LevelUnlockConfig setI18nDescription(I18nString i18nDescription) {
        this.i18nDescription = i18nDescription;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public LevelUnlockConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getBaseItemType() {
        return baseItemType;
    }

    public LevelUnlockConfig setBaseItemType(Integer baseItemType) {
        this.baseItemType = baseItemType;
        return this;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public LevelUnlockConfig setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        return this;
    }

    public int getCrystalCost() {
        return crystalCost;
    }

    public LevelUnlockConfig setCrystalCost(int crystalCost) {
        this.crystalCost = crystalCost;
        return this;
    }
}
