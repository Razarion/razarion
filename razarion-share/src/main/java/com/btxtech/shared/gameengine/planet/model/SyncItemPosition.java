package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 26.07.2016.
 */
public class SyncItemPosition {
    private Vertex position;
    private Vertex norm;
    private double angle;
    private double radius;

    public SyncItemPosition(Vertex position, double radius) {
        this.position = position;
        norm = Vertex.Z_NORM;
        this.radius = radius;
    }

    public ModelMatrices createModelMatrices(SyncBaseItem syncBaseItem) {
        Vertex direction = new Vertex(DecimalPosition.createVector(angle, 1.0), 0);
        double yRotation = direction.unsignedAngle(norm) - MathHelper.QUARTER_RADIANT;
        Matrix4 rotation = Matrix4.createZRotation(angle).multiply(Matrix4.createYRotation(-yRotation));
        Matrix4 translationRotation = Matrix4.createTranslation(position.getX(), position.getY(), position.getZ()).multiply(rotation);
        return new ModelMatrices().setSyncBaseItem(syncBaseItem).setModel(translationRotation).setNorm(rotation);
    }

    public Vertex getPosition() {
        return position;
    }

    public Vertex getNorm() {
        return norm;
    }

    public double getAngle() {
        return angle;
    }

    public double getRadius() {
        return radius;
    }

    public boolean hasPosition() {
        return position != null;
    }

    @Override
    public String toString() {
        return "SyncItemPosition{" +
                "position=" + position +
                ", norm=" + norm +
                ", angle=" + Math.toDegrees(angle) + "°" +
                ", radius=" + radius +
                '}';
    }
}
