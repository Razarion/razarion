package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 18.10.13
 * Time: 08:08
 */
public class Projectile {
    private double totalDistance;
    private double speed;
    private SyncBaseItem actor;
    private Vertex muzzle;
    private Vertex target;
    private long startTime;

    public Projectile(long timeStamp, SyncBaseItem actor, Vertex muzzle, Vertex target) {
        this.actor = actor;
        this.muzzle = muzzle;
        this.target = target;
        speed = actor.getSyncWeapon().getWeaponType().getProjectileSpeed();
        totalDistance = muzzle.distance(target);
        startTime = timeStamp;
    }

    public boolean isTargetReached(long timeStamp) {
        return calculateDistance(timeStamp) > totalDistance;
    }

    public SyncBaseItem getActor() {
        return actor;
    }

    public Vertex getInterpolatedPosition(long timeStamp) {
        double distance = calculateDistance(timeStamp);
        if (distance > totalDistance) {
            distance = totalDistance;
        }
        return muzzle.interpolate(distance, target);
    }

    public Vertex getTarget() {
        return target;
    }

    private double calculateDistance(long timeStamp) {
        return (double) (timeStamp - startTime) * speed;
    }

    @Override
    public String toString() {
        return "Projectile{" +
                "totalDistance=" + totalDistance +
                ", speed=" + speed +
                ", actor=" + actor +
                ", muzzle=" + muzzle +
                ", target=" + target +
                ", startTime=" + startTime +
                '}';
    }
}
