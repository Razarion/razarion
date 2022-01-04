package com.btxtech.server.persistence.asset;

import com.btxtech.shared.datatypes.shape.ShapeTransform;

import javax.persistence.Embeddable;

@Embeddable
public class ShapeTransformEmbeddable {
    private Double translateX;
    private Double translateY;
    private Double translateZ;
    private Double rotateX;
    private Double rotateY;
    private Double rotateZ;
    private Double rotateW;
    private Double scaleX;
    private Double scaleY;
    private Double scaleZ;


    public ShapeTransform toShapeTransform() {
        return new ShapeTransform()
                .setTranslateX(translateX)
                .setTranslateY(translateY)
                .setTranslateZ(translateZ)
                .setRotateX(rotateX)
                .setRotateY(rotateY)
                .setRotateZ(rotateZ)
                .setRotateW(rotateW)
                .setScaleX(scaleX)
                .setScaleY(scaleY)
                .setScaleZ(scaleZ);
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
}
