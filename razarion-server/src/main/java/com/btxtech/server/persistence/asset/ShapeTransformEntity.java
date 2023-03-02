package com.btxtech.server.persistence.asset;

import com.btxtech.shared.datatypes.shape.ShapeTransform;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ASSET_MESH_CONTAINER_TRANSFORMS")
public class ShapeTransformEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private double rotateY;
    private double rotateZ;
    private double rotateW;
    private double scaleX;
    private double scaleY;
    private double scaleZ;

    public ShapeTransform toShapeTransform() {
        return new ShapeTransform()
                .translateX(translateX)
                .translateY(translateY)
                .translateZ(translateZ)
                .rotateX(rotateX)
                .rotateY(rotateY)
                .rotateZ(rotateZ)
                .rotateW(rotateW)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .scaleZ(scaleZ);
    }

    public void fromShapeTransform(ShapeTransform shapeTransform) {
        translateX = shapeTransform.getTranslateX();
        translateY = shapeTransform.getTranslateY();
        translateZ = shapeTransform.getTranslateZ();
        rotateX = shapeTransform.getRotateX();
        rotateY = shapeTransform.getRotateY();
        rotateZ = shapeTransform.getRotateZ();
        rotateW = shapeTransform.getRotateW();
        scaleX = shapeTransform.getScaleX();
        scaleY = shapeTransform.getScaleY();
        scaleZ = shapeTransform.getScaleZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShapeTransformEntity that = (ShapeTransformEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
