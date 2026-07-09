package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

/**
 * A collectible artifact. Artifacts are dropped by boxes and can be assembled
 * into an {@link InventoryItem} in the workshop. Ported from the legacy
 * controltheland project (DbInventoryArtifact).
 */
@JsType
@JsonPersistable
public class InventoryArtifact implements Config {
    private int id;
    private I18nString i18nName;
    private String internalName;
    private Rareness rareness;
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

    public InventoryArtifact id(int id) {
        setId(id);
        return this;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public void setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
    }

    public InventoryArtifact i18nName(I18nString i18nName) {
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

    public InventoryArtifact internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Rareness getRareness() {
        return rareness;
    }

    public void setRareness(Rareness rareness) {
        this.rareness = rareness;
    }

    public InventoryArtifact rareness(Rareness rareness) {
        setRareness(rareness);
        return this;
    }

    public @Nullable Integer getImageId() {
        return imageId;
    }

    public void setImageId(@Nullable Integer imageId) {
        this.imageId = imageId;
    }

    public InventoryArtifact imageId(Integer imageId) {
        setImageId(imageId);
        return this;
    }

    public Integer getCrystalCost() {
        return crystalCost;
    }

    public void setCrystalCost(@Nullable Integer crystalCost) {
        this.crystalCost = crystalCost;
    }

    public InventoryArtifact crystalCost(@Nullable Integer crystalCost) {
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
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        InventoryArtifact that = (InventoryArtifact) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "InventoryArtifact{" +
                "id=" + id +
                ", name='" + internalName + '\'' +
                ", rareness=" + rareness +
                ", imageId=" + imageId +
                ", crystalCost=" + crystalCost +
                '}';
    }
}
