package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class SyncPhysicalDirection extends SyncPhysicalArea {
    private double angle;
    private double angleVelocity;

    public SyncPhysicalDirection(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, Vertex position, Vertex norm, double angle) {
        super(syncItem, physicalAreaConfig, position, norm);
        this.angle = angle;
        angleVelocity = physicalAreaConfig.getAngularVelocity();
    }

    public double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngleVelocity() {
        return angleVelocity;
    }

    @Override
    public ModelMatrices createModelMatrices(SyncBaseItem syncBaseItem, double scale) {
        Vertex direction = new Vertex(DecimalPosition.createVector(angle, 1.0), 0);
        double yRotation = direction.unsignedAngle(getNorm()) - MathHelper.QUARTER_RADIANT;
        Matrix4 rotation = Matrix4.createZRotation(angle).multiply(Matrix4.createYRotation(-yRotation));
        Matrix4 matrix = Matrix4.createTranslation(getPosition().getX(), getPosition().getY(), getPosition().getZ()).multiply(rotation).multiply(Matrix4.createScale(scale, scale, scale));
        return new ModelMatrices().setSyncBaseItem(syncBaseItem).setModel(matrix).setNorm(matrix.normTransformation());
    }
}
