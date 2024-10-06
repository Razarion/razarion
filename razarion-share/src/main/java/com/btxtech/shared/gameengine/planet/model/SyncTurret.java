package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.utils.MathHelper;

import javax.inject.Inject;

/**
 * Created by Beat
 * 18.11.2016.
 */

public class SyncTurret {
    private static final double MAX_DELTA_ANGLE = Math.toRadians(1);
    private SyncBaseItem syncBaseItem;
    private TurretType turretType;
    private double angle;

    @Inject
    public SyncTurret() {
    }

    public void init(SyncBaseItem syncBaseItem, TurretType turretType) {
        this.syncBaseItem = syncBaseItem;
        this.turretType = turretType;
    }

    public void tick(DecimalPosition target) {
        if (isOnTarget(target)) {
            return;
        }

        double absoluteTargetAngle = calculateAbsoluteTargetAngle(target);
        double absoluteTurretAngle = calculateAbsoluteTurretAngle();

        double deltaAngle = MathHelper.negateAngle(absoluteTurretAngle) - MathHelper.negateAngle(absoluteTargetAngle);
        double moveAngle = turretType.getAngleVelocity() * PlanetService.TICK_FACTOR;
        if (Math.abs(deltaAngle) < moveAngle) {
            angle -= deltaAngle;
        } else {
            angle -= Math.signum(deltaAngle) * moveAngle; // TODO Math.signum() return 0 if deltaAngle = 0
        }
    }

    @Deprecated
    Matrix4 createMatrix4Shape3D() {
        return Matrix4.createZRotation(angle);
    }

    public double getAngle() {
        return angle;
    }

    boolean isOnTarget(DecimalPosition target) {
        double absoluteTargetAngle = calculateAbsoluteTargetAngle(target);
        double absoluteTorrentAngle = calculateAbsoluteTurretAngle();
        return MathHelper.compareWithPrecision(MathHelper.getAngle(absoluteTargetAngle, absoluteTorrentAngle), 0.0, MAX_DELTA_ANGLE);
    }

    private double calculateAbsoluteTargetAngle(DecimalPosition target) {
        return syncBaseItem.getAbstractSyncPhysical().getPosition().getAngle(target);
    }

    private double calculateAbsoluteTurretAngle() {
        return angle + syncBaseItem.getAbstractSyncPhysical().getAngle();
    }
}
