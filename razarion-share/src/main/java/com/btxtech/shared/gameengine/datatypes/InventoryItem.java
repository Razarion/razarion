package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 25.10.2016.
 */
@JsType
public class InventoryItem implements Config {
    private int id;
    private I18nString i18nName;
    private String internalName;
    private Integer razarion;
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer baseItemTypeId;
    private int baseItemTypeCount;
    private double baseItemTypeFreeRange;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer imageId;
    private Integer crystalCost;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InventoryItem id(int id) {
        setId(id);
        return this;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public void setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
    }

    public InventoryItem i18nName(I18nString i18nName) {
        setI18nName(i18nName);
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public InventoryItem internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Integer getRazarion() {
        return razarion;
    }

    public InventoryItem razarion(@Nullable Integer razarion) {
        setRazarion(razarion);
        return this;
    }

    public @Nullable Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(@Nullable Integer baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public InventoryItem baseItemTypeId(Integer baseItemTypeId) {
        setBaseItemTypeId(baseItemTypeId);
        return this;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public InventoryItem baseItemTypeCount(int baseItemTypeCount) {
        setBaseItemTypeCount(baseItemTypeCount);
        return this;
    }

    public void setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
    }

    public void setBaseItemTypeFreeRange(double baseItemTypeFreeRange) {
        this.baseItemTypeFreeRange = baseItemTypeFreeRange;
    }

    public double getBaseItemTypeFreeRange() {
        return baseItemTypeFreeRange;
    }

    public InventoryItem baseItemTypeFreeRange(double baseItemTypeFreeRange) {
        setBaseItemTypeFreeRange(baseItemTypeFreeRange);
        return this;
    }

    public @Nullable Integer getImageId() {
        return imageId;
    }

    public void setImageId(@Nullable Integer imageId) {
        this.imageId = imageId;
    }

    public InventoryItem imageId(Integer imageId) {
        setImageId(imageId);
        return this;
    }

    public void setRazarion(Integer razarion) {
        this.razarion = razarion;
    }

    public boolean hasBaseItemTypeId() {
        return baseItemTypeId != null;
    }

    public Integer getCrystalCost() {
        return crystalCost;
    }

    public void setCrystalCost(@Nullable Integer crystalCost) {
        this.crystalCost = crystalCost;
    }

    public InventoryItem crystalCost(@Nullable Integer crystalCost) {
        setCrystalCost(crystalCost);
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) { // equals needed because Errai binder proxy. May be wrong -> BindableProxyFactory.getBindableProxy()
            return false;
        }

        InventoryItem that = (InventoryItem) o;

        return getId() == that.getId(); // itemType.getId() needed because Errai binder proxy. May be wrong -> BindableProxyFactory.getBindableProxy()
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", name='" + internalName + '\'' +
                ", razarion=" + razarion +
                ", baseItemTypeId=" + baseItemTypeId +
                ", baseItemTypeCount=" + baseItemTypeCount +
                ", baseItemTypeFreeRange=" + baseItemTypeFreeRange +
                ", imageId=" + imageId +
                '}';
    }
}
