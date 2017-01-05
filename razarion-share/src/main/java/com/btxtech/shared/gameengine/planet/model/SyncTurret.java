package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 18.11.2016.
 */
@Dependent
public class SyncTurret {
    private static final double MAX_DELTA_ANGLE = Math.toRadians(1);
    private SyncBaseItem syncBaseItem;
    private TurretType turretType;
    private double angle;

    public void init(SyncBaseItem syncBaseItem, TurretType turretType) {
        this.syncBaseItem = syncBaseItem;
        this.turretType = turretType;
    }

    public void tick(DecimalPosition target) {
        if (isOnTarget(target)) {
            return;
        }

        double absoluteTargetAngle = calculateAbsoluteTargetAngle(target);
        double absoluteTorrentAngle = calculateAbsoluteTorrentAngle();

        double deltaAngle = MathHelper.negateAngle(absoluteTorrentAngle) - MathHelper.negateAngle(absoluteTargetAngle);
        double moveAngle = turretType.getAngleVelocity() * PlanetService.TICK_FACTOR;
        if (Math.abs(deltaAngle) < moveAngle) {
            angle -= deltaAngle;
        } else {
            angle -= Math.signum(deltaAngle) * moveAngle;
        }
    }

    ModelMatrices createModelMatrices() {
        return ModelMatrices.createFromPositionAndZRotation(turretType.getTorrentCenter(), angle);
    }

    Matrix4 createModelMatrices4Shape3D() {
        return Matrix4.createZRotation(angle);
    }

    boolean isOnTarget(DecimalPosition target) {
        double absoluteTargetAngle = calculateAbsoluteTargetAngle(target);
        double absoluteTorrentAngle = calculateAbsoluteTorrentAngle();
        return MathHelper.compareWithPrecision(MathHelper.getAngle(absoluteTargetAngle, absoluteTorrentAngle), 0.0, MAX_DELTA_ANGLE);
    }

    private double calculateAbsoluteTargetAngle(DecimalPosition target) {
        ModelMatrices modelMatrices = syncBaseItem.getSyncPhysicalArea().getModelMatrices();
        Vertex absolutePosition = modelMatrices.getModel().multiply(turretType.getTorrentCenter(), 1.0);
        return MathHelper.normaliseAngle(absolutePosition.toXY().getAngle(target));
    }

    private double calculateAbsoluteTorrentAngle() {
        return angle + syncBaseItem.getSyncPhysicalArea().getAngle();
    }
}
