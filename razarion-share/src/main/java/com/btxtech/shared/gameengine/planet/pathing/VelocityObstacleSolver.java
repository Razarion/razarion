package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * on 24.05.2018.
 */
@Deprecated
public class VelocityObstacleSolver {
    public Orca orca;
    private SyncPhysicalMovable syncPhysicalMovable;
    private DecimalPosition bestVelocity;

    public VelocityObstacleSolver(SyncPhysicalMovable syncPhysicalMovable) {
        this.syncPhysicalMovable = syncPhysicalMovable;
    }

    public void analyzeAndAdd(SyncPhysicalMovable other) {
        double distance = syncPhysicalMovable.getDesiredPosition().sub(other.getDesiredPosition()).magnitude();
        if (distance > syncPhysicalMovable.getRadius() + other.getRadius()) {
            return;
        }
        // orca = new Orca(syncPhysicalMovable, other);
    }

    public void solve() {
        if (orca == null) {
            bestVelocity = syncPhysicalMovable.getVelocity();
        } else {
            bestVelocity = orca.getNewVelocity().divide(PlanetService.TICK_FACTOR);
        }
    }

    public void implementVelocity() {
        if (bestVelocity != null) {
            syncPhysicalMovable.setVelocity(bestVelocity);
        }
    }

    public Orca getOrca() {
        return orca;
    }

    public DecimalPosition getBestVelocity() {
        return bestVelocity;
    }
}
