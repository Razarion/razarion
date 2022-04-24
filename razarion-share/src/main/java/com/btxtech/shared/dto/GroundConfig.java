package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

import java.util.Objects;

/**
 * Created by Beat
 * 07.05.2016.
 */
@JsType
public class GroundConfig implements Config {
    private int id;
    private String internalName;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer topThreeJsMaterial;
    private PhongMaterialConfig topMaterial;
    private PhongMaterialConfig bottomMaterial;
    private GroundSplattingConfig splatting;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getTopThreeJsMaterial() {
        return topThreeJsMaterial;
    }

    public void setTopThreeJsMaterial(Integer topThreeJsMaterial) {
        this.topThreeJsMaterial = topThreeJsMaterial;
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

    public GroundSplattingConfig getSplatting() {
        return splatting;
    }

    public void setSplatting(GroundSplattingConfig splatting) {
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

    public GroundConfig topThreeJsMaterial(Integer topThreeJsMaterial) {
        setTopThreeJsMaterial(topThreeJsMaterial);
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

    public GroundConfig splatting(GroundSplattingConfig splatting) {
        setSplatting(splatting);
        return this;
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
