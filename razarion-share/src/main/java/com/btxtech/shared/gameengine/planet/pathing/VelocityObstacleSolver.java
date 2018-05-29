package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 24.05.2018.
 */
public class VelocityObstacleSolver {
    public Collection<ReciprocalVelocityObstacle> reciprocalVelocityObstacles = new ArrayList<>();
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
        reciprocalVelocityObstacles.add(new ReciprocalVelocityObstacle(syncPhysicalMovable, other));
    }

    public void solve() {
        if (reciprocalVelocityObstacles.isEmpty()) {
            return;
        }
        if (reciprocalVelocityObstacles.size() != 1) {
            throw new UnsupportedOperationException();
        }
        ReciprocalVelocityObstacle reciprocalVelocityObstacle = reciprocalVelocityObstacles.stream().findFirst().get();
        Collection<DecimalPosition> possibilities = reciprocalVelocityObstacle.getPossibleIntersections();
        if (possibilities.isEmpty()) {
            return;
        }
        bestVelocity = DecimalPosition.getNearestPoint(syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR), possibilities).divide(PlanetService.TICK_FACTOR);
    }

    public void implementVelocity() {
        if (bestVelocity != null) {
            syncPhysicalMovable.setVelocity(bestVelocity);
        }
    }

    public Collection<ReciprocalVelocityObstacle> getReciprocalVelocityObstacles() {
        return reciprocalVelocityObstacles;
    }

    public DecimalPosition getBestVelocity() {
        return bestVelocity;
    }
}
