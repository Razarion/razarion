package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import org.teavm.flavour.json.JsonPersistable;

/**
 * Created by Beat
 * on 22.09.2017.
 */
@JsonPersistable
public class LevelUnlockConfig {
    private Integer id;
    private String internalName;
    private Integer thumbnail;
    private I18nString i18nName;
    private I18nString i18nDescription;
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer baseItemType;
    private int baseItemTypeCount;
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

    public @Nullable Integer getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(@Nullable Integer thumbnail) {
        this.thumbnail = thumbnail;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public void setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
    }

    public I18nString getI18nDescription() {
        return i18nDescription;
    }

    public void setI18nDescription(I18nString i18nDescription) {
        this.i18nDescription = i18nDescription;
    }

    public @Nullable Integer getBaseItemType() {
        return baseItemType;
    }

    public void setBaseItemType(@Nullable Integer baseItemType) {
        this.baseItemType = baseItemType;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public void setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
    }

    public int getCrystalCost() {
        return crystalCost;
    }

    public void setCrystalCost(int crystalCost) {
        this.crystalCost = crystalCost;
    }


    public LevelUnlockConfig id(Integer id) {
        setId(id);
        return this;
    }

    public LevelUnlockConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public LevelUnlockConfig thumbnail(Integer thumbnail) {
        setThumbnail(thumbnail);
        return this;
    }

    public LevelUnlockConfig i18nName(I18nString i18nName) {
        setI18nName(i18nName);
        return this;
    }

    public LevelUnlockConfig i18nDescription(I18nString i18nDescription) {
        setI18nDescription(i18nDescription);
        return this;
    }

    public LevelUnlockConfig baseItemType(Integer baseItemType) {
        setBaseItemType(baseItemType);
        return this;
    }

    public LevelUnlockConfig baseItemTypeCount(int baseItemTypeCount) {
        setBaseItemTypeCount(baseItemTypeCount);
        return this;
    }

    public LevelUnlockConfig crystalCost(int crystalCost) {
        setCrystalCost(crystalCost);
        return this;
    }

    @Override
    public String toString() {
        return "LevelUnlockConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                ", thumbnail=" + thumbnail +
                ", i18nName=" + i18nName +
                ", i18nDescription=" + i18nDescription +
                ", baseItemType=" + baseItemType +
                ", baseItemTypeCount=" + baseItemTypeCount +
                ", crystalCost=" + crystalCost +
                '}';
    }
}
