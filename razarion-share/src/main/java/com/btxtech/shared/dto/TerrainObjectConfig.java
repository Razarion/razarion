package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 10.05.2016.
 */
@JsType
public class TerrainObjectConfig implements Config {
    private int id;
    private String internalName;
    private double radius;
    private Integer model3DId;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL_PACK)
    @Deprecated
    private Integer threeJsModelPackConfigId;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Integer getModel3DId() {
        return model3DId;
    }

    public void setModel3DId(Integer model3DId) {
        this.model3DId = model3DId;
    }

    @Deprecated
    public Integer getThreeJsModelPackConfigId() {
        return threeJsModelPackConfigId;
    }

    @Deprecated
    public void setThreeJsModelPackConfigId(Integer threeJsModelPackConfigId) {
        this.threeJsModelPackConfigId = threeJsModelPackConfigId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectConfig that = (TerrainObjectConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public TerrainObjectConfig id(int id) {
        setId(id);
        return this;
    }

    public TerrainObjectConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public TerrainObjectConfig radius(double radius) {
        setRadius(radius);
        return this;
    }

    @Deprecated
    public TerrainObjectConfig threeJsModelPackConfigId(Integer threeJsModelPackConfigId) {
        setThreeJsModelPackConfigId(threeJsModelPackConfigId);
        return this;
    }

    public TerrainObjectConfig model3DId(Integer model3DId) {
        setModel3DId(model3DId);
        return this;
    }

    @Override
    public String toString() {
        return "TerrainObjectConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                ", radius=" + radius +
                ", threeJsModelPackConfigId='" + threeJsModelPackConfigId + '\'' +
                '}';
    }
}
