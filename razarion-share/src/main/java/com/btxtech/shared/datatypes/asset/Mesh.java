package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

import java.util.Objects;

public class Mesh {
    @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer shape3DId;
    private String element3DId;
    private ShapeTransform shapeTransform;

    public Integer getShape3DId() {
        return shape3DId;
    }

    public void setShape3DId(Integer shape3DId) {
        this.shape3DId = shape3DId;
    }

    public String getElement3DId() {
        return element3DId;
    }

    public void setElement3DId(String element3DId) {
        this.element3DId = element3DId;
    }

    public ShapeTransform getShapeTransform() {
        return shapeTransform;
    }

    public void setShapeTransform(ShapeTransform shapeTransform) {
        this.shapeTransform = shapeTransform;
    }

    public Mesh shape3DId(Integer shape3DId) {
        setShape3DId(shape3DId);
        return this;
    }

    public Mesh element3DId(String element3DId) {
        setElement3DId(element3DId);
        return this;
    }

    public Mesh shapeTransform(ShapeTransform shapeTransform) {
        setShapeTransform(shapeTransform);
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
        Mesh mesh = (Mesh) o;
        return Objects.equals(shape3DId, mesh.shape3DId) && Objects.equals(element3DId, mesh.element3DId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape3DId, element3DId);
    }
}
