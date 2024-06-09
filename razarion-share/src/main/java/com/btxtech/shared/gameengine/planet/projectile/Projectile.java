package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 18.10.13
 * Time: 08:08
 */
public class Projectile {
    private final SyncBaseItem actor;
    private final DecimalPosition start;
    private final DecimalPosition target;
    private final double tickDistance;
    private DecimalPosition position;

    Projectile(SyncBaseItem actor, DecimalPosition target) {
        this.actor = actor;
        this.start = actor.getSyncPhysicalArea().getPosition();
        this.target = target;
        position = start;
        tickDistance = actor.getSyncWeapon().getWeaponType().getProjectileSpeed() * PlanetService.TICK_FACTOR;
    }

    /**
     * Tick the projectile toward the target
     *
     * @return true if more tick needed to reach target
     */
    public boolean tick() {
        double remainingDistance = position.getDistance(target);
        if (remainingDistance <= tickDistance) {
            return false;
        }
        position = position.getPointWithDistance(tickDistance, target, false);
        return true;
    }

    public SyncBaseItem getActor() {
        return actor;
    }

    public DecimalPosition getTarget() {
        return target;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Projectile{" +
                "actor=" + actor +
                ", start=" + start +
                ", target=" + target +
                '}';
    }
}
