package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 18.11.2016.
 */
public class TurretType {
    private double angleVelocity;
    private Vertex turretCenter;
    private Vertex muzzlePosition;
    private String shape3dMaterialId;

    public double getAngleVelocity() {
        return angleVelocity;
    }

    public TurretType setAngleVelocity(double angleVelocity) {
        this.angleVelocity = angleVelocity;
        return this;
    }

    public Vertex getTurretCenter() {
        return turretCenter;
    }

    public TurretType setTurretCenter(Vertex turretCenter) {
        this.turretCenter = turretCenter;
        return this;
    }

    public Vertex getMuzzlePosition() {
        return muzzlePosition;
    }

    public TurretType setMuzzlePosition(Vertex muzzlePosition) {
        this.muzzlePosition = muzzlePosition;
        return this;
    }

    public String getShape3dMaterialId() {
        return shape3dMaterialId;
    }

    public TurretType setShape3dMaterialId(String shape3dMaterialId) {
        this.shape3dMaterialId = shape3dMaterialId;
        return this;
    }
}
