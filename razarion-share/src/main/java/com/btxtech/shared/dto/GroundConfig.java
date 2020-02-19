package com.btxtech.shared.dto;

import java.util.Objects;

/**
 * Created by Beat
 * 07.05.2016.
 */
public class GroundConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private PhongMaterialConfig topMaterial;
    private PhongMaterialConfig bottomMaterial;
    private DoubleSplattingConfig splatting;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public PhongMaterialConfig getTopMaterial() {
        return topMaterial;
    }

    public void setTopMaterial(PhongMaterialConfig topMaterial) {
        this.topMaterial = topMaterial;
    }

    public PhongMaterialConfig getBottomMaterial() {
        return bottomMaterial;
    }

    public void setBottomMaterial(PhongMaterialConfig bottomMaterial) {
        this.bottomMaterial = bottomMaterial;
    }

    public DoubleSplattingConfig getSplatting() {
        return splatting;
    }

    public void setSplatting(DoubleSplattingConfig splatting) {
        this.splatting = splatting;
    }

    public GroundConfig id(int id) {
        this.id = id;
        return this;
    }

    public GroundConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public GroundConfig topMaterial(PhongMaterialConfig topMaterial) {
        setTopMaterial(topMaterial);
        return this;
    }

    public GroundConfig bottomMaterial(PhongMaterialConfig bottomMaterial) {
        setBottomMaterial(bottomMaterial);
        return this;
    }

    public GroundConfig splatting(DoubleSplattingConfig splatting) {
        setSplatting(splatting);
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroundConfig that = (GroundConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
