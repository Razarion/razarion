package com.btxtech.shared.datatypes.asset;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

import java.util.List;
import java.util.Objects;

@JsType
public class Mesh {
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer threeJsModelId;
    private String element3DId;
    private List<ShapeTransform> shapeTransforms;

    public Integer getThreeJsModelId() {
        return threeJsModelId;
    }

    public void setThreeJsModelId(Integer threeJsModelId) {
        this.threeJsModelId = threeJsModelId;
    }

    public String getElement3DId() {
        return element3DId;
    }

    public void setElement3DId(String element3DId) {
        this.element3DId = element3DId;
    }

    public List<ShapeTransform> getShapeTransforms() {
        return shapeTransforms;
    }

    @SuppressWarnings("unused")
    public ShapeTransform[] toShapeTransformsArray() {
        if(shapeTransforms == null) {
            return null;
        }
        return shapeTransforms.toArray(new ShapeTransform[0]);
    }

    public void setShapeTransforms(List<ShapeTransform> shapeTransforms) {
        this.shapeTransforms = shapeTransforms;
    }

    public Mesh threeJsModelId(Integer threeJsModelId) {
        setThreeJsModelId(threeJsModelId);
        return this;
    }

    public Mesh element3DId(String element3DId) {
        setElement3DId(element3DId);
        return this;
    }

    public Mesh shapeTransforms(List<ShapeTransform> shapeTransforms) {
        setShapeTransforms(shapeTransforms);
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
        return Objects.equals(threeJsModelId, mesh.threeJsModelId) && Objects.equals(element3DId, mesh.element3DId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threeJsModelId, element3DId);
    }
}
