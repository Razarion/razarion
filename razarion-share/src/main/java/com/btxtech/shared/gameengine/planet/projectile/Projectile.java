package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 18.10.13
 * Time: 08:08
 */
public class Projectile {
    private SyncBaseItem actor;
    private Vertex start;
    private Vertex target;
    private Vertex position;
    private double tickDistance;

    Projectile(SyncBaseItem actor, Vertex start, Vertex target) {
        this.actor = actor;
        this.start = start;
        this.target = target;
        position = start;
        tickDistance = actor.getSyncWeapon().getWeaponType().getProjectileSpeed() * PlanetService.TICK_FACTOR;
    }

    /**
     * Ticke the projectile toward the target
     *
     * @return true if more tick needed to reach target
     */
    public boolean tick() {
        double remainingDistance = position.distance(target);
        if(remainingDistance <= tickDistance) {
            return false;
        }
        position = position.interpolate(tickDistance, target);
        return true;
    }

    public SyncBaseItem getActor() {
        return actor;
    }

    public Vertex getTarget() {
        return target;
    }

    public Vertex getPosition() {
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
