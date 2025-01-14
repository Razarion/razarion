package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @Deprecated
    private Integer topThreeJsMaterial;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    @JsonIgnore
    @Deprecated
    private Integer bottomThreeJsMaterial;
    @JsonIgnore
    @Deprecated
    private String color;
    private Integer groundBabylonMaterialId;
    private Integer waterBabylonMaterialId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable Integer getGroundBabylonMaterialId() {
        return groundBabylonMaterialId;
    }

    public void setGroundBabylonMaterialId(@Nullable Integer groundBabylonMaterialId) {
        this.groundBabylonMaterialId = groundBabylonMaterialId;
    }

    public @Nullable Integer getWaterBabylonMaterialId() {
        return waterBabylonMaterialId;
    }

    public void setWaterBabylonMaterialId(@Nullable Integer waterBabylonMaterialId) {
        this.waterBabylonMaterialId = waterBabylonMaterialId;
    }

    public Integer getTopThreeJsMaterial() {
        return topThreeJsMaterial;
    }

    public void setTopThreeJsMaterial(Integer topThreeJsMaterial) {
        this.topThreeJsMaterial = topThreeJsMaterial;
    }

    @JsonIgnore
    public Integer getBottomThreeJsMaterial() {
        return bottomThreeJsMaterial;
    }

    @JsonIgnore
    public void setBottomThreeJsMaterial(Integer bottomThreeJsMaterial) {
        this.bottomThreeJsMaterial = bottomThreeJsMaterial;
    }

    @JsonIgnore
    public String getColor() {
        return color;
    }

    @JsonIgnore
    public void setColor(String color) {
        this.color = color;
    }

    public GroundConfig id(int id) {
        setId(id);
        return this;
    }

    public GroundConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public GroundConfig groundBabylonMaterialId(Integer groundBabylonMaterialId) {
        setGroundBabylonMaterialId(groundBabylonMaterialId);
        return this;
    }

    public GroundConfig waterBabylonMaterialId(Integer waterBabylonMaterialId) {
        setWaterBabylonMaterialId(waterBabylonMaterialId);
        return this;
    }

    public GroundConfig topThreeJsMaterial(Integer topThreeJsMaterial) {
        setTopThreeJsMaterial(topThreeJsMaterial);
        return this;
    }

    public GroundConfig bottomThreeJsMaterial(Integer bottomThreeJsMaterial) {
        setBottomThreeJsMaterial(bottomThreeJsMaterial);
        return this;
    }

    public GroundConfig color(String color) {
        setColor(color);
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
