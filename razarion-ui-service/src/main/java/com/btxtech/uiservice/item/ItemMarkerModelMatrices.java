package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;

/**
 * Created by Beat
 * 13.02.2017.
 */
public class ItemMarkerModelMatrices extends ModelMatrices {
    private double radius;
    private Color color;

    public ItemMarkerModelMatrices(Matrix4 model, Color color, double radius) {
        super(model);
        this.color = color;
        this.radius = radius;
    }

    @Override
    public ModelMatrices interpolateVelocity(double factor) {
        if (getInterpolatableVelocity() != null && factor != 0.0) {
            DecimalPosition interpolation = getInterpolatableVelocity().multiply(factor);
            return new ItemMarkerModelMatrices(Matrix4.createTranslation(interpolation.getX(), interpolation.getY(), 0).multiply(getModel()), color, radius);
        } else {
            return this;
        }
    }


    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }
}
